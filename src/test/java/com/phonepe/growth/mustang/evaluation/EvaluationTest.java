/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.evaluation;

import java.util.Map;

import com.phonepe.growth.mustang.MustangEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class EvaluationTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
    }

    @Test
    public void testDNFPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        Assert.assertTrue(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));
    }

    @Test
    public void testDNFNegativeMatch() throws Exception {

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        Assert.assertFalse(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));

    }

    @Test
    public void testCNFPositiveMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A4");
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", false);

        Assert.assertTrue(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));
    }

    @Test
    public void testCNFNegativeMatch() throws Exception {

        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "C");
        testQuery.put("n", "7");

        Assert.assertFalse(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));

    }

    @Test
    public void testPredicateWithAbsentPath() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", "7"); // path $.a is not being given explicitly

        Assert.assertFalse(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));

    }

    @Test
    public void testCriteriaHavingPredicateWithAbsentPath() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", "7"); // path $.a is not being given explicitly

        Assert.assertTrue(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));

    }

    @Test
    public void testPredicateWithANonJsonPath() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("A") // Not a valid path
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "B"); // Doesn't matter as the predicate setup above already satisfies itself.
        testQuery.put("n", "7");

        Assert.assertFalse(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));

        Criteria c2 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("1")
                                .values(Sets.newHashSet("6", "7")) // Never satisfied
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.evaluate(c2,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));

    }

}
