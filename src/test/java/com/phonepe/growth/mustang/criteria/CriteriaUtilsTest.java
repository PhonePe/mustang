package com.phonepe.growth.mustang.criteria;

import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.UNFCriteria;
import com.phonepe.growth.mustang.criteria.tautology.DNFTautologicalCriteria;
import com.phonepe.growth.mustang.criteria.tautology.UNFTautologicalCriteria;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class CriteriaUtilsTest {

    private static final String CRITERIA_ID = "TEST_CRITERIA_ID";
    private static final String CRITERIA_ID2 = "TEST_CRITERIA_ID_2";
    private static final String CRITERIA_ID3 = "TEST_CRITERIA_ID_3";
    private static final String CRITERIA_ID4 = "TEST_CRITERIA_ID_4";

    private Predicate PREDICATE_A = IncludedPredicate.builder()
            .lhs("$.a")
            .values(Sets.newHashSet("A1", "A2"))
            .build();

    private Predicate PREDICATE_B = IncludedPredicate.builder()
            .lhs("$.b")
            .values(Sets.newHashSet("B1", "B2"))
            .build();

    private Predicate PREDICATE_C = IncludedPredicate.builder()
            .lhs("$.c")
            .values(Sets.newHashSet("C1", "C2"))
            .build();

    private Predicate PREDICATE_D = IncludedPredicate.builder()
            .lhs("$.d")
            .values(Sets.newHashSet("D1", "D2"))
            .build();

    private Predicate PREDICATE_E = IncludedPredicate.builder()
            .lhs("$.e")
            .values(Sets.newHashSet("E1", "E2"))
            .build();

    private Predicate PREDICATE_F = IncludedPredicate.builder()
            .lhs("$.f")
            .values(Sets.newHashSet("F1", "F2"))
            .build();

    private static Criteria orderCriteria(Criteria criteria) {
        return criteria.accept(new CriteriaVisitor<>() {
            @Override
            public Criteria visit(DNFCriteria dnf) {
                return orderDNF(dnf);
            }

            @Override
            public Criteria visit(CNFCriteria cnf) {
                return orderCNF(cnf);
            }

            @Override
            public Criteria visit(UNFCriteria unf) {
                return orderUNF(unf);
            }
        });
    }

    private static UNFCriteria orderUNF(UNFCriteria unf) {
        // Since order is not maintained in list
        return UNFCriteria.builder()
                .id(unf.getId())
                .type(unf.getType())
                .predicates(new HashSet<>(unf.getPredicates()))
                .criterias(new HashSet<>(unf.getCriterias()
                        .stream()
                        .map(CriteriaUtilsTest::orderCriteria)
                        .toList()))
                .build();
    }

    private static DNFCriteria orderDNF(DNFCriteria dnf) {
        // Since order is not maintained in list
        return DNFCriteria.builder()
                .id(dnf.getId())
                .conjunctions(new HashSet<>(dnf.getConjunctions()
                        .stream()
                        .map(conjunction -> Conjunction.builder()
                                .predicates(new HashSet<>(conjunction.getPredicates()))
                                .build())
                        .collect(Collectors.toSet())))
                .build();
    }

    private static CNFCriteria orderCNF(CNFCriteria cnf) {
        // Since order is not maintained in list
        return CNFCriteria.builder()
                .id(cnf.getId())
                .disjunctions(new HashSet<>(cnf.getDisjunctions()
                        .stream()
                        .map(disjunction -> Disjunction.builder()
                                .predicates(new HashSet<>(disjunction.getPredicates()))
                                .build())
                        .collect(Collectors.toSet())))
                .build();
    }

    @Test
    public void testCNFToGetDNFCriteria() {
        // (A OR B) AND (C OR D) AND E
        CNFCriteria cnfCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_C)
                        .predicate(PREDICATE_D)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // (A AND C AND E) OR (A AND D AND E) OR (B AND C AND E) OR (B AND D AND E)
        DNFCriteria expectedDNFCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_C)
                        .predicate(PREDICATE_E)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .predicate(PREDICATE_E)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        DNFCriteria actualDNFCriteria = CriteriaUtils.getDNFCriteria(cnfCriteria);
        Assert.assertEquals(orderCriteria(expectedDNFCriteria), orderCriteria(actualDNFCriteria));
        Assert.assertEquals(expectedDNFCriteria, CriteriaUtils.getDNFCriteria(expectedDNFCriteria));
    }

    @Test
    public void testUNFToGetDNFCriteria() {
        // ((A OR B) AND (C OR D)) OR E
        UNFCriteria unfCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID)
                .type(CompositionType.OR)
                .criteria(UNFCriteria.builder()
                        .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(0)))
                        .type(CompositionType.AND)
                        .criteria(UNFCriteria.builder()
                                .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(0), String.valueOf(0)))
                                .type(CompositionType.OR)
                                .predicate(PREDICATE_A)
                                .predicate(PREDICATE_B)
                                .build())
                        .criteria(UNFCriteria.builder()
                                .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(0), String.valueOf(1)))
                                .type(CompositionType.OR)
                                .predicate(PREDICATE_C)
                                .predicate(PREDICATE_D)
                                .build())
                        .build())
                .predicate(PREDICATE_E)
                .build();
        // (A AND C) OR (B AND C) OR (A AND D) OR (B AND D) OR E
        DNFCriteria expectedDNFCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_C)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_D)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_D)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        DNFCriteria actualDNFCriteria = CriteriaUtils.getDNFCriteria(unfCriteria);
        Assert.assertEquals(orderCriteria(expectedDNFCriteria), orderCriteria(actualDNFCriteria));
        Assert.assertEquals(expectedDNFCriteria, CriteriaUtils.getDNFCriteria(expectedDNFCriteria));

        // (A OR B)
        unfCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID2)
                .type(CompositionType.AND)
                .criteria(UNFCriteria.builder()
                        .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID2, String.valueOf(0)))
                        .type(CompositionType.OR)
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .build();
        // A OR B
        expectedDNFCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID2)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_B)
                        .build())
                .build();
        actualDNFCriteria = CriteriaUtils.getDNFCriteria(unfCriteria);
        Assert.assertEquals(orderCriteria(expectedDNFCriteria), orderCriteria(actualDNFCriteria));
        Assert.assertEquals(expectedDNFCriteria, CriteriaUtils.getDNFCriteria(expectedDNFCriteria));
    }

    @Test
    public void testGetDNFCriteriaNoMultiPredicate() {
        // A AND D AND E
        CNFCriteria cnfCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_D)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // (A AND D AND E)
        DNFCriteria expectedDNFCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        DNFCriteria actualDNFCriteria = CriteriaUtils.getDNFCriteria(cnfCriteria);
        Assert.assertEquals(expectedDNFCriteria, actualDNFCriteria);
    }

    @Test
    public void testDNFToGetCNFCriteria() {
        // (A AND B AND C) OR D OR E
        DNFCriteria dnfCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_D)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // (A OR D OR E) AND (B OR D OR E) AND (C OR D OR E)
        CNFCriteria expectedCNFCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_C)
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        CNFCriteria actualCNFCriteria = CriteriaUtils.getCNFCriteria(dnfCriteria);
        Assert.assertEquals(orderCriteria(expectedCNFCriteria), orderCriteria(actualCNFCriteria));
        Assert.assertEquals(expectedCNFCriteria, CriteriaUtils.getCNFCriteria(expectedCNFCriteria));
    }

    @Test
    public void testUNFToGetCNFCriteria() {
        // ((A AND B) OR C) AND E
        UNFCriteria unfCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID)
                .type(CompositionType.AND)
                .criteria(UNFCriteria.builder()
                        .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(0)))
                        .type(CompositionType.OR)
                        .criteria(UNFCriteria.builder()
                                .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(0), String.valueOf(0)))
                                .type(CompositionType.AND)
                                .predicate(PREDICATE_A)
                                .predicate(PREDICATE_B)
                                .build())
                        .predicate(PREDICATE_C)
                        .build())
                .predicate(PREDICATE_E)
                .build();
        // (A OR C) AND (B OR C) AND E
        CNFCriteria expectedCNFCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_C)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        CNFCriteria actualCNFCriteria = CriteriaUtils.getCNFCriteria(unfCriteria);
        Assert.assertEquals(orderCriteria(expectedCNFCriteria), orderCriteria(actualCNFCriteria));
        Assert.assertEquals(expectedCNFCriteria, CriteriaUtils.getCNFCriteria(expectedCNFCriteria));

        // (A OR B)
        unfCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID2)
                .type(CompositionType.AND)
                .criteria(UNFCriteria.builder()
                        .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID2, String.valueOf(0)))
                        .type(CompositionType.OR)
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .build();
        // (A OR B)
        expectedCNFCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID2)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .build();
        actualCNFCriteria = CriteriaUtils.getCNFCriteria(unfCriteria);
        Assert.assertEquals(orderCriteria(expectedCNFCriteria), orderCriteria(actualCNFCriteria));
        Assert.assertEquals(expectedCNFCriteria, CriteriaUtils.getCNFCriteria(expectedCNFCriteria));

    }

    @Test
    public void testGetCNFCriteriaNoMultiPredicate() {
        // A OR D OR E
        DNFCriteria dnfCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_D)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // (A OR D OR E)
        CNFCriteria expectedCNFCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        CNFCriteria actualCNFCriteria = CriteriaUtils.getCNFCriteria(dnfCriteria);
        Assert.assertEquals(expectedCNFCriteria, actualCNFCriteria);
    }

    @Test
    public void testCNFToGetUNFCriteria() {
        // 2 depth
        // (A OR B) AND (C OR D) AND E
        CNFCriteria cnfCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_C)
                        .predicate(PREDICATE_D)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // (A OR B) AND (C OR D) AND E
        UNFCriteria expectedUNFCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID)
                .type(CompositionType.AND)
                .criteria(UNFCriteria.builder()
                        .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(0)))
                        .type(CompositionType.OR)
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .criteria(UNFCriteria.builder()
                        .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(1)))
                        .type(CompositionType.OR)
                        .predicate(PREDICATE_C)
                        .predicate(PREDICATE_D)
                        .build())
                .predicate(PREDICATE_E)
                .build();
        UNFCriteria actualUNFCriteria = CriteriaUtils.getUNFCriteria(cnfCriteria);
        Assert.assertEquals(orderCriteria(expectedUNFCriteria), orderCriteria(actualUNFCriteria));
        Assert.assertEquals(expectedUNFCriteria, CriteriaUtils.getUNFCriteria(expectedUNFCriteria));
        // 1 depth
        // (A OR B)
        cnfCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID2)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .build();
        // (A OR B) AND (C OR D) AND E
        expectedUNFCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID2)
                .type(CompositionType.OR)
                .predicate(PREDICATE_A)
                .predicate(PREDICATE_B)
                .build();
        actualUNFCriteria = CriteriaUtils.getUNFCriteria(cnfCriteria);
        Assert.assertEquals(orderCriteria(expectedUNFCriteria), orderCriteria(actualUNFCriteria));
        Assert.assertEquals(expectedUNFCriteria, CriteriaUtils.getUNFCriteria(expectedUNFCriteria));

        // 0 depth
        cnfCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID3)
                .build();
        expectedUNFCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID3)
                .type(CompositionType.AND)
                .build();
        actualUNFCriteria = CriteriaUtils.getUNFCriteria(cnfCriteria);
        Assert.assertEquals(orderCriteria(expectedUNFCriteria), orderCriteria(actualUNFCriteria));
        Assert.assertEquals(expectedUNFCriteria, CriteriaUtils.getUNFCriteria(expectedUNFCriteria));
    }

    @Test
    public void testDNFToGetUNFCriteria() {
        // 2 depth
        // (A AND B AND C) OR D OR E
        DNFCriteria dnfCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_D)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // (A AND B AND C) OR D OR E
        UNFCriteria expectedUNFCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID)
                .type(CompositionType.OR)
                .criteria(UNFCriteria.builder()
                        .id(String.join(CriteriaUtils.UNF_CRITERIA_SEPARATOR, CRITERIA_ID, String.valueOf(0)))
                        .type(CompositionType.AND)
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .build())
                .predicate(PREDICATE_D)
                .predicate(PREDICATE_E)
                .build();
        UNFCriteria actualUNFCriteria = CriteriaUtils.getUNFCriteria(dnfCriteria);
        Assert.assertEquals(orderCriteria(expectedUNFCriteria), orderCriteria(actualUNFCriteria));
        Assert.assertEquals(expectedUNFCriteria, CriteriaUtils.getUNFCriteria(expectedUNFCriteria));
        // 1 depth
        // A AND B AND C
        dnfCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID2)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .build())
                .build();
        // A AND B AND C
        expectedUNFCriteria = UNFCriteria.builder()
                .id(CRITERIA_ID2)
                .type(CompositionType.AND)
                .predicate(PREDICATE_A)
                .predicate(PREDICATE_B)
                .predicate(PREDICATE_C)
                .build();
        actualUNFCriteria = CriteriaUtils.getUNFCriteria(dnfCriteria);
        Assert.assertEquals(orderCriteria(expectedUNFCriteria), orderCriteria(actualUNFCriteria));
        Assert.assertEquals(expectedUNFCriteria, CriteriaUtils.getUNFCriteria(expectedUNFCriteria));
        // 0 depth
        dnfCriteria = new DNFTautologicalCriteria(CRITERIA_ID3);
        expectedUNFCriteria = new UNFTautologicalCriteria(CRITERIA_ID3);
        actualUNFCriteria = CriteriaUtils.getUNFCriteria(dnfCriteria);
        Assert.assertEquals(orderCriteria(expectedUNFCriteria), orderCriteria(actualUNFCriteria));
        Assert.assertEquals(expectedUNFCriteria, CriteriaUtils.getUNFCriteria(expectedUNFCriteria));
    }

    @Test
    public void testMergeCriteriaORType() {
        // ((A AND B) OR C)
        DNFCriteria dnfCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_C)
                        .build())
                .build();
        // (D AND E)
        CNFCriteria cnfCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID2)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_D)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // ((A AND B) OR C OR (D AND E))
        DNFCriteria expectedCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID3)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_C)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_D)
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        Criteria actualCriteria = CriteriaUtils.mergeCriteria(CompositionType.OR, CRITERIA_ID3, dnfCriteria,
                cnfCriteria);
        Assert.assertEquals(expectedCriteria, actualCriteria);
    }

    @Test
    public void testMergeCriteriaNoCriteria() {
        DNFCriteria expectedCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID3)
                .build();
        Criteria actualCriteria = CriteriaUtils.mergeCriteria(CompositionType.OR, CRITERIA_ID3);
        Assert.assertEquals(expectedCriteria, actualCriteria);
    }

    @Test
    public void testMergeCriteriaSingleCriteria() {
        // ((A AND B) OR C)
        DNFCriteria dnfCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_C)
                        .build())
                .build();

        // ((A AND B) OR C)
        DNFCriteria expectedCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID3)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_C)
                        .build())
                .build();
        Criteria actualCriteria = CriteriaUtils.mergeCriteria(CompositionType.OR, CRITERIA_ID3, dnfCriteria);
        Assert.assertEquals(expectedCriteria, actualCriteria);
    }

    @Test
    public void testMergeCriteriaANDType() {
        // ((A AND B) OR C)
        DNFCriteria dnfCriteria = DNFCriteria.builder()
                .id(CRITERIA_ID)
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_B)
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(PREDICATE_C)
                        .build())
                .build();
        // (D AND E)
        CNFCriteria cnfCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID2)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_D)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        // ((A OR C) AND (B OR C) AND D AND E)
        CNFCriteria expectedCriteria = CNFCriteria.builder()
                .id(CRITERIA_ID3)
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_A)
                        .predicate(PREDICATE_C)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_B)
                        .predicate(PREDICATE_C)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_D)
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(PREDICATE_E)
                        .build())
                .build();
        Criteria actualCriteria = CriteriaUtils.mergeCriteria(CompositionType.AND, CRITERIA_ID3, dnfCriteria,
                cnfCriteria);
        Assert.assertEquals(orderCriteria(expectedCriteria), orderCriteria(actualCriteria));
    }

}
