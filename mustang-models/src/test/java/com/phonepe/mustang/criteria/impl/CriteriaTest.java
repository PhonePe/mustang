/*
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonepe.mustang.criteria.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.CompositionType;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.composition.impl.Disjunction;
import com.phonepe.mustang.criteria.CriteriaForm;
import com.phonepe.mustang.criteria.CriteriaVisitor;
import com.phonepe.mustang.debug.DebugResult;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CriteriaTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private RequestContext buildContext(Map<String, Object> data) {
        JsonNode node = mapper.valueToTree(data);
        return RequestContext.builder().node(node).build();
    }

    private IncludedPredicate included(String lhs, Object... vals) {
        Set<Object> values = new HashSet<>(Arrays.asList(vals));
        return IncludedPredicate.builder().lhs(lhs).values(values).build();
    }

    // --- DNFCriteria ---
    @Test
    public void testDNFCriteriaEvaluateMatch() {
        DNFCriteria dnf = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .predicate(included("$.b", "B1"))
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(included("$.a", "A2"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A2");
        data.put("b", "B2");
        Assert.assertTrue(dnf.evaluate(buildContext(data)));
    }

    @Test
    public void testDNFCriteriaEvaluateNoMatch() {
        DNFCriteria dnf = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A2");
        Assert.assertFalse(dnf.evaluate(buildContext(data)));
    }

    @Test
    public void testDNFCriteriaForm() {
        DNFCriteria dnf = DNFCriteria.builder().id("C1")
                .conjunction(Conjunction.builder().predicate(included("$.a", "A1")).build())
                .build();
        Assert.assertEquals(CriteriaForm.DNF, dnf.getForm());
    }

    @Test
    public void testDNFCriteriaDebug() {
        DNFCriteria dnf = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        DebugResult result = dnf.debug(buildContext(data));
        Assert.assertTrue(result.isResult());
        Assert.assertEquals("C1", result.getId());
        Assert.assertEquals(CriteriaForm.DNF, result.getForm());
    }

    @Test
    public void testDNFCriteriaScore() {
        DNFCriteria dnf = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(included("$.b", "B1"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B1");
        // DNF score = max of conjunction scores = 1.0
        Assert.assertEquals(1.0, dnf.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testDNFCriteriaVisitor() {
        DNFCriteria dnf = DNFCriteria.builder().id("C1")
                .conjunction(Conjunction.builder().predicate(included("$.a", "A1")).build())
                .build();
        String result = dnf.accept(new CriteriaVisitor<>() {
            @Override public String visit(DNFCriteria dnf) { return "DNF"; }
            @Override public String visit(CNFCriteria cnf) { return "CNF"; }
            @Override public String visit(UNFCriteria unf) { return "UNF"; }
        });
        Assert.assertEquals("DNF", result);
    }

    // --- CNFCriteria ---
    @Test
    public void testCNFCriteriaEvaluateMatch() {
        CNFCriteria cnf = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(included("$.a", "A1", "A2"))
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(included("$.b", "B1", "B2"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertTrue(cnf.evaluate(buildContext(data)));
    }

    @Test
    public void testCNFCriteriaEvaluateNoMatch() {
        CNFCriteria cnf = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(included("$.b", "B1"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertFalse(cnf.evaluate(buildContext(data)));
    }

    @Test
    public void testCNFCriteriaForm() {
        CNFCriteria cnf = CNFCriteria.builder().id("C2")
                .disjunction(Disjunction.builder().predicate(included("$.a", "A1")).build())
                .build();
        Assert.assertEquals(CriteriaForm.CNF, cnf.getForm());
    }

    @Test
    public void testCNFCriteriaDebug() {
        CNFCriteria cnf = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        DebugResult result = cnf.debug(buildContext(data));
        Assert.assertTrue(result.isResult());
        Assert.assertEquals("C2", result.getId());
        Assert.assertEquals(CriteriaForm.CNF, result.getForm());
    }

    @Test
    public void testCNFCriteriaScoreAllMatch() {
        CNFCriteria cnf = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(included("$.b", "B1"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B1");
        // CNF score = sum of disjunction scores = 1 + 1 = 2
        Assert.assertEquals(2.0, cnf.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testCNFCriteriaScoreNoMatch() {
        CNFCriteria cnf = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(included("$.a", "A1"))
                        .build())
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A2");
        Assert.assertEquals(-1.0, cnf.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testCNFCriteriaVisitor() {
        CNFCriteria cnf = CNFCriteria.builder().id("C2")
                .disjunction(Disjunction.builder().predicate(included("$.a", "A1")).build())
                .build();
        String result = cnf.accept(new CriteriaVisitor<>() {
            @Override public String visit(DNFCriteria dnf) { return "DNF"; }
            @Override public String visit(CNFCriteria cnf) { return "CNF"; }
            @Override public String visit(UNFCriteria unf) { return "UNF"; }
        });
        Assert.assertEquals("CNF", result);
    }

    // --- UNFCriteria ---
    @Test
    public void testUNFCriteriaANDEvaluateMatch() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .predicate(included("$.b", "B1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B1");
        Assert.assertTrue(unf.evaluate(buildContext(data)));
    }

    @Test
    public void testUNFCriteriaANDEvaluateNoMatch() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .predicate(included("$.b", "B1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertFalse(unf.evaluate(buildContext(data)));
    }

    @Test
    public void testUNFCriteriaOREvaluateMatch() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.OR)
                .predicate(included("$.a", "A1"))
                .predicate(included("$.b", "B1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertTrue(unf.evaluate(buildContext(data)));
    }

    @Test
    public void testUNFCriteriaOREvaluateNoMatch() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.OR)
                .predicate(included("$.a", "A1"))
                .predicate(included("$.b", "B1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A2");
        data.put("b", "B2");
        Assert.assertFalse(unf.evaluate(buildContext(data)));
    }

    @Test
    public void testUNFCriteriaWithNestedCriteria() {
        UNFCriteria inner = UNFCriteria.builder()
                .id("INNER")
                .type(CompositionType.OR)
                .predicate(included("$.x", "X1"))
                .predicate(included("$.y", "Y1"))
                .build();

        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .criteria(inner)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("x", "X1");
        Assert.assertTrue(unf.evaluate(buildContext(data)));
    }

    @Test
    public void testUNFCriteriaForm() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .build();
        Assert.assertEquals(CriteriaForm.UNF, unf.getForm());
    }

    @Test
    public void testUNFCriteriaDebug() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        DebugResult result = unf.debug(buildContext(data));
        Assert.assertTrue(result.isResult());
        Assert.assertEquals("C3", result.getId());
    }

    @Test
    public void testUNFCriteriaANDScore() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .predicate(included("$.b", "B1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B1");
        Assert.assertEquals(2.0, unf.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testUNFCriteriaANDScoreNoMatch() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .predicate(included("$.b", "B1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        Assert.assertEquals(-1.0, unf.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testUNFCriteriaORScore() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.OR)
                .predicate(included("$.a", "A1"))
                .predicate(included("$.b", "B1"))
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("b", "B2");
        // max of 1 (match) and -1 (no match) = 1
        Assert.assertEquals(1.0, unf.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testUNFCriteriaVisitor() {
        UNFCriteria unf = UNFCriteria.builder()
                .id("C3").type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .build();
        String result = unf.accept(new CriteriaVisitor<>() {
            @Override public String visit(DNFCriteria dnf) { return "DNF"; }
            @Override public String visit(CNFCriteria cnf) { return "CNF"; }
            @Override public String visit(UNFCriteria unf) { return "UNF"; }
        });
        Assert.assertEquals("UNF", result);
    }

    @Test
    public void testUNFCriteriaANDScoreWithNestedCriteria() {
        UNFCriteria inner = UNFCriteria.builder()
                .id("INNER")
                .type(CompositionType.AND)
                .predicate(included("$.x", "X1"))
                .build();

        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .criteria(inner)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("x", "X1");
        Assert.assertEquals(2.0, unf.getScore(buildContext(data)), 0.001);
    }

    @Test
    public void testUNFCriteriaANDScoreWithNestedCriteriaNoMatch() {
        UNFCriteria inner = UNFCriteria.builder()
                .id("INNER")
                .type(CompositionType.AND)
                .predicate(included("$.x", "X1"))
                .build();

        UNFCriteria unf = UNFCriteria.builder()
                .id("C3")
                .type(CompositionType.AND)
                .predicate(included("$.a", "A1"))
                .criteria(inner)
                .build();

        Map<String, Object> data = new HashMap<>();
        data.put("a", "A1");
        data.put("x", "X2");
        Assert.assertEquals(-1.0, unf.getScore(buildContext(data)), 0.001);
    }
}

