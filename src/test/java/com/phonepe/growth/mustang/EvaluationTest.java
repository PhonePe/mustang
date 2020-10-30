package com.phonepe.growth.mustang;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.EvaluationContext;
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
        engine = MustangEngine.builder().mapper(mapper).build();
    }

    @Test
    public void testDNFPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n")
                        .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhs("$.p").values(Sets.newHashSet(true)).build()).build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        Assert.assertTrue(engine.evaluate(c1, EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build()));
    }

    @Test
    public void testDNFNegativeMatch() throws Exception {

        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A", "B")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet(1, 2, 3)).build()).build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        Assert.assertFalse(
                engine.evaluate(c1, EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build()));

    }

    @Test
    public void testCNFPositiveMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder().id("C1").disjunction(Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n")
                        .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhs("$.p").values(Sets.newHashSet(true)).build()).build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A4");
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", false);

        Assert.assertTrue(engine.evaluate(c1, EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build()));
    }

    @Test
    public void testCNFNegativeMatch() throws Exception {

        Criteria c1 = CNFCriteria.builder().id("C1").disjunction(Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A", "B")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet(1, 2, 3)).build()).build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "C");
        testQuery.put("n", "7");

        Assert.assertFalse(
                engine.evaluate(c1, EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build()));

    }

}