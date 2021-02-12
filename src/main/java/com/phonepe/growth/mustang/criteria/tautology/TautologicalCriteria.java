/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.growth.mustang.criteria.tautology;

import java.util.UUID;

import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TautologicalCriteria {
    private static final String KEY_FORMAT = "$.%s";

    public static final Criteria generate(final CriteriaForm criteriaForm, final String criteriaId) {
        final String randomSalt = UUID.randomUUID()
                .toString();
        return criteriaForm.accept(new CriteriaForm.Visitor<Criteria>() {

            @Override
            public Criteria visitDNF() {
                return DNFCriteria.builder()
                        .id(criteriaId)
                        .conjunction(Conjunction.builder()
                                .predicate(ExcludedPredicate.builder()
                                        .lhs(String.format(KEY_FORMAT, randomSalt))
                                        .values(Sets.newHashSet(randomSalt))
                                        .build())
                                .build())
                        .build();
            }

            @Override
            public Criteria visitCNF() {
                return CNFCriteria.builder()
                        .id(criteriaId)
                        .disjunction(Disjunction.builder()
                                .predicate(ExcludedPredicate.builder()
                                        .lhs(String.format(KEY_FORMAT, randomSalt))
                                        .values(Sets.newHashSet(randomSalt))
                                        .build())
                                .build())
                        .build();
            }

        });
    }
}
