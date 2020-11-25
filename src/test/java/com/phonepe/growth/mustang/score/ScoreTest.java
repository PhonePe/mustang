package com.phonepe.growth.mustang.score;

import java.util.Map;
import java.util.Set;

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
                                .weight(10)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(10)
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .weight(10)
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
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(engine.score(c2, context) == -1);
        Assert.assertTrue(engine.score(c1, context) == 30);
    }

    @Test
    public void testDNFNegativeMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10)
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .weight(10)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        engine.index("test", Lists.asList(c1, new Criteria[] { c2 }));
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.isEmpty());
        Assert.assertTrue(engine.score(c1, context) == -1);
        Assert.assertTrue(engine.score(c2, context) == -1);
    }

    @Test
    public void testCNFPositiveMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .weight(10)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(10)
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .weight(10)
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
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(engine.score(c1, context) == 10);
        Assert.assertTrue(engine.score(c2, context) == -1);
    }

    @Test
    public void testCNFNegativeMatch() throws Exception {

        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10)
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("B", "C"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .weight(10)
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "P");
        testQuery.put("n", 8);

        engine.index("test", c1);
        engine.index("test", c2);
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.isEmpty());
        Assert.assertTrue(engine.score(c1, context) == -1);
        Assert.assertTrue(engine.score(c2, context) == -1);

    }

    @Test
    public void testMultiMixedPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .weight(10)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .weight(10)
                                .build())
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A3", "A4"))
                                .weight(10)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3", "B4"))
                                .weight(10)
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
                                .weight(10)
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(false))
                                .weight(10)
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10)
                                .build())
                        .build())
                .build();
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .weight(10)
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A7", "A5", "A6"))
                                .weight(10)
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(4, 5, 6))
                                .weight(10)
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
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();

        Set<String> searchResults = engine.search("test", context);
        Assert.assertTrue(searchResults.size() == 2);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));

        searchResults = engine.search("test", context, 1);
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(engine.score(c1, context) == 30);
        Assert.assertTrue(engine.score(c2, context) == 20);
        Assert.assertTrue(engine.score(c3, context) == -1);

        searchResults = engine.search("test", context, 3);
        Assert.assertTrue(searchResults.size() == 2);
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
    }

}
