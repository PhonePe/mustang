package com.phonepe.growth;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class SanityTest {

    @Test
    public void testPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet(0.1000000000001,0.20000000000002,0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.p").values(Sets.newHashSet(true)).build())
                .build()).build();
        Criteria c2 = DNFCriteria.builder().id("C2").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2", "A3")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet("4", "5", "6")).build())
                .build()).build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
        engine.index("test", c1);
        engine.index("test", c2);
        final List<String> searchResults = engine.search("test",
                EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build());
        System.out.println(searchResults);
        Assert.assertTrue(searchResults.contains("C1"));
    }

    @Test
    public void testNegativeMatch() throws Exception {

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

        final ObjectMapper mapper = new ObjectMapper();
        final MustangEngine engine = MustangEngine.builder().mapper(mapper).build();
        engine.index("test", c1);
        engine.index("test", c2);
        final List<String> searchResults = engine.search("test",
                EvaluationContext.builder().node(mapper.valueToTree(testQuery)).build());
        System.out.println(searchResults);
        Assert.assertTrue(searchResults.isEmpty());

    }

}
