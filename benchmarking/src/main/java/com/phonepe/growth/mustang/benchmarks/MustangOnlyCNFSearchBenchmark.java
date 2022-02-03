/**
 * Copyright (c) 2022 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
 *
 */
package com.phonepe.growth.mustang.benchmarks;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
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
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.growth.mustang.utils.Utils;

import lombok.Getter;

public class MustangOnlyCNFSearchBenchmark {

    @Getter
    @State(Scope.Benchmark)
    public static class BenchmarkContext {
        @Param({ "10", "100", "1000", "10000" })
        private int indexSize;

        private final ObjectMapper mapper = new ObjectMapper();
        private MustangEngine engine;
        private RequestContext requestContext;

        @Setup(Level.Trial)
        public void setUp() {

            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            engine = MustangEngine.builder()
                    .mapper(mapper)
                    .build();

            for (int i = 0; i < indexSize; i++) {
                Collections.shuffle(Utils.PATHS);

                final Criteria c = DNFCriteria.builder()
                        .id(UUID.randomUUID()
                                .toString())
                        .conjunction(Conjunction.builder()
                                .predicates(Utils.PATHS.subList(0, Utils.RANDOM.nextInt(5) + 1)
                                        .stream()
                                        .map(path -> {
                                            if (Utils.RANDOM.nextInt(5) != 0) { // 80-20 split
                                                return IncludedPredicate.builder()
                                                        .lhs("$." + path)
                                                        .values(Sets.newHashSet(Utils.getRandom()))
                                                        .build();
                                            }
                                            return ExcludedPredicate.builder()
                                                    .lhs("$." + path)
                                                    .values(Sets.newHashSet(Utils.getRandom()))
                                                    .build();
                                        })
                                        .collect(Collectors.toList()))
                                .build())
                        .build();
                engine.add(Utils.INDEX_NAME, c);

            }

        }

        @Setup(Level.Invocation)
        public void prepareContext() {
            requestContext = RequestContext.builder()
                    .node(mapper.valueToTree(Utils.PATHS.stream()
                            .collect(Collectors.toMap(x -> x, x -> Utils.getRandom()))))
                    .build();
        }

    }

    @Benchmark
    @Measurement(iterations = 1)
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Threads(Threads.MAX)
    @BenchmarkMode(Mode.Throughput)
    public void search(final Blackhole blackhole, final BenchmarkContext context) {
        blackhole.consume(context.getEngine()
                .search(Utils.INDEX_NAME, context.getRequestContext()));
    }

}
