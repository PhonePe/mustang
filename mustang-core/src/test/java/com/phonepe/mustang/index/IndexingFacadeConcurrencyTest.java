/**
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phonepe.mustang.index;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;

/**
 * Reproduces GH-9: the search/read path (MustangEngine.search/scan/checkSimilarity,
 * SearchFacade.search) takes no lock at all, while add/update/delete/replace correctly
 * serialize against each other via IndexingFacade's write lock. A search running
 * concurrently with index mutation can observe a torn/partially-mutated structure.
 *
 * <p>This is a data race, not a deterministic failure, so the test drives a writer
 * thread and several reader threads against the same index for a fixed wall-clock
 * window and fails if either side throws, or if the pool doesn't wind down shortly
 * after. On the pre-fix code this reliably either throws
 * {@code ConcurrentModificationException} or hangs with reader threads stuck inside
 * {@code TreeMap}/{@code TreeSet} traversal (an infinite loop over a structure being
 * concurrently mutated) well inside the run window.
 */
public class IndexingFacadeConcurrencyTest {

    // Kept small and steady-state (rather than growing/shrinking 0->1 each
    // iteration) so readers iterate a non-trivial collection while the writer
    // repeatedly removes and re-inserts into the *same* nested TreeMap bucket.
    private static final int STEADY_STATE_SIZE = 30;
    private static final int READER_THREADS = 8;
    private static final long RUN_DURATION_MILLIS = 90_000;
    // Generous headroom over RUN_DURATION_MILLIS: on the pre-fix code this
    // doesn't matter (it fails via exception/hang well inside the run window),
    // and on the fixed code every thread is merely checking an AtomicBoolean
    // or finishing its current, already-acquired lock operation, not starting
    // new contended work, so it should return almost immediately after
    // writerDone flips.
    private static final long SHUTDOWN_GRACE_SECONDS = 120;

    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;
    private RequestContext matchingContext;

    @Before
    public void setUp() {
        engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        final Map<String, Object> query = Maps.newHashMap();
        query.put("a", "A1");
        matchingContext = RequestContext.builder()
                .node(mapper.valueToTree(query))
                .build();
    }

    @Test
    public void searchDoesNotRaceAddDelete() throws InterruptedException {
        final String index = "race-test";
        final long deadline = System.currentTimeMillis() + RUN_DURATION_MILLIS;
        final AtomicBoolean stop = new AtomicBoolean(false);
        final AtomicLong writerCycles = new AtomicLong();
        final AtomicLong readerSearches = new AtomicLong();
        final CopyOnWriteArrayList<Throwable> failures = new CopyOnWriteArrayList<>();
        final ExecutorService pool = Executors.newFixedThreadPool(READER_THREADS + 1);
        final CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < STEADY_STATE_SIZE; i++) {
            engine.add(index, criteria("seed-" + i));
        }

        pool.submit(() -> {
            try {
                start.await();
                while (!stop.get() && System.currentTimeMillis() < deadline) {
                    for (int i = 0; i < STEADY_STATE_SIZE && !stop.get(); i++) {
                        final Criteria criteria = criteria("seed-" + i);
                        engine.delete(index, criteria);
                        engine.add(index, criteria);
                    }
                    writerCycles.incrementAndGet();
                }
            } catch (Throwable t) {
                failures.add(t);
            } finally {
                stop.set(true);
            }
        });

        for (int r = 0; r < READER_THREADS; r++) {
            pool.submit(() -> {
                try {
                    start.await();
                    while (!stop.get()) {
                        try {
                            engine.search(index, matchingContext);
                            readerSearches.incrementAndGet();
                        } catch (com.phonepe.mustang.exception.MustangException e) {
                            // INDEX_NOT_FOUND before the first add() is a legitimate,
                            // non-racy outcome; anything else is the bug under test.
                            if (!"INDEX_NOT_FOUND".equals(e.getErrorCode()
                                    .name())) {
                                throw e;
                            }
                        }
                    }
                } catch (Throwable t) {
                    failures.add(t);
                }
            });
        }

        start.countDown();
        pool.shutdown();
        final boolean finished = pool.awaitTermination(SHUTDOWN_GRACE_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            stop.set(true);
            final StringBuilder dump = new StringBuilder();
            dump.append("reader/writer threads did not wind down within ")
                    .append(SHUTDOWN_GRACE_SECONDS)
                    .append("s of the ")
                    .append(RUN_DURATION_MILLIS)
                    .append("ms run window closing (writer completed ")
                    .append(writerCycles.get())
                    .append(" full churn cycles, readers completed ")
                    .append(readerSearches.get())
                    .append(" searches) — stuck, not merely slow:\n");
            Thread.getAllStackTraces()
                    .forEach((thread, trace) -> {
                        if (thread.getName()
                                .startsWith("pool-")) {
                            dump.append(thread)
                                    .append('\n');
                            for (final StackTraceElement e : trace) {
                                dump.append("\tat ")
                                        .append(e)
                                        .append('\n');
                            }
                        }
                    });
            Assert.fail(dump.toString());
        }

        if (!failures.isEmpty()) {
            final Throwable first = failures.get(0);
            Assert.fail("search() raced add()/delete() and threw " + failures.size()
                    + " exception(s) (writer completed " + writerCycles.get() + " churn cycles, readers completed "
                    + readerSearches.get() + " searches); first: " + first);
        }

        Assert.assertTrue("writer made no progress at all (" + writerCycles.get() + " cycles) — test is not exercising"
                + " real contention", writerCycles.get() > 0);
        Assert.assertTrue("readers made no progress at all (" + readerSearches.get() + " searches) — test is not"
                + " exercising real contention", readerSearches.get() > 0);
    }

    private Criteria criteria(final String id) {
        return DNFCriteria.builder()
                .id(id)
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
    }

}
