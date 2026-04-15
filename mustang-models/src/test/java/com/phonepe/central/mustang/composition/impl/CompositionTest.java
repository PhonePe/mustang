package com.phonepe.central.mustang.composition.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.central.mustang.common.RequestContext;
import com.phonepe.central.mustang.composition.CompositionType;
import com.phonepe.central.mustang.debug.CompositionDebugResult;
import com.phonepe.central.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.central.mustang.predicate.impl.IncludedPredicate;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CompositionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private RequestContext buildContext(Map<String, Object> data) {
        JsonNode node = mapper.valueToTree(data);
        return RequestContext.builder().node(node).build();
    }

    private IncludedPredicate includedPredicate(String lhs, Object... vals) {
        Set<Object> values = new HashSet<>(Arrays.asList(vals));
        return IncludedPredicate.builder().lhs(lhs).values(values).build();
    }

    private ExcludedPredicate excludedPredicate(String lhs, Object... vals) {
        Set<Object> values = new HashSet<>(Arrays.asList(vals));
        return ExcludedPredicate.builder().lhs(lhs).values(values).build();
    }

    // --- Conjunction ---
    @Test
    public void testConjunctionAllMatch() {
        Conjunction conj = Conjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(includedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B1");
        Assert.assertTrue(conj.evaluate(buildContext(data)));
    }

    @Test
    public void testConjunctionPartialMatch() {
        Conjunction conj = Conjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(includedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertFalse(conj.evaluate(buildContext(data)));
    }

    @Test
    public void testConjunctionWithExcluded() {
        Conjunction conj = Conjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(excludedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertTrue(conj.evaluate(buildContext(data)));
    }

    @Test
    public void testConjunctionType() {
        Conjunction conj = Conjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .build();
        Assert.assertEquals(CompositionType.AND, conj.getType());
    }

    @Test
    public void testConjunctionDebug() {
        Conjunction conj = Conjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        CompositionDebugResult result = conj.debug(buildContext(data));
        Assert.assertTrue(result.isResult());
        Assert.assertEquals(CompositionType.AND, result.getType());
        Assert.assertEquals(1, result.getPredicateDebugResults().size());
    }

    @Test
    public void testConjunctionScoreAllMatch() {
        Conjunction conj = Conjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(includedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B1");
        // default weight 1 per predicate, sum = 2
        Assert.assertEquals(2.0, conj.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testConjunctionScorePartialMatch() {
        Conjunction conj = Conjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(includedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertEquals(-1.0, conj.getScore(buildContext(data)), 0.001);
    }

    // --- Disjunction ---
    @Test
    public void testDisjunctionAnyMatch() {
        Disjunction disj = Disjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(includedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertTrue(disj.evaluate(buildContext(data)));
    }

    @Test
    public void testDisjunctionNoneMatch() {
        Disjunction disj = Disjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(includedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A2");
        data.put("b", "B2");
        Assert.assertFalse(disj.evaluate(buildContext(data)));
    }

    @Test
    public void testDisjunctionType() {
        Disjunction disj = Disjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .build();
        Assert.assertEquals(CompositionType.OR, disj.getType());
    }

    @Test
    public void testDisjunctionDebug() {
        Disjunction disj = Disjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        CompositionDebugResult result = disj.debug(buildContext(data));
        Assert.assertTrue(result.isResult());
        Assert.assertEquals(CompositionType.OR, result.getType());
    }

    @Test
    public void testDisjunctionScore() {
        Disjunction disj = Disjunction.builder()
                .predicate(includedPredicate("$.a", "A1"))
                .predicate(includedPredicate("$.b", "B1"))
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        // max of scores: 1 (match) and -1 (no match) = 1
        Assert.assertEquals(1.0, disj.getScore(buildContext(data)), 0.001);
    }
}

