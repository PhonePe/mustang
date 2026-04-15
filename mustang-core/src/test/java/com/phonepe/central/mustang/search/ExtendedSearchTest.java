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
 */
package com.phonepe.central.mustang.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import com.phonepe.central.mustang.MustangEngine;
import com.phonepe.central.mustang.common.RequestContext;
import com.phonepe.central.mustang.composition.impl.Conjunction;
import com.phonepe.central.mustang.composition.impl.Disjunction;
import com.phonepe.central.mustang.criteria.Criteria;
import com.phonepe.central.mustang.criteria.impl.CNFCriteria;
import com.phonepe.central.mustang.criteria.impl.DNFCriteria;
import com.phonepe.central.mustang.exception.ErrorCode;
import com.phonepe.central.mustang.exception.MustangException;
import com.phonepe.central.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.central.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.central.mustang.preoperation.impl.AdditionPreOperation;
import com.phonepe.central.mustang.preoperation.impl.BinaryConversionPreOperation;
import com.phonepe.central.mustang.preoperation.impl.DateExtractionImpl;
import com.phonepe.central.mustang.preoperation.impl.DateExtractionType;
import com.phonepe.central.mustang.preoperation.impl.DateTimePreOperation;
import com.phonepe.central.mustang.preoperation.impl.DivisionPreOperation;
import com.phonepe.central.mustang.preoperation.impl.LengthPreOperation;
import com.phonepe.central.mustang.preoperation.impl.ModuloPreOperation;
import com.phonepe.central.mustang.preoperation.impl.MultiplicationPreOperation;
import com.phonepe.central.mustang.preoperation.impl.SizePreOperation;
import com.phonepe.central.mustang.preoperation.impl.SubStringPreOperation;
import com.phonepe.central.mustang.preoperation.impl.SubtractionPreOperation;
import com.phonepe.central.mustang.preoperation.impl.ToDateTimePreOperation;
import com.phonepe.central.mustang.ratify.RatificationResult;

public class ExtendedSearchTest {

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
                                .preOperation(SubtractionPreOperation.builder()
                                        .rhs(0.0000001)
                                        .build())
                                .values(Sets.newHashSet(0.0000001, 0.0000002, 0.0000003))
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
                                .lhs("$.x")
                                .preOperation(BinaryConversionPreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.0000004);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
    public void testDNFPositiveMatch1() throws Exception {
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
                                .preOperation(BinaryConversionPreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet("00000000000000000000000001111111"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .preOperation(BinaryConversionPreOperation.builder()
                                        .build())
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
        testQuery.put("n", 127);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(SubStringPreOperation.builder()
                                        .beginIndex(0)
                                        .endIndex(1)
                                        .build())
                                .values(Sets.newHashSet("AB", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .preOperation(SubtractionPreOperation.builder()
                                        .rhs(7) // Has no impact as lhs is a string
                                        .build())
                                .values(Sets.newHashSet("7"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "AB");
        testQuery.put("n", "7");

        engine.add("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(ModuloPreOperation.builder()
                                        .rhs(4)
                                        .build())
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
                                .preOperation(ModuloPreOperation.builder()
                                        .rhs(3)
                                        .build())
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
                                .preOperation(DivisionPreOperation.builder()
                                        .rhs(10)
                                        .build())
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", 10);

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C2"));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
        assertThat(ratificationResult.getStatus(), is(true));
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
                                .preOperations(List.of(SubtractionPreOperation.builder()
                                        .rhs(2)
                                        .build(),
                                        SubtractionPreOperation.builder()
                                                .rhs(2)
                                                .build()))
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
                                .preOperations(List.of(AdditionPreOperation.builder()
                                        .rhs(2)
                                        .build(),
                                        AdditionPreOperation.builder()
                                                .rhs(2)
                                                .build()))
                                .values(Sets.newHashSet(4, 5, 6))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "P");
        testQuery.put("n", 4);

        engine.add("test", c1);
        engine.add("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(LengthPreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet(1))
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(SubStringPreOperation.builder()
                                        .beginIndex(0)
                                        .endIndex(1)
                                        .build())
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$['store']['book'][0]['basePrice']")
                                .preOperation(DivisionPreOperation.builder()
                                        .rhs(1)
                                        .build())
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(MultiplicationPreOperation.builder()
                                        .rhs(1)
                                        .build())
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$['store']['book'][0]['basePrice']")
                                .preOperation(SizePreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.category == 'reference')].basePrice")
                                .preOperation(LengthPreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet(8.90))
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.store.book[?(@.category == 'reference')].basePrice")
                                .preOperations(List.of(SizePreOperation.builder()
                                        .build(),
                                        MultiplicationPreOperation.builder()
                                                .rhs(1)
                                                .build()))
                                .values(Sets.newHashSet(1)) // Size works on collections
                                .build())
                        .build())
                .build();
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$['store']['book'][0]['price']")
                                .preOperation(SizePreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet(8.95))
                                .build())
                        .build())
                .build();

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);
        engine.add("test", c4);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.readTree(
                                "{\"store\":{\"book\":[{\"category\":\"reference\",\"basePrice\":8.95,\"inStock\":true},{\"category\":\"fiction\",\"basePrice\":22.99,\"inStock\":false}]}}"))
                        .build());
        assertThat(searchResults, hasSize(2));
        assertThat(searchResults, containsInAnyOrder("C1", "C3"));

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
                                .preOperation(SubtractionPreOperation.builder()
                                        .rhs(0.10000000001)
                                        .build())
                                .values(Sets.newHashSet(0.10000000001, 0.30000000003))
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
                                .preOperation(AdditionPreOperation.builder()
                                        .rhs(0.10000000001)
                                        .build())
                                .values(Sets.newHashSet(0.10000000001, 0.30000000003))
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
                                .values(Sets.newHashSet(0.20000000002))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 0.20000000002);
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
    public void testCNFSearchingMultipleMatchForPreOpChain() {
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
                                .preOperation(SubtractionPreOperation.builder()
                                        .rhs(0.10000000001)
                                        .build())
                                .values(Sets.newHashSet(0.10000000001, 0.30000000003))
                                .build())
                        .build())
                .build();
        final Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.d")
                                .preOperations(List.of(ToDateTimePreOperation.builder()
                                        .dateTimeFormat(DateTimeFormatter.ISO_OFFSET_DATE_TIME.toString())
                                        .build(),
                                        DateTimePreOperation.builder()
                                                .extract(DateExtractionType.YEAR)
                                                .build()))
                                .values(Sets.newHashSet(2025))
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
                                .values(Sets.newHashSet(0.20000000002))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 0.20000000002);
        testQuery.put("d", "2025-11-28T10:44:39.472601+05:30");
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("testsearch", c1);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
                                .preOperation(SubtractionPreOperation.builder()
                                        .rhs(0.000000000000001)
                                        .build())
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
                                .preOperation(AdditionPreOperation.builder()
                                        .rhs(-4.000000000000003)
                                        .build())
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(DivisionPreOperation.builder()
                                        .rhs(7)
                                        .build())
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
                                .preOperation(SubtractionPreOperation.builder()
                                        .rhs(3)
                                        .build())
                                .values(Sets.newHashSet(4, 5, 6))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        engine.add("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(LengthPreOperation.builder()
                                        .build())
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(LengthPreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "C"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .preOperation(LengthPreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet(1))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        engine.add("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, containsInAnyOrder("C2"));

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
                                .preOperation(DivisionPreOperation.builder()
                                        .rhs(0)
                                        .build())
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
                                .preOperation(SubStringPreOperation.builder()
                                        .beginIndex(0)
                                        .build())
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // Assertion
        assertThat(searchResults, hasSize(1));
        assertThat(searchResults, containsInAnyOrder("C5"));

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
                                .preOperation(AdditionPreOperation.builder()
                                        .rhs(1)
                                        .build())
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
                                .preOperation(SubStringPreOperation.builder()
                                        .beginIndex(1)
                                        .build())
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
            searchResults = engine.search("test",
                    RequestContext.builder()
                            .node(mapper.valueToTree(testQuery))
                            .build());
        } catch (MustangException e) {
            Assert.assertEquals("Error code message is not matching",
                    "INDEX_NOT_FOUND",
                    e.getErrorCode()
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
                                .preOperation(MultiplicationPreOperation.builder()
                                        .rhs(0)
                                        .build())
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
                                .preOperation(SizePreOperation.builder()
                                        .build())
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
                                .preOperation(ModuloPreOperation.builder()
                                        .rhs(10)
                                        .build())
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
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
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
                                .preOperation(MultiplicationPreOperation.builder()
                                        .rhs(1)
                                        .build())
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .preOperation(ModuloPreOperation.builder()
                                        .rhs(10)
                                        .build())
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
                                .preOperation(MultiplicationPreOperation.builder()
                                        .rhs(0)
                                        .build())
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
            Assert.assertEquals(ErrorCode.INDEX_GENERATION_ERROR, e.getErrorCode());
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
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
        final Set<String> searchResults1 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        // Search query - with older value
        final Set<String> searchResults2 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        assertThat(searchResults2, is(empty()));

        engine.ratify("test");
        final RatificationResult ratificationResult = engine.getRatificationResult("test");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));
    }

    @Test
    public void testDateExtractsImpl() {

        final Date now = new Date();

        ZonedDateTime zdt = ZonedDateTime.parse("2025-11-28T14:12:39.473702+05:30",
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(zdt.toEpochSecond() * 1000L);
        DateExtractionImpl impl = DateExtractionImpl.builder()
                .instance(instance)
                .build();

        assertThat(impl.visitEra(), is("AD"));
        assertThat(impl.visitYear(), is(2025));
        assertThat(impl.visitMonth(), is(11));
        assertThat(impl.visitDayOfMonth(), is(28));
        assertThat(impl.visitDayOfWeek(), is(6));
        assertThat(impl.visitDayOfWeekInMonth(), is(4));
        assertThat(impl.visitDayOfYear(), is(332));
        assertThat(impl.visitDate(), is(28));
        assertThat(impl.visitHour(), is(2));
        assertThat(impl.visitHourOfDay(), is(14));
        assertThat(impl.visitMinute(), is(12));
        assertThat(impl.visitSecond(), is(39));
        assertThat(impl.visitAmPm(), is("PM"));
        assertThat(Long.valueOf(impl.visitDiffWithEpoch()
                .toString()) >= now.getTime() - zdt.toEpochSecond() * 1000L, is(true));

        zdt = ZonedDateTime.parse("2025-11-28T10:44:00+05:30", DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        instance = Calendar.getInstance();
        instance.setTimeInMillis(zdt.toEpochSecond() * 1000L);
        impl = DateExtractionImpl.builder()
                .instance(instance)
                .build();

        assertThat(impl.visitEra(), is("AD"));
        assertThat(impl.visitYear(), is(2025));
        assertThat(impl.visitMonth(), is(11));
        assertThat(impl.visitDayOfMonth(), is(28));
        assertThat(impl.visitDayOfWeek(), is(6));
        assertThat(impl.visitDayOfWeekInMonth(), is(4));
        assertThat(impl.visitDayOfYear(), is(332));
        assertThat(impl.visitDate(), is(28));
        assertThat(impl.visitHour(), is(10));
        assertThat(impl.visitHourOfDay(), is(10));
        assertThat(impl.visitMinute(), is(44));
        assertThat(impl.visitSecond(), is(0));
        assertThat(impl.visitAmPm(), is("AM"));
        assertThat(Long.valueOf(impl.visitDiffWithEpoch()
                .toString()) >= now.getTime() - zdt.toEpochSecond() * 1000L, is(true));
    }

}
