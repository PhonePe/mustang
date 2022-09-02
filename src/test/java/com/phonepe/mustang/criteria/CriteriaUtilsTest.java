/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phonepe.mustang.criteria;

import com.google.common.collect.Sets;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.composition.impl.Disjunction;
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.predicate.Predicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import org.junit.Assert;
import org.junit.Test;

public class CriteriaUtilsTest {

    private static final String CRITERIA_ID = "TEST_CRITERIA_ID";

    private final Predicate PREDICATE_A = IncludedPredicate.builder()
            .lhs("$.a")
            .values(Sets.newHashSet("A1", "A2"))
            .build();

    private final Predicate PREDICATE_B = IncludedPredicate.builder()
            .lhs("$.b")
            .values(Sets.newHashSet("B1", "B2"))
            .build();

    private final Predicate PREDICATE_C = IncludedPredicate.builder()
            .lhs("$.c")
            .values(Sets.newHashSet("C1", "C2"))
            .build();

    private final Predicate PREDICATE_D = IncludedPredicate.builder()
            .lhs("$.d")
            .values(Sets.newHashSet("D1", "D2"))
            .build();

    private final Predicate PREDICATE_E = IncludedPredicate.builder()
            .lhs("$.e")
            .values(Sets.newHashSet("E1", "E2"))
            .build();

    @Test
    public void testGetDNFCriteria() {
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
        Assert.assertEquals(expectedDNFCriteria, actualDNFCriteria);
    }

    @Test
    public void testGetCNFCriteria() {
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
        Assert.assertEquals(expectedCNFCriteria, actualCNFCriteria);
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

}
