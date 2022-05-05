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
package com.phonepe.mustang.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.composition.impl.Disjunction;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.CriteriaForm;
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.criteria.tautology.TautologicalCriteria;
import com.phonepe.mustang.detail.impl.CheckType;
import com.phonepe.mustang.detail.impl.EqualityDetail;
import com.phonepe.mustang.detail.impl.RangeDetail;
import com.phonepe.mustang.detail.impl.RegexDetail;
import com.phonepe.mustang.detail.impl.VersioningDetail;
import com.phonepe.mustang.exception.ErrorCode;
import com.phonepe.mustang.exception.MustangException;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.mustang.ratify.RatificationResult;

public class SearchTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;

    @Before
    public void setUp() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFNegativeMatchMultiple() throws Exception {

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
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        engine.add("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDnfMultipleMatch() {
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
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6", 0.300000000003))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 0.300000000003);

        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        /* Assertions for multiple matches */
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDnfMultipleMatch1() {
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
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6", 0.300000000003))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 0.300000000003);

        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        /* Assertions for multiple matches */
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
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
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveMatch2() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 4))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", 1);

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
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
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "P");
        testQuery.put("n", 8);

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveMatch1() throws Exception {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFPositiveMatch3() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.category == 'reference')].basePrice")
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$['store']['book'][0]['basePrice']")
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.inStock == false)].category")
                                .values(Sets.newHashSet("fiction"))
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.category == 'reference')].inStock")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(
                    "{\"store\":{\"book\":[{\"category\":\"reference\",\"basePrice\":8.95,\"inStock\":true},{\"category\":\"fiction\",\"basePrice\":22.99,\"inStock\":false}]}}"))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveMatch3() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.category == 'reference')].basePrice")
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$['store']['book'][0]['basePrice']")
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.category == 'reference')].basePrice")
                                .values(Sets.newHashSet(8.90))
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.category == 'reference')].price")
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .build();
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$['store']['book'][0]['price']")
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .build();

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(
                    "{\"store\":{\"book\":[{\"category\":\"reference\",\"basePrice\":8.95,\"inStock\":true},{\"category\":\"fiction\",\"basePrice\":22.99,\"inStock\":false}]}}"))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFSearchingMultipleMatch() {
        final Criteria c1 = CNFCriteria.builder()
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
                        .build())
                .build();
        final Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .build())
                .build();
        final Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.300000000003))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 0.300000000003);
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        /* Assertions for multiple matches */
        assertThat(searchResults, hasSize(3));
        assertThat(searchResults, containsInAnyOrder("C1", "C2", "C3"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testMixedPositiveMatch() throws Exception {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testMixedNegativeMatch() throws Exception {

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
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        engine.add("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testMultiMixedPositiveMatch() throws Exception {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A3", "A4"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3", "B4"))
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
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A7", "A5", "A6"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testMultiMixedNegativeMatch() throws Exception {

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
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(5, 6, 7))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(5, 6, 7))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        engine.add("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFOnlyWithExclusionPredicateAndQueryWithNonExclusionIndexData() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        testQuery.put("b", "B10");
        testQuery.put("n", 1.000000000000001);
        testQuery.put("p", false);
        // Search query
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFQueryMultiple() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("NY"))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("F"))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .build())
                .build();
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();
        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3, 4))
                                .build())
                        .build())
                .build();
        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA", "NY"))
                                .build())
                        .build())
                .build();
        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        engine.add("testsearch", c4);
        engine.add("testsearch", c5);
        engine.add("testsearch", c6);

        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", 3);// C1 value
        testQuery.put("s", "CA");// C2 value
        testQuery.put("g", "M");// C3 value
        // Search query for same criteria
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C4", "C5"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFQueryMultiple() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.c")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.c")
                                .values(Sets.newHashSet(2))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.c")
                                .values(Sets.newHashSet(2))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .build();
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(1, 2))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .build();
        Criteria c5 = CNFCriteria.builder()
                .id("C5")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.c")
                                .values(Sets.newHashSet(1, 2))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.e")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .build();
        Criteria c6 = CNFCriteria.builder()
                .id("C6")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(1))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .build();
        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        engine.add("testsearch", c4);
        engine.add("testsearch", c5);
        engine.add("testsearch", c6);

        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", 1);// C1 value
        testQuery.put("c", 2);// C2 value
        // Search query for same criteria
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        assertThat(searchResults, hasSize(3));
        assertThat(searchResults, containsInAnyOrder("C3", "C4", "C5"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFQueryWithEachValueFromEveryCriteria() {
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
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet("D1", "D2", "D3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// C1 value
        testQuery.put("d", "D1");// C2 value
        testQuery.put("p", "P1");// C3 value
        // Search query for same criteria
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        assertThat(searchResults, hasSize(3));
        assertThat(searchResults, containsInAnyOrder("C1", "C2", "C3"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFQueryWithEachValueFromEveryCriteria1() {
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
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet("D1", "D2", "D3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet("D1", "D2", "D3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();
        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// C1 value
        testQuery.put("d", "D1");// C2 value
        testQuery.put("p", "P1");// C3 value
        // Search query for same criteria
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        assertThat(searchResults, hasSize(3));
        assertThat(searchResults, containsInAnyOrder("C1", "C2", "C3"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testNoIndexQueryEngine() throws Exception {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);
        Set<String> searchResults = new HashSet<>();
        try {
            searchResults = engine.search("test", RequestContext.builder()
                    .node(mapper.valueToTree(testQuery))
                    .build());
        } catch (MustangException e) {
            Assert.assertEquals("Error code message is not matching", "INDEX_NOT_FOUND", e.getErrorCode()
                    .toString());
        }
        assertThat(searchResults, is(empty()));
    }

    // DNF cases
    @Test
    public void testDNFQueryWithValuesPostiveAndNegativeForSameCriteria() {
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
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6", 0.300000000003))
                                .build())
                        .build())
                .build();
        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");// Negative value
        testQuery.put("n", 0.300000000003);// Postive value
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        assertThat(searchResults, is(empty()));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFQueryWithEachValueFromEveryCriteria() {
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
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet("D1", "D2", "D3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet("P1", "P2", "P3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.300000000003))
                                .build())
                        .build())
                .build();
        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// C1 value
        testQuery.put("d", "D1");// C2 value
        testQuery.put("p", "P1");// C3 value
        // Search query for same criteria
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        assertThat(searchResults, is(empty()));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFStoreIntegerAStringInCriteriaQueryTheSameValueAsInteger() {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        engine.add("test", c2);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 6); // Same value as criteria but as Integer
        // Search query
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFSingleInclusionPredicateAndQueryWithInclusionIndexData() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search query
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFSingleExclusionPredicateAndQueryWithNonExclusionIndexData() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        // Search query
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFSingleInclusionPredicateQueryEngineAndUpdateSameCriteriaQueryAgain() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));

        /* Updated Criteria -set value as FALSE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        try {
            // Index ingestion
            engine.add("test", c1);
            Assert.fail("MustangException should have been thrown");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_GENERATION_ERROR.equals(e.getErrorCode()));
        }
    }

    @Test
    public void testDNFSingleInclusionPredicateQueryEngineAndUpdateSameCriteriaQueryAgainAfterUpdate() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        /* Updated Criteria -set value as FALSE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.update("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        // Search query - with new value
        final Set<String> searchResults1 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        // Search query - with older value
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults2, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFSingleInclusionPredicateQueryEngineAndUpdateSameCriteriaQueryAgainAfterDelete() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        final Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        // Delete from the index
        engine.delete("test", c1);

        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults2.isEmpty());

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void
           testDNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        testQuery.put("a", "A1");
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));

        /* Updated Criteria Builder -set value as FALSE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        try {
            // Index ingestion
            engine.add("test", c1);
            Assert.fail("MustangException should have been thrown");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_GENERATION_ERROR.equals(e.getErrorCode()));
        }
    }

    @Test
    public void
           testDNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgainAfterUpdate() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        testQuery.put("a", "A1");
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        /* Updated Criteria Builder -set value as FALSE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.update("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        testQuery.put("a", "B1");
        // Search query with new values
        final Set<String> searchResults1 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults1, hasSize(1));
        assertThat(searchResults1, contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        testQuery.put("a", "A1");
        // Search query with new values
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults2, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void
           testDNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgainAfterDelete() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        testQuery.put("a", "A1");
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        // Remove from index
        engine.delete("test", c1);

        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults2, is(empty()));

        engine.ratify("test", false);
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
        assertThat(ratificationResult.isFullFledgedRun(), is(false));
    }

    @Test
    public void testDNFSingleExclusionPredicateQueryEngineAndUpdateSameCriteriaQueryAgain() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        /* Updated Criteria Builder -set value as FALSE */
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c2);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        // Search query - with new value
        final Set<String> searchResults1 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults1, hasSize(1));
        assertThat(searchResults1, contains("C2"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        // Search query - with older value
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults2, hasSize(1));
        assertThat(searchResults2, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void
           testDNFMultipleExclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        /* Updated Criteria Builder -set value as FALSE */
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c2);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        testQuery.put("a", "B10");
        // Search query with new values
        final Set<String> searchResults1 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults1, hasSize(1));
        assertThat(searchResults1, contains("C2"));

        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults2, hasSize(1));
        assertThat(searchResults2, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    // CNF cases
    @Test
    public void testCNFQueryWithValuesPostiveAndNegativeForSameCriteria() {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// positive value
        testQuery.put("b", "B3"); // negative value

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFStoreIntegerAStringInCriteriaQueryTheSameValueAsInteger() {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        engine.add("test", c2);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", 6); // Same value as criteria but as Integer
        // Search query
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1)); // TODO
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFOnlyWithExclusionPredicateAndQueryWithNonExclusionIndexData() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        testQuery.put("b", "B10");
        testQuery.put("n", 1.000000000000001);
        testQuery.put("p", false);

        // Search query
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());

        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFSingleInclusionPredicateAndQueryWithInclusionIndexData() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search query
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void
           testCNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));

        /* Updated Criteria Builder -set value as FALSE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        try {
            // Index ingestion
            engine.add("test", c1);
            Assert.fail("MustangException should have been thrown");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_GENERATION_ERROR.equals(e.getErrorCode()));
        }
    }

    @Test
    public void
           testCNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgainAfterUpdate() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        /* Updated Criteria Builder -set value as FALSE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.update("test", c1);

        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);

        // Search query with new values
        final Set<String> searchResults1 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));

        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);

        // Search query with new values
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults2.isEmpty());

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void
           testCNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgainAfterDelete() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        // delete from index
        engine.delete("test", c1);

        // Search query with new values
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults2.isEmpty());

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFSingleExclusionPredicateQueryEngineAndUpdateSameCriteriaQueryAgain() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        // Re-initialize engine
        engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        /* Updated Criteria Builder -set value as FALSE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        // Search query - with new value
        final Set<String> searchResults1 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        // Search query - with older value
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults2.isEmpty());

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void
           testCNFMultipleExclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));

        /* Updated Criteria Builder -set value as FALSE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        try {
            // Index ingestion
            engine.add("test", c1);
            Assert.fail("MustangException should have been thrown");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_GENERATION_ERROR.equals(e.getErrorCode()));
        }
    }

    @Test
    public void
           testCNFMultipleExclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgainAfterUpdate() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        /* Updated Criteria Builder -set value as FALSE */
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.update("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        testQuery.put("a", "B10");
        // Search query with new values
        final Set<String> searchResults1 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults2.size());
        Assert.assertTrue(searchResults2.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFWithBlankIncludePredicate() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(" "))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", " ");
        engine.add("test", c1);
        // Query engine
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFWithSpecialCharacterIncludePredicate() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("!"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "!");
        engine.add("test", c1);
        // Query engine
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFWithDoubleWhiteSpaceAndQueryWithOneWhiteSpace() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("  "))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", " ");
        engine.add("test", c1);
        // Query engine
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.isEmpty());

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    // CNF & DNF caases
    @Test
    public void testIndexDNFCNFQueryForBoth() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();

        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1,C2 value
        testQuery.put("b", "B1"); // C1 value
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testIndexDNFCNFQueryForCNF() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();

        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1 value
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C2"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testIndexDNFCNFQueryForDNF() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();

        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1 value
        testQuery.put("b", "B1"); // C1 value
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testIndexDNFCNFQueryForValueNotPresentInBothCriterias() {

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();

        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);

        // Search query -values not present in both the criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        testQuery.put("b", "B10");
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        Assert.assertEquals(0, searchResults.size());

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testIndexDNFCNFQueryWithAllValuesForBothCriteria() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6, 0.300000000003))
                                .build())
                        .build())
                .build();

        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1,C2 value
        testQuery.put("b", "B1"); // C1 value
        testQuery.put("n", 4); // C2 value
        final Set<String> searchResults = engine.search("testsearch", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        // Assertion
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFSingleExclusionNegativeMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000003))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.isEmpty());

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFSingleExclusionPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFSingleExclusionNegativeMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.isEmpty());

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFSingleExclusionPositiveMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testIndexReplacementPositive() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000003))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        final RequestContext requestContext = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        Set<String> searchResults = engine.search("test", requestContext);
        Assert.assertTrue(searchResults.isEmpty());

        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000004))
                                .build())
                        .build())
                .build();
        engine.add("test1", c1);
        engine.replaceIndex("test", "test1");
        searchResults = engine.search("test", requestContext);
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));

        try {
            searchResults = engine.search("test1", requestContext);
            Assert.fail("Should have thrown an exception");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_NOT_FOUND.equals(e.getErrorCode()));
        }
    }

    @Test
    public void testOnEmptyIndex() throws Exception {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        final RequestContext requestContext = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        try {
            engine.search("test", requestContext);
            Assert.fail("Should have thrown an exception");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_NOT_FOUND.equals(e.getErrorCode()));
        }
    }

    @Test
    public void testMultiLevelDNFPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a.value")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b.value")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n.value")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p.value")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a.value")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n.value")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        String str = "{\"a\":{\"value\":\"A1\"},\"b\":{\"value\":\"B3\"},\"n\":{\"value\":0.000000000000003},\"p\":{\"value\":true}}";

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(str))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFCriteriaSerDe() throws Exception {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Assert.assertEquals(
            "{\"form\":\"DNF\",\"id\":\"C1\",\"conjunctions\":[{\"type\":\"AND\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.a\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"A1\",\"A2\"]},\"weight\":1},{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"B2\",\"B1\"]},\"weight\":1},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[1.0E-15,2.0E-15,3.0E-15]},\"weight\":1},{\"type\":\"INCLUDED\",\"lhs\":\"$.p\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[true]},\"weight\":1}]}]}",
            mapper.writeValueAsString(c1));
        Criteria c11 = mapper.readValue(mapper.writeValueAsString(c1), Criteria.class);
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c11);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFCriteria1xxSerDe() throws Exception {
        String stringifiedCriteria = "{\"form\":\"DNF\",\"id\":\"C1\",\"conjunctions\":[{\"type\":\"AND\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.s\",\"values\":[\"CA\"],\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false},{\"type\":\"INCLUDED\",\"lhs\":\"$.g\",\"values\":[\"M\"],\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false}]}]}";
        Criteria c11 = mapper.readValue(stringifiedCriteria, Criteria.class);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", 3);
        testQuery.put("s", "CA");
        testQuery.put("g", "M");
        engine.add("test", c11);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFCriteria2xxSerDe() throws Exception {
        final String stringifiedCriteria = "{\"form\":\"DNF\",\"id\":\"C1\",\"conjunctions\":[{\"type\":\"AND\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.a\",\"detail\":{\"caveat\":\"REGEX\",\"regex\":\"A.*\"},\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false},{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"B2\",\"B1\"]},\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":true},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"detail\":{\"caveat\":\"RANGE\",\"lowerBound\":4.9E-324,\"upperBound\":3.0E-15,\"includeLowerBound\":false,\"includeUpperBound\":true},\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false},{\"type\":\"INCLUDED\",\"lhs\":\"$.p\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[true]},\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false}]}]}";
        Criteria c11 = mapper.readValue(stringifiedCriteria, Criteria.class);
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c11);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFCriteriaSerDe() throws Exception {
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
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Assert.assertEquals(
            "{\"form\":\"CNF\",\"id\":\"C1\",\"disjunctions\":[{\"type\":\"OR\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.a\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"A1\",\"A2\"]},\"weight\":1},{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"B2\",\"B1\"]},\"weight\":1},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[1.0E-15,2.0E-15,3.0E-15]},\"weight\":1},{\"type\":\"INCLUDED\",\"lhs\":\"$.p\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[true]},\"weight\":1}]}]}",
            mapper.writeValueAsString(c1));
        Criteria c11 = mapper.readValue(mapper.writeValueAsString(c1), Criteria.class);

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// positive value
        testQuery.put("b", "B3"); // negative value

        engine.add("test", c11);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFCriteria1xxSerDe() throws Exception {
        final String stringifiedCriteria = "{\"form\":\"CNF\",\"id\":\"C1\",\"disjunctions\":[{\"type\":\"OR\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.a\",\"values\":[\"A1\",\"A2\"],\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false},{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"values\":[\"B2\",\"B1\"],\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":true},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"values\":[1.0E-15,2.0E-15,3.0E-15],\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false},{\"type\":\"INCLUDED\",\"lhs\":\"$.p\",\"values\":[true],\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false}]}]}";
        Criteria c11 = mapper.readValue(stringifiedCriteria, Criteria.class);

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// positive value
        testQuery.put("b", "B3"); // negative value

        engine.add("test", c11);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFCriteria2xxSerDe() throws Exception {
        final String stringifiedCriteria = "{\"form\":\"CNF\",\"id\":\"C1\",\"disjunctions\":[{\"type\":\"OR\",\"predicates\":[{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"B3\"]},\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":true},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"detail\":{\"caveat\":\"RANGE\",\"lowerBound\":3.0E-15,\"upperBound\":1.7976931348623157E308,\"includeLowerBound\":true,\"includeUpperBound\":false},\"lhsNotAPath\":false,\"weight\":1,\"defaultResult\":false}]}]}";
        Criteria c11 = mapper.readValue(stringifiedCriteria, Criteria.class);

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);

        engine.add("test", c11);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testPassThroughCriterias() throws Exception {
        Criteria c1 = TautologicalCriteria.generate(CriteriaForm.CNF, "C1");
        Criteria c2 = TautologicalCriteria.generate(CriteriaForm.DNF, "C2");
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testSearchOnNonExistentIndex() throws Exception {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        final RequestContext requestContext = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        try {
            engine.search("test", requestContext);
            Assert.fail("MustangException should have been thrown.");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_NOT_FOUND.equals(e.getErrorCode()));
        }
    }

    @Test
    public void testDNFAddDeleteAdd() {
        Criteria c1;
        /* Initial Criteria Builder -set value as TRUE */
        c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        // Index ingestion
        engine.add("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        testQuery.put("a", "A1");
        // Search Engine call
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));

        // Remove from index
        engine.delete("test", c1);

        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults2.isEmpty());

        engine.add("test", c1);
        // Search Engine call
        final Set<String> searchResults3 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults3.size());
        Assert.assertTrue(searchResults3.contains("C1"));

        engine.ratify("test");
        ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFAddDeleteAdd() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        Assert.assertTrue(searchResults.contains("C1"));

        // Remove from index
        engine.delete("test", c1);

        final Set<String> searchResults2 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults2.isEmpty());

        engine.add("test", c1);
        // Search Engine call
        final Set<String> searchResults3 = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertEquals(1, searchResults3.size());
        Assert.assertTrue(searchResults3.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFNextHigherFixCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.operatorId")
                                .values(Sets.newHashSet("AIRTELPRE"))
                                .build())
                        .build())
                .build();

        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("V", "P"))
                                .build())
                        .build())
                .build();

        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        String str = "{\"context\":{\"categoryId\":\"M\",\"subCategoryId\":\"PREPAID\",\"operatorId\":\"AIRTELPRE\",\"RCircle\":\"KK\",\"RPlanType\":\"AIRTELPRE_TALKTIME\",\"RNumber\":\"XXYXXXYXZZ\",\"mode\":\"R\"}}";
        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        engine.add("test", c5);
        engine.add("test", c6);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(str))
                .build(),
            false);
        Assert.assertEquals(5, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
        Assert.assertTrue(searchResults.contains("C3"));
        Assert.assertFalse(searchResults.contains("C4"));
        Assert.assertTrue(searchResults.contains("C5"));
        Assert.assertTrue(searchResults.contains("C6"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFNextHigherFixCheck() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.operatorId")
                                .values(Sets.newHashSet("AIRTELPRE"))
                                .build())
                        .build())
                .build();

        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("V", "P"))
                                .build())
                        .build())
                .build();

        Criteria c5 = CNFCriteria.builder()
                .id("C5")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        Criteria c6 = CNFCriteria.builder()
                .id("C6")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("R"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.categoryId")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();

        String str = "{\"context\":{\"categoryId\":\"M\",\"subCategoryId\":\"PREPAID\",\"operatorId\":\"AIRTELPRE\",\"RCircle\":\"KK\",\"RPlanType\":\"AIRTELPRE_TALKTIME\",\"RNumber\":\"XXYXXXYXZZ\",\"mode\":\"R\"}}";
        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        engine.add("test", c5);
        engine.add("test", c6);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(str))
                .build(),
            false);
        Assert.assertEquals(5, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
        Assert.assertTrue(searchResults.contains("C3"));
        Assert.assertFalse(searchResults.contains("C4"));
        Assert.assertTrue(searchResults.contains("C5"));
        Assert.assertTrue(searchResults.contains("C6"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFNextIdFixCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("A", "P"))
                                .build())
                        .build())
                .build();

        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("V", "P"))
                                .build())
                        .build())
                .build();

        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("A"))
                                .build())
                        .build())
                .build();

        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("V", "P"))
                                .build())
                        .build())
                .build();

        String str = "{\"uId\":\"U123456789\",\"mId\":\"ACME\",\"amount\":1000000,\"context\":{\"receiverType\":\"P\",\"receiverId\":\"9999999999\",\"mode\":\"PP\"}}";
        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(str))
                .build(),
            false);
        Assert.assertEquals(3, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
        Assert.assertFalse(searchResults.contains("C3"));
        Assert.assertTrue(searchResults.contains("C4"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFNextIdFixCheck() throws Exception {

        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("A", "P"))
                                .build())
                        .build())
                .build();

        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("V", "P"))
                                .build())
                        .build())
                .build();

        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("A"))
                                .build())
                        .build())
                .build();

        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.mode")
                                .values(Sets.newHashSet("PP"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.context.receiverType")
                                .values(Sets.newHashSet("V", "P"))
                                .build())
                        .build())
                .build();

        String str = "{\"uId\":\"U123456789\",\"mId\":\"ACME\",\"amount\":1000000,\"context\":{\"receiverType\":\"P\",\"receiverId\":\"9999999999\",\"mode\":\"PP\"}}";
        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(str))
                .build(),
            false);
        Assert.assertEquals(3, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
        Assert.assertFalse(searchResults.contains("C3"));
        Assert.assertTrue(searchResults.contains("C4"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
        assertThat(ratificationResult.isFullFledgedRun(), is(true));

    }

    @Test
    public void testDNFPositiveRegexMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.*")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFPositiveRangeCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.*")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("B1", "B2"))
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveRegexMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.?")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3"))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveRangeMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
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
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveMultiRangeMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000003))
                                .build())
                        .build())
                .build();
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RegexDetail.builder()
                                        .regex("[0-9]*\\.?[0-9]*")
                                        .build())
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
        engine.add("test", c3);
        engine.add("test", c4);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(3));
        assertThat(searchResults, containsInAnyOrder("C1", "C2", "C3"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFPositiveMultipleRangeCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000004)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000002)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c7 = DNFCriteria.builder()
                .id("C7")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c8 = DNFCriteria.builder()
                .id("C8")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000004)
                                        .includeLowerBound(true)
                                        .build())
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
        engine.add("test", c3);
        engine.add("test", c4);
        engine.add("test", c5);
        engine.add("test", c6);
        engine.add("test", c7);
        engine.add("test", c8);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(4));
        assertThat(searchResults, containsInAnyOrder("C1", "C3", "C5", "C7"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveMultipleRangeCheck() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000004)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000002)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c5 = CNFCriteria.builder()
                .id("C5")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c6 = CNFCriteria.builder()
                .id("C6")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c7 = CNFCriteria.builder()
                .id("C7")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c8 = CNFCriteria.builder()
                .id("C8")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000004)
                                        .includeLowerBound(true)
                                        .build())
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
        engine.add("test", c3);
        engine.add("test", c4);
        engine.add("test", c5);
        engine.add("test", c6);
        engine.add("test", c7);
        engine.add("test", c8);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(4));
        assertThat(searchResults, containsInAnyOrder("C1", "C3", "C5", "C7"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testFullRangeDetail() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder() // Test across the number range
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x") // path doesn't exist
                                .detail(RangeDetail.builder()
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.x") // path doesn't exist
                                .detail(RangeDetail.builder()
                                        .build())
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", 0.000000000000003);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C3"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFPositiveVersioningCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.*")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("B1", "B2"))
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("1.2")
                                        .build())
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", "5.7.40");
        testQuery.put("x", 1.2); // Not a number and hence shall fail
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveVersioningMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("1.2")
                                        .build())
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", "5.7.40");
        testQuery.put("x", 1.2); // Not a number and hence shall fail.
        testQuery.put("p", false);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveMultiVersioningMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.39")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.41")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("5.7.40"))
                                .build())
                        .build())
                .build();
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", "5.7.40");
        testQuery.put("p", false);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(3));
        assertThat(searchResults, containsInAnyOrder("C1", "C2", "C3"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFPositiveMultipleVersioningCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.41")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.40")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.39")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.41")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c7 = DNFCriteria.builder()
                .id("C7")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.39")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c8 = DNFCriteria.builder()
                .id("C8")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", "5.7.40");
        testQuery.put("p", false);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        engine.add("test", c5);
        engine.add("test", c6);
        engine.add("test", c7);
        engine.add("test", c8);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(4));
        assertThat(searchResults, containsInAnyOrder("C1", "C3", "C5", "C7"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testCNFPositiveMultipleVersioningCheck() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.41")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.40")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.39")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c5 = CNFCriteria.builder()
                .id("C5")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c6 = CNFCriteria.builder()
                .id("C6")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.41")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c7 = CNFCriteria.builder()
                .id("C7")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.39")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c8 = CNFCriteria.builder()
                .id("C8")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .excludeBase(true)
                                        .build())
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", "5.7.40");
        testQuery.put("p", false);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        engine.add("test", c5);
        engine.add("test", c6);
        engine.add("test", c7);
        engine.add("test", c8);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        assertThat(searchResults, hasSize(4));
        assertThat(searchResults, containsInAnyOrder("C1", "C3", "C5", "C7"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDetailSerDe() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.*")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("B1", "B2"))
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Assert.assertEquals(
            "{\"form\":\"DNF\",\"id\":\"C1\",\"conjunctions\":[{\"type\":\"AND\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.a\",\"detail\":{\"caveat\":\"REGEX\",\"regex\":\"A.*\"},\"weight\":1},{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[\"B2\",\"B1\"]},\"weight\":1},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"detail\":{\"caveat\":\"VERSIONING\",\"check\":\"ABOVE\",\"baseVersion\":\"5.7.40\",\"excludeBase\":false,\"normalisedView\":\"ABOVE#5.7.40#false\"},\"weight\":1},{\"type\":\"INCLUDED\",\"lhs\":\"$.x\",\"detail\":{\"caveat\":\"RANGE\",\"lowerBound\":3.0E-15,\"upperBound\":1.7976931348623157E308,\"includeLowerBound\":true,\"includeUpperBound\":false,\"normalisedView\":\"3.0E-15#1.7976931348623157E308#true#false\"},\"weight\":1},{\"type\":\"INCLUDED\",\"lhs\":\"$.p\",\"detail\":{\"caveat\":\"EQUALITY\",\"values\":[true]},\"weight\":1}]}]}",
            mapper.writeValueAsString(c1));
        Criteria c11 = mapper.readValue(mapper.writeValueAsString(c1), Criteria.class);
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", "5.7.40");
        testQuery.put("x", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c11);
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build());
        Assert.assertTrue(searchResults.contains("C1"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDNFLinkages() throws IOException {

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("abc"))
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c11 = DNFCriteria.builder()
                .id("C11")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("abc"))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("F"))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .build())
                .build();
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();
        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3, 4))
                                .build())
                        .build())
                .build();
        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA", "NY"))
                                .build())
                        .build())
                .build();

        Criteria c7 = TautologicalCriteria.generate(CriteriaForm.CNF, "C7");
        Criteria c8 = TautologicalCriteria.generate(CriteriaForm.DNF, "C8");
        Criteria c9 = TautologicalCriteria.generate(CriteriaForm.CNF, "C9");

        // Index ingestion
        engine.add("test", c1);
        engine.add("test", c11);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        engine.add("test", c5);
        engine.add("test", c6);
        engine.add("test", c7);
        engine.add("test", c8);
        engine.add("test", c9);

        String testquery = "{\"a\":\"abc\"}";
        // Search query for same criteria
        final Set<String> searchResults = engine.search("test", RequestContext.builder()
                .node(mapper.readTree(testquery))
                .build());
        assertThat(searchResults, hasSize(4));
        assertThat(searchResults, containsInAnyOrder("C6", "C7", "C8", "C9"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

}
