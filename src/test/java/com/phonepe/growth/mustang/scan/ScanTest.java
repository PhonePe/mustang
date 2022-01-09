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
package com.phonepe.growth.mustang.scan;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class ScanTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;

    @Before
    public void setUp() {
        engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
    }

    @Test
    public void testDNFPositiveMatch() {
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
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testDNFNegativeMatch() {

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

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.isEmpty());

    }

    @Test
    public void testDNFMultiMatch() {
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
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.300000000003))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.size() == 2);
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testCNFPositiveMatch() {
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
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testCNFNegativeMatch() {

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
        testQuery.put("a", "D");
        testQuery.put("n", "7");

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.isEmpty());

    }

    @Test
    public void testCNFMultiMatch() {
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
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.300000000003))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.size() == 2);
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testDNFSingleCriteriaIncludePredicate() {
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
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testDNFSingleCriteriaIncludeAndExcludePredicate() {
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
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertFalse(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testDNFSingleCriteriaExcludePredicateAndQueryWithNonExclusionValues() {
        Criteria c1 = DNFCriteria.builder()
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

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        testQuery.put("p", Boolean.FALSE);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testDNFSingleCriteriaExcludePredicateAndQueryWithExclusionValues() {
        Criteria c1 = DNFCriteria.builder()
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

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("p", Boolean.TRUE);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertFalse(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testDNFMultipleCriteriaIncludePredicate() {
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

        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.abc")
                                .values(Sets.newHashSet("ABC1", "ABC2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.boolTest")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);
        testQuery.put("abc", "ABC1");
        testQuery.put("boolTest", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testDNFMultipleCriteriaExcludePredicate() {
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
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.abc")
                                .values(Sets.newHashSet("ABC1", "ABC2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.boolTest")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        testQuery.put("b", "B10");
        testQuery.put("n", 0.400000000003);
        testQuery.put("p", false);
        testQuery.put("abc", "ABC10");
        testQuery.put("boolTest", false);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testDNFMultipleCriteriaExcludePredicateAndQueryWithNonExclusionValues() {
        Criteria c1 = DNFCriteria.builder()
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

        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.helloWorld")
                                .values(Sets.newHashSet("helloWorld1", "helloWorld2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.statusCodes")
                                .values(Sets.newHashSet("200", "400", "500"))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("helloWorld", "helloWorld100");
        testQuery.put("statusCodes", "404");
        testQuery.put("a", "A10");
        testQuery.put("p", false);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testDNFMultipleCriteriaExcludePredicateAndQueryWithExclusionValues() {
        Criteria c1 = DNFCriteria.builder()
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

        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.helloWorld")
                                .values(Sets.newHashSet("helloWorld1", "helloWorld2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.statusCodes")
                                .values(Sets.newHashSet("200", "400", "500"))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("helloWorld", "helloWorld1");
        testQuery.put("statusCodes", "400");
        testQuery.put("a", "A1");
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertFalse(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertFalse(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testCNFSingleCriteriaIncludePredicate() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
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

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testCNFSingleCriteriaIncludeAndExcludePredicate() {
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
        testQuery.put("a", "A1");
        testQuery.put("b", "B10");

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testCNFSingleCriteriaExcludePredicateAndQueryWithNonExclusionValues() {
        Criteria c1 = CNFCriteria.builder()
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

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testCNFSingleCriteriaExcludePredicateAndQueryWithExclusionValues() {
        Criteria c1 = CNFCriteria.builder()
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

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("p", Boolean.TRUE);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] {}),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertFalse(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
    }

    @Test
    public void testCNFMultipleCriteriaIncludePredicate() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
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

        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.abc")
                                .values(Sets.newHashSet("ABC1", "ABC2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.boolTest")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("abc", "ABC1");

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testCNFMultipleCriteriaExcludePredicate() {
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
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.abc")
                                .values(Sets.newHashSet("ABC1", "ABC2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.boolTest")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        testQuery.put("abc", "ABC10");
        testQuery.put("boolTest", false);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testCNFMultipleCriteriaExcludePredicateAndQueryWithNonExclusionValues() {
        Criteria c1 = CNFCriteria.builder()
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

        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.helloWorld")
                                .values(Sets.newHashSet("helloWorld1", "helloWorld2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.statusCodes")
                                .values(Sets.newHashSet("200", "400", "500"))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("helloWorld", "helloWorld100");
        testQuery.put("a", "A10");

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertTrue(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

    @Test
    public void testCNFMultipleCriteriaExcludePredicateAndQueryWithExclusionValues() {
        Criteria c1 = CNFCriteria.builder()
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

        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.helloWorld")
                                .values(Sets.newHashSet("helloWorld1", "helloWorld2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.statusCodes")
                                .values(Sets.newHashSet("200", "400", "500"))
                                .build())
                        .build())
                .build();

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("helloWorld", "helloWorld1");
        testQuery.put("statusCodes", "400");
        testQuery.put("a", "A1");
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertFalse(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C1")));
        Assert.assertFalse(scan.stream()
                .anyMatch(criteria -> criteria.getId()
                        .equals("C2")));
    }

}
