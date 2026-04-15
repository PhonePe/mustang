/**
 * Copyright (c) 2022 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
 */
package com.phonepe.central.mustang.criteria.tautology;

import com.phonepe.central.mustang.criteria.Criteria;
import com.phonepe.central.mustang.criteria.CriteriaForm;
import com.phonepe.central.mustang.criteria.CriteriaVisitor;
import com.phonepe.central.mustang.criteria.impl.CNFCriteria;
import com.phonepe.central.mustang.criteria.impl.DNFCriteria;
import com.phonepe.central.mustang.criteria.impl.UNFCriteria;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TautologicalCriteria {

    public static Criteria generate(final CriteriaForm criteriaForm,
            final String criteriaId) {
        return criteriaForm.accept(new CriteriaForm.Visitor<>() {

            @Override
            public Criteria visitDNF() {
                return new DNFTautologicalCriteria(criteriaId);
            }

            @Override
            public Criteria visitCNF() {
                return new CNFTautologicalCriteria(criteriaId);
            }

            @Override
            public Criteria visitUNF() {
                return new UNFTautologicalCriteria(criteriaId);
            }

        });
    }

    public static boolean isTautologicalCriteria(final Criteria criteria) {
        return criteria.accept(new CriteriaVisitor<>() {
            @Override
            public Boolean visit(DNFCriteria dnf) {
                return dnf.getConjunctions().isEmpty();
            }

            @Override
            public Boolean visit(CNFCriteria cnf) {
                return cnf.getDisjunctions().isEmpty();
            }

            @Override
            public Boolean visit(UNFCriteria unf) {
                return unf.getPredicates().isEmpty() && unf.getCriterias().isEmpty();
            }
        });
    }
}
