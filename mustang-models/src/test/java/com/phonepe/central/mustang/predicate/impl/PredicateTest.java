package com.phonepe.central.mustang.predicate.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.central.mustang.common.RequestContext;
import com.phonepe.central.mustang.debug.PredicateDebugResult;
import com.phonepe.central.mustang.detail.Caveat;
import com.phonepe.central.mustang.detail.impl.ExistenceDetail;
import com.phonepe.central.mustang.detail.impl.RangeDetail;
import com.phonepe.central.mustang.detail.impl.RegexDetail;
import com.phonepe.central.mustang.predicate.PredicateType;
import com.phonepe.central.mustang.predicate.PredicateVisitor;
import com.phonepe.central.mustang.preoperation.impl.AdditionPreOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PredicateTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private RequestContext buildContext(Map<String, Object> data) {
        JsonNode node = mapper.valueToTree(data);
        return RequestContext.builder().node(node).build();
    }

    // --- IncludedPredicate ---
    @Test
    public void testIncludedPredicateWithValuesMatch() {
        Set<Object> values = new HashSet<>();
        values.add("A1");
        values.add("A2");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .values(values)
                .build();
        Assert.assertEquals(PredicateType.INCLUDED, predicate.getType());
        Assert.assertEquals(Caveat.EQUALITY, predicate.getDetail().getCaveat());
        Assert.assertTrue(predicate.evaluate("A1"));
    }

    @Test
    public void testIncludedPredicateWithValuesNoMatch() {
        Set<Object> values = new HashSet<>();
        values.add("A1");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .values(values)
                .build();
        Assert.assertFalse(predicate.evaluate("B1"));
    }

    @Test
    public void testIncludedPredicateWithExplicitDetail() {
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .detail(RegexDetail.builder().regex("A.*").build())
                .build();
        Assert.assertEquals(Caveat.REGEX, predicate.getDetail().getCaveat());
        Assert.assertTrue(predicate.evaluate("Apple"));
        Assert.assertFalse(predicate.evaluate("Banana"));
    }

    @Test
    public void testIncludedPredicateWithRangeDetail() {
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.n")
                .detail(RangeDetail.builder()
                        .lowerBound(1)
                        .upperBound(10)
                        .includeLowerBound(true)
                        .includeUpperBound(true)
                        .build())
                .build();
        Assert.assertTrue(predicate.evaluate(5));
        Assert.assertFalse(predicate.evaluate(11));
    }

    @Test
    public void testIncludedPredicateEvaluateWithContext() {
        Set<Object> values = new HashSet<>();
        values.add("A1");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .values(values)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        Assert.assertTrue(predicate.evaluate(buildContext(data)));
    }

    @Test
    public void testIncludedPredicateDebug() {
        Set<Object> values = new HashSet<>();
        values.add("A1");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .values(values)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        PredicateDebugResult debugResult = predicate.debug(buildContext(data));
        Assert.assertTrue(debugResult.isResult());
        Assert.assertEquals(PredicateType.INCLUDED, debugResult.getType());
        Assert.assertEquals("$.a", debugResult.getLhs());
    }

    @Test
    public void testIncludedPredicateScoreMatch() {
        Set<Object> values = new HashSet<>();
        values.add("A1");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .weight(5L)
                .values(values)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        Assert.assertEquals(5L, predicate.getScore(buildContext(data)));
    }

    @Test
    public void testIncludedPredicateScoreNoMatch() {
        Set<Object> values = new HashSet<>();
        values.add("A1");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .weight(5L)
                .values(values)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "B1");
        Assert.assertEquals((long) -1.0, predicate.getScore(buildContext(data)));
    }

    @Test
    public void testIncludedPredicateDefaultWeight() {
        Set<Object> values = new HashSet<>();
        values.add("A1");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .values(values)
                .build();
        Assert.assertEquals(Long.valueOf(1L), predicate.getWeight());
    }

    @Test
    public void testIncludedPredicateWithPreOperation() {
        Set<Object> values = new HashSet<>();
        values.add(15.0);
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.n")
                .preOperation(AdditionPreOperation.builder().rhs(5).build())
                .values(values)
                .build();
        Assert.assertNotNull(predicate.getPreOperations());
        Assert.assertEquals(1, predicate.getPreOperations().size());
    }

    @Test
    public void testIncludedPredicateAcceptVisitor() {
        Set<Object> values = new HashSet<>();
        values.add("A");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .values(values)
                .build();
        String result = predicate.accept(new PredicateVisitor<>() {
            @Override
            public String visit(IncludedPredicate includedPredicate) {
                return "INCLUDED";
            }

            @Override
            public String visit(ExcludedPredicate excludedPredicate) {
                return "EXCLUDED";
            }
        });
        Assert.assertEquals("INCLUDED", result);
    }

    // --- ExcludedPredicate ---
    @Test
    public void testExcludedPredicateWithValuesMatch() {
        Set<Object> values = new HashSet<>();
        values.add("B1");
        values.add("B2");
        ExcludedPredicate predicate = ExcludedPredicate.builder()
                .lhs("$.b")
                .values(values)
                .build();
        Assert.assertEquals(PredicateType.EXCLUDED, predicate.getType());
        // excluded means NOT matching, so if value is "B1" it should return false
        Assert.assertFalse(predicate.evaluate("B1"));
    }

    @Test
    public void testExcludedPredicateWithValuesNoMatch() {
        Set<Object> values = new HashSet<>();
        values.add("B1");
        ExcludedPredicate predicate = ExcludedPredicate.builder()
                .lhs("$.b")
                .values(values)
                .build();
        // "B3" is not in excluded set, so predicate passes
        Assert.assertTrue(predicate.evaluate("B3"));
    }

    @Test
    public void testExcludedPredicateEvaluateWithContext() {
        Set<Object> values = new HashSet<>();
        values.add("B1");
        ExcludedPredicate predicate = ExcludedPredicate.builder()
                .lhs("$.b")
                .values(values)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("b", "B3");
        Assert.assertTrue(predicate.evaluate(buildContext(data)));
    }

    @Test
    public void testExcludedPredicateScoreMatch() {
        Set<Object> values = new HashSet<>();
        values.add("B1");
        ExcludedPredicate predicate = ExcludedPredicate.builder()
                .lhs("$.b")
                .values(values)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("b", "B3");
        // excluded predicate returns 0 on match
        Assert.assertEquals(0L, predicate.getScore(buildContext(data)));
    }

    @Test
    public void testExcludedPredicateScoreNoMatch() {
        Set<Object> values = new HashSet<>();
        values.add("B1");
        ExcludedPredicate predicate = ExcludedPredicate.builder()
                .lhs("$.b")
                .values(values)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("b", "B1");
        Assert.assertEquals((long) -1.0, predicate.getScore(buildContext(data)));
    }

    @Test
    public void testExcludedPredicateAcceptVisitor() {
        Set<Object> values = new HashSet<>();
        values.add("A");
        ExcludedPredicate predicate = ExcludedPredicate.builder()
                .lhs("$.a")
                .values(values)
                .build();
        String result = predicate.accept(new PredicateVisitor<>() {
            @Override
            public String visit(IncludedPredicate includedPredicate) {
                return "INCLUDED";
            }

            @Override
            public String visit(ExcludedPredicate excludedPredicate) {
                return "EXCLUDED";
            }
        });
        Assert.assertEquals("EXCLUDED", result);
    }

    // --- Predicate validation ---
    @Test
    public void testIsValidPredicateValidPath() {
        Set<Object> values = new HashSet<>();
        values.add("A");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a.b")
                .values(values)
                .build();
        Assert.assertTrue(predicate.isValidPredicate());
    }

    @Test
    public void testIsValidPredicateInvalidPath() {
        Set<Object> values = new HashSet<>();
        values.add("A");
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("A B")
                .values(values)
                .build();
        Assert.assertFalse(predicate.isValidPredicate());
    }

    @Test
    public void testIncludedPredicateWithExistenceDetail() {
        IncludedPredicate predicate = IncludedPredicate.builder()
                .lhs("$.a")
                .detail(ExistenceDetail.builder().build())
                .build();
        Assert.assertTrue(predicate.evaluate("anything"));
        Assert.assertFalse(predicate.evaluate((Object) null));
    }
}

