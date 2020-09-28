package com.phonepe.growth.mustang.search;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class SearchTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDNFPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n")
                        .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003)).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.p").values(Sets.newHashSet(true)).build()).build())
                .build();
        Criteria c2 = DNFCriteria.builder().id("C2").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2", "A3")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet("4", "5", "6")).build())
                .build()).build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        final MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
        engine.index("test", c1);
        engine.index("test", c2);
        final List<String> searchResults = engine.search("test",
                EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build());
        Assert.assertTrue(searchResults.contains("C1"));
    }

    @Test
    public void testDNFNegativeMatch() throws Exception {

        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A", "B")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet(1, 2, 3)).build()).build())
                .build();
        Criteria c2 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("B", "C")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet(4, 5, 6)).build()).build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        final MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
        engine.index("test", Lists.asList(c1, new Criteria[] { c2 }));
        final List<String> searchResults = engine.search("test",
                EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build());
        Assert.assertTrue(searchResults.isEmpty());

    }

    @Test
    public void testDnfSearchingMultipleMatch(){
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet(0.1000000000001,0.20000000000002,0.300000000003)).build())
                .build()).build();
        Criteria c2 = DNFCriteria.builder().id("C2").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2", "A3")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet("4", "5", "6", 0.300000000003)).build())
                .build()).build();

        Criteria c3 = DNFCriteria.builder().id("C2").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2", "A3")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.p").values(Sets.newHashSet("P1", "P2", "P3")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet("4", "5", "6", 0.300000000003)).build())
                .build()).build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("n", 0.300000000003);
        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
        engine.index("testsearch", c1);
        engine.index("testsearch", c2);
        engine.index("testsearch", c3);
        final List<String> searchResults = engine.search("testsearch",
                EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build());
        System.out.println(searchResults);
        /* Assertions for multiple matches */
        Assert.assertEquals(2, searchResults.size());
        Assert.assertTrue(searchResults.contains("C1"));
        Assert.assertTrue(searchResults.contains("C2"));
    }
}
