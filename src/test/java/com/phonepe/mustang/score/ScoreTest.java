/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
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
package com.phonepe.mustang.score;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.composition.impl.Disjunction;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;

public class ScoreTest {

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
                                .weight(10L)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.contains("C1"));
        assertThat(engine.score(c2, context), is(-1.0));
        assertThat(engine.score(c1, context), is(30.0));

        final List<Pair<String, Double>> scores = engine.score(Arrays.asList(c1, c2), context);
        Assert.assertTrue(scores.get(0)
                .getKey()
                .equals("C1")
                && scores.get(0)
                        .getValue() == 30);
        Assert.assertTrue(scores.get(1)
                .getKey()
                .equals("C2")
                && scores.get(1)
                        .getValue() == -1);
    }

    @Test
    public void testDNFPositiveMatchImplicitWeights() throws Exception {
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
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(0L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(0L)
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.contains("C1"));
        assertThat(engine.score(c2, context), is(-1.0));
        assertThat(engine.score(c1, context), is(3.0));

        final List<Pair<String, Double>> scores = engine.score(Arrays.asList(c1, c2), context);
        Assert.assertTrue(scores.get(0)
                .getKey()
                .equals("C1")
                && scores.get(0)
                        .getValue() == 3);
        Assert.assertTrue(scores.get(1)
                .getKey()
                .equals("C2")
                && scores.get(1)
                        .getValue() == -1);
    }

    @Test
    public void testDNFNegativeMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        engine.add("test", Lists.asList(c1, new Criteria[] { c2 }));
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.isEmpty());
        assertThat(engine.score(c1, context), is(-1.0));
        assertThat(engine.score(c2, context), is(-1.0));
    }

    @Test
    public void testCNFPositiveMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .weight(10L)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", false);

        engine.add("test", c1);
        engine.add("test", c2);
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));
        assertThat(engine.score(c1, context), is(10.0));
        assertThat(engine.score(c2, context), is(-1.0));
    }

    @Test
    public void testCNFPositiveMatchImplicitWeights() throws Exception {
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
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(0L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(0L)
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", false);

        engine.add("test", c1);
        engine.add("test", c2);
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));
        assertThat(engine.score(c1, context), is(1.0));
        assertThat(engine.score(c2, context), is(-1.0));
    }

    @Test
    public void testCNFNegativeMatch() throws Exception {

        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "P");
        testQuery.put("n", 8);

        engine.add("test", c1);
        engine.add("test", c2);
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.isEmpty());
        assertThat(engine.score(c1, context), is(-1.0));
        assertThat(engine.score(c2, context), is(-1.0));

    }

    @Test
    public void testMultiMixedPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .weight(10L)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(10L)
                                .build())
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A3", "A4"))
                                .weight(10L)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3", "B4"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();

        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A7", "A2"))
                                .weight(10L)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .weight(10L)
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10L)
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A7", "A5", "A6"))
                                .weight(10L)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .weight(10L)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();

        Set<String> searchResults = engine.search("test", context);
        assertThat(searchResults, hasSize(2));
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));

        searchResults = engine.search("test", context, 1);
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));
        assertThat(engine.score(c1, context), is(30.0));
        assertThat(engine.score(c2, context), is(20.0));
        assertThat(engine.score(c3, context), is(-1.0));

        searchResults = engine.search("test", context, 3);
        assertThat(searchResults, hasSize(2));
        final String[] searchResultsArr = searchResults.toArray(new String[0]);
        assertThat(searchResultsArr[0], is("C1"));
        assertThat(searchResultsArr[1], is("C2"));

    }

}
