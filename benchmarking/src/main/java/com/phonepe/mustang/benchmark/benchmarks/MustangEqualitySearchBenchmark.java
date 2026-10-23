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
package com.phonepe.mustang.benchmark.benchmarks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.benchmark.utils.Utils;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;

import lombok.Getter;

/**
 * Search latency for indexes dominated by EQUALITY keys, with and without the equality key lookup.
 *
 * <ul>
 * <li>{@code DISTINCT_VALUES}: every criteria matches one distinct value on a single path (e.g. one geo-fence id per
 * offer). The number of distinct keys grows with the index size.</li>
 * <li>{@code VALUE_LISTS}: every criteria lists 50 values from a large pool on one path, plus one of 5 values on a
 * second path (e.g. allowed pricing plans per offer, per tenant).</li>
 * </ul>
 */
public class MustangEqualitySearchBenchmark {

    private static final int REQUEST_POOL_SIZE = 1024;

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkContext {
        @Param({ "1000", "10000" })
        private int indexSize;

        @Param({ "DISTINCT_VALUES", "VALUE_LISTS" })
        private String shape;

        @Param({ "true", "false" })
        private boolean equalityKeyLookupEnabled;

        private final ObjectMapper mapper = new ObjectMapper();
        private MustangEngine engine;
        private RequestContext[] requests;

        @Setup(Level.Trial)
        public void setUp() {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            engine = MustangEngine.builder()
                    .mapper(mapper)
                    .equalityKeyLookupEnabled(equalityKeyLookupEnabled)
                    .build();
            final Random random = new Random(7);
            final List<Criteria> criterias = new ArrayList<>(indexSize);
            final int valuePool = indexSize * 10;
            for (int i = 0; i < indexSize; i++) {
                final Conjunction.ConjunctionBuilder conjunction = Conjunction.builder();
                if ("DISTINCT_VALUES".equals(shape)) {
                    conjunction.predicate(IncludedPredicate.builder()
                            .lhs("$.fenceId")
                            .values(Sets.newHashSet("FE" + i))
                            .build());
                } else {
                    final Set<Object> plans = new HashSet<>();
                    while (plans.size() < 50) {
                        plans.add("PLAN" + random.nextInt(valuePool));
                    }
                    conjunction.predicate(IncludedPredicate.builder()
                            .lhs("$.pricingPlanId")
                            .values(plans)
                            .build())
                            .predicate(IncludedPredicate.builder()
                                    .lhs("$.tenant")
                                    .values(Sets.newHashSet("T" + (i % 5)))
                                    .build());
                }
                criterias.add(DNFCriteria.builder()
                        .id("C" + i)
                        .conjunction(conjunction.build())
                        .build());
            }
            engine.add(Utils.INDEX_NAME, criterias);

            requests = new RequestContext[REQUEST_POOL_SIZE];
            for (int r = 0; r < REQUEST_POOL_SIZE; r++) {
                final Map<String, Object> request = new HashMap<>();
                if ("DISTINCT_VALUES".equals(shape)) {
                    request.put("fenceId", "FE" + random.nextInt(indexSize * 2)); // ~50% hit rate
                } else {
                    request.put("pricingPlanId", "PLAN" + random.nextInt(valuePool));
                    request.put("tenant", "T" + random.nextInt(5));
                }
                requests[r] = RequestContext.builder()
                        .node(mapper.valueToTree(request))
                        .build();
            }
        }
    }

    @State(Scope.Thread)
    public static class Cursor {
        private int next;

        int next() {
            next = (next + 1) & (REQUEST_POOL_SIZE - 1);
            return next;
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 3, time = 5)
    @Threads(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void search(final Blackhole blackhole, final BenchmarkContext context, final Cursor cursor) {
        blackhole.consume(context.getEngine()
                .search(Utils.INDEX_NAME, context.getRequests()[cursor.next()], false));
    }

}
