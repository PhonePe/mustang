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
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class ScanTest {

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
        Criteria c2 = DNFCriteria.builder().id("C2").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2", "A3")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet("4", "5", "6")).build())
                .build()).build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build());
        Assert.assertTrue(scan.stream().filter(criteria -> criteria.getId().equals("C1")).findFirst().isPresent());
    }

    @Test
    public void testDNFNegativeMatch() throws Exception {

        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A", "B")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet(1, 2, 3)).build()).build())
                .build();
        Criteria c2 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("B", "C")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet(4, 5, 6)).build()).build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", "7");

        final List<Criteria> scan = engine.scan(Lists.asList(c1, new Criteria[] { c2 }),
                EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build());
        Assert.assertTrue(scan.isEmpty());

    }

}
