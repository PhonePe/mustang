package com.phonepe.growth.mustang.search;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.criteria.tautology.TautologicalCriteria;
import com.phonepe.growth.mustang.exception.MustangException;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class SearchTest {

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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.contains("C1"));
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

        engine.index("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());

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
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        /* Assertions for multiple matches */
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
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
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        /* Assertions for multiple matches */
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 2);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());

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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        /* Assertions for multiple matches */
        Assert.assertEquals(3, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
        Assert.assertTrue(searchResults.contains("C3"));
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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 2);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
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

        engine.index("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());

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

        engine.index("test", c1);
        engine.index("test", c2);
        engine.index("test", c3);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 2);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
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

        engine.index("test", Lists.asList(c1, new Criteria[] { c2 }));
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());

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
        engine.index("test", c1);
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
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
        engine.index("testsearch", c4);
        engine.index("testsearch", c5);
        engine.index("testsearch", c6);

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
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C4"));
        Assert.assertTrue(searchResults.contains("C5"));
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
        engine.index("testsearch", c4);
        engine.index("testsearch", c5);
        engine.index("testsearch", c6);

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
        Assert.assertEquals(3, searchResults.size());
        Assert.assertTrue(searchResults.contains("C3"));
        Assert.assertTrue(searchResults.contains("C4"));
        Assert.assertTrue(searchResults.contains("C5"));
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
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
        Assert.assertEquals(3, searchResults.size());
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
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
        Assert.assertEquals(3, searchResults.size());
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
        Assert.assertTrue(searchResults.isEmpty());
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");// Negative value
        testQuery.put("n", 0.300000000003);// Postive value
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // Assertion
        Assert.assertEquals(0, searchResults.size());
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
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
        Assert.assertEquals(0, searchResults.size());
    }

    @Test // TODO -issue reported
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
        engine.index("test", c1);
        engine.index("test", c2);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 6); // Same value as criteria but as Integer
        // Search query
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search query
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
    }

    @Test // TODO -issue reported
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        // Search query
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // issue
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        // Search Engine call
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

        /* Updated Criteria Builder -set value as FALSE */
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
        engine.index("test", c1);
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
        Assert.assertEquals(1, searchResults2.size());
        Assert.assertTrue(searchResults2.contains("C1"));
    }

    @Test
    public void testDNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);
        testQuery.put("a", "A1");
        // Search Engine call
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

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
        engine.index("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        testQuery.put("a", "B1");
        // Search query with new values
        final Set<String> searchResults1 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        testQuery.put("a", "A1");
        // Search query with new values
        final Set<String> searchResults2 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults2.size());
        Assert.assertTrue(searchResults2.contains("C1"));
    }

    @Test // TODO -issue reported
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        // Search Engine call
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

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
        engine.index("test", c2);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        // Search query - with new value
        final Set<String> searchResults1 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C2"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        // Search query - with older value
        final Set<String> searchResults2 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults2.size());
        Assert.assertTrue(searchResults2.contains("C1"));
    }

    @Test // TODO -issue reported
    public void testDNFMultipleExclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

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
        engine.index("test", c2);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        testQuery.put("a", "B10");
        // Search query with new values
        final Set<String> searchResults1 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C2"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults2 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults2.size());
        Assert.assertTrue(searchResults2.contains("C1"));
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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
    }

    @Test // TODO -issue reported
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
        engine.index("test", c1);
        engine.index("test", c2);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", 6); // Same value as criteria but as Integer
        // Search query
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
    }

    @Test // TODO -issue reported
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
        engine.index("test", c1);
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

        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search query
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
    }

    @Test
    public void testCNFMultipleInclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", true);

        // Search Engine call
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
        engine.index("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);

        // Search query with new values
        final Set<String> searchResults1 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);

        // Search query with new values
        final Set<String> searchResults2 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults2.size());
        Assert.assertTrue(searchResults2.contains("C1"));
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        // Search Engine call
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

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
        engine.index("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        // Search query - with new value
        final Set<String> searchResults1 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        // Search query - with older value
        final Set<String> searchResults2 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults2.isEmpty());
    }

    @Test
    public void testCNFMultipleExclusionPredicateQueryEngineAndUpdateSameCriteriaWithSameNumberOfInclusionPredicateQueryAgain() {
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
        engine.index("test", c1);
        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
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
        engine.index("test", c1);
        /*** validate with older value and new value ***/
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", true);
        testQuery.put("a", "B10");
        // Search query with new values
        final Set<String> searchResults1 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults1.size());
        Assert.assertTrue(searchResults1.contains("C1"));
        // Request Map - updation
        testQuery.clear();
        testQuery.put("p", false);
        testQuery.put("a", "A10");
        // Search Engine call
        final Set<String> searchResults2 = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertEquals(1, searchResults2.size());
        Assert.assertTrue(searchResults2.contains("C1"));
    }

    @Test // TODO -issue reported
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
        engine.index("test", c1);
        // Query engine
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
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
        engine.index("test", c1);
        // Query engine
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
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
        engine.index("test", c1);
        // Query engine
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 0);
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1,C2 value
        testQuery.put("b", "B1"); // C1 value
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // Assertion
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1 value
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // Assertion
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C2"));

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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1 value
        testQuery.put("b", "B1"); // C1 value
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // Assertion
        Assert.assertEquals(1, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));

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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);

        // Search query -values not present in both the criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        testQuery.put("b", "B10");
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // Assertion
        Assert.assertEquals(0, searchResults.size());

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
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);

        // Search query for same criteria
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1"); // C1,C2 value
        testQuery.put("b", "B1"); // C1 value
        testQuery.put("n", 4); // C2 value
        final Set<String> searchResults = engine.search("testsearch",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        // Assertion
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
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

        engine.index("test", c1);
        Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());
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

        engine.index("test", c1);
        Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
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

        engine.index("test", c1);
        Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());
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

        engine.index("test", c1);
        Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
    }

    @Test(expected = MustangException.class)
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

        engine.index("test", c1);
        Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
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
        engine.index("test1", c1);
        engine.replace("test", "test1");
        searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));

        searchResults = engine.search("test1",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.fail("Should have thrown an exception");
    }

    @Test(expected = MustangException.class)
    public void testOnEmptyIndex() throws Exception {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.fail("Should have thrown an exception");
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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.readTree(str))
                        .build());
        Assert.assertTrue(searchResults.contains("C1"));
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
                "{\"form\":\"DNF\",\"id\":\"C1\",\"conjunctions\":[{\"type\":\"AND\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.a\",\"values\":[\"A1\",\"A2\"],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false},{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"values\":[\"B2\",\"B1\"],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"values\":[1.0E-15,2.0E-15,3.0E-15],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false},{\"type\":\"INCLUDED\",\"lhs\":\"$.p\",\"values\":[true],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false}]}]}",
                mapper.writeValueAsString(c1));
        Criteria c11 = mapper.readValue(mapper.writeValueAsString(c1), Criteria.class);
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.index("test", c11);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.contains("C1"));
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
                "{\"form\":\"CNF\",\"id\":\"C1\",\"disjunctions\":[{\"type\":\"OR\",\"predicates\":[{\"type\":\"INCLUDED\",\"lhs\":\"$.a\",\"values\":[\"A1\",\"A2\"],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false},{\"type\":\"EXCLUDED\",\"lhs\":\"$.b\",\"values\":[\"B2\",\"B1\"],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false},{\"type\":\"INCLUDED\",\"lhs\":\"$.n\",\"values\":[1.0E-15,2.0E-15,3.0E-15],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false},{\"type\":\"INCLUDED\",\"lhs\":\"$.p\",\"values\":[true],\"lhsNotAPath\":false,\"weight\":0,\"defaultResult\":false}]}]}",
                mapper.writeValueAsString(c1));
        Criteria c11 = mapper.readValue(mapper.writeValueAsString(c1), Criteria.class);

        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// positive value
        testQuery.put("b", "B3"); // negative value

        engine.index("test", c11);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
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

        engine.index("test", c1);
        engine.index("test", c2);
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
    }

    @Test(expected = MustangException.class)
    public void testExceptionDuringQueryNormalisation() throws Exception {
        final ObjectMapper mapperSpy = spy(mapper);
        doThrow(IOException.class).when(mapperSpy)
                .readValue(Matchers.<byte[]>any(), Matchers.<TypeReference<Map<String, Object>>>any());
        MustangEngine engine = MustangEngine.builder()
                .mapper(mapperSpy)
                .build();
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");// positive value
        testQuery.put("b", "B3"); // negative value

        engine.index("test", c1);
        engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.fail();
    }

}
