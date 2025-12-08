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
package com.phonepe.growth.mustang.criteria;

import com.google.common.collect.Lists;
import com.phonepe.growth.mustang.composition.Composition;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.composition.CompositionType.Visitor;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.UNFCriteria;
import com.phonepe.growth.mustang.criteria.tautology.TautologicalCriteria;
import com.phonepe.growth.mustang.criteria.tautology.UNFTautologicalCriteria;
import com.phonepe.growth.mustang.predicate.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CriteriaUtils {

    public static final String UNF_CRITERIA_SEPARATOR = "_";
    public static final String INTERMEDIATE_CRITERIA_ID = "INTERMEDIATE";

    public static CNFCriteria getCNFCriteria(Criteria criteria) {
        return criteria.accept(new CriteriaVisitor<>() {
            @Override
            public CNFCriteria visit(DNFCriteria dnfCriteria) {
                return CNFCriteria.builder()
                        .id(dnfCriteria.getId())
                        .disjunctions(getCompositions(dnfCriteria.getConjunctions(), predicates -> Disjunction.builder()
                                .predicates(predicates)
                                .build()))
                        .build();
            }

            @Override
            public CNFCriteria visit(CNFCriteria cnfCriteria) {
                return cnfCriteria;
            }

            @Override
            public CNFCriteria visit(UNFCriteria unf) {
                // convert to normalize form and recall this function
                return getCNFCriteria(getNormalizedCriteria(unf));
            }
        });
    }

    public static DNFCriteria getDNFCriteria(Criteria criteria) {
        return criteria.accept(new CriteriaVisitor<>() {
            @Override
            public DNFCriteria visit(DNFCriteria dnfCriteria) {
                return dnfCriteria;
            }

            @Override
            public DNFCriteria visit(CNFCriteria cnfCriteria) {
                return DNFCriteria.builder()
                        .id(cnfCriteria.getId())
                        .conjunctions(getCompositions(cnfCriteria.getDisjunctions(), predicates -> Conjunction.builder()
                                .predicates(predicates)
                                .build()))
                        .build();
            }

            @Override
            public DNFCriteria visit(UNFCriteria unf) {
                // convert to normalize form and recall this function
                return getDNFCriteria(getNormalizedCriteria(unf));
            }
        });
    }

    public static Criteria getNormalizedCriteria(Criteria criteria) {
        return criteria.accept(new CriteriaVisitor<>() {
            @Override
            public Criteria visit(DNFCriteria dnf) {
                return dnf;
            }

            @Override
            public Criteria visit(CNFCriteria cnf) {
                return cnf;
            }

            @Override
            public Criteria visit(UNFCriteria unf) {
                List<Criteria> filteredCriterias = unf.getCriterias()
                        .stream()
                        .filter(c -> !TautologicalCriteria.isTautologicalCriteria(c))
                        .toList();
                // early exit in case UNF contains a single criteria and no predicate
                if (unf.getPredicates()
                        .isEmpty() && filteredCriterias.size() == 1) {
                    Criteria resultCriteria = filteredCriterias.get(0);
                    resultCriteria.setId(unf.getId());
                    return getNormalizedCriteria(resultCriteria);
                }
                Criteria predicateCriteria = unf.getType()
                        .accept(new Visitor<>() {
                            @Override
                            public Criteria visitAnd() {
                                return CNFCriteria.builder()
                                        .id(INTERMEDIATE_CRITERIA_ID)
                                        .disjunctions(unf.getPredicates()
                                                .stream()
                                                .map(predicate -> Disjunction.builder()
                                                        .predicate(predicate)
                                                        .build())
                                                .toList())
                                        .build();
                            }

                            @Override
                            public Criteria visitOr() {
                                return DNFCriteria.builder()
                                        .id(INTERMEDIATE_CRITERIA_ID)
                                        .conjunctions(unf.getPredicates()
                                                .stream()
                                                .map(predicate -> Conjunction.builder()
                                                        .predicate(predicate)
                                                        .build())
                                                .toList())
                                        .build();
                            }
                        });
                // early exit in case UNF contains no criteria
                if (filteredCriterias.isEmpty()) {
                    predicateCriteria.setId(unf.getId());
                    return predicateCriteria;
                }
                ArrayList<Criteria> criterias = new ArrayList<>(filteredCriterias);
                criterias.add(predicateCriteria);
                return mergeCriteria(unf.getType(), unf.getId(), criterias.toArray(new Criteria[0]));
            }
        });
    }

    public static UNFCriteria getUNFCriteria(final Criteria criteria) {
        return criteria.accept(new CriteriaVisitor<>() {
            @Override
            public UNFCriteria visit(DNFCriteria dnf) {
                // Tautological criteria
                if (dnf.getConjunctions().isEmpty()){
                    return new UNFTautologicalCriteria(dnf.getId());
                }
                // 1 depth criteria
                if (dnf.getConjunctions().size() == 1){
                    return UNFCriteria.builder()
                            .id(dnf.getId())
                            .type(CompositionType.AND)
                            .predicates(dnf.getConjunctions()
                                    .get(0)
                                    .getPredicates())
                            .build();
                }
                // 2 depth criteria
                return UNFCriteria.builder()
                        .id(dnf.getId())
                        .type(CompositionType.OR)
                        .criterias(IntStream.range(0, dnf.getConjunctions()
                                        .size())
                                .filter(i -> dnf.getConjunctions()
                                        .get(i)
                                        .getPredicates()
                                        .size() > 1)
                                .mapToObj(i -> UNFCriteria.builder()
                                        .id(String.join(UNF_CRITERIA_SEPARATOR, dnf.getId(), String.valueOf(i)))
                                        .type(CompositionType.AND)
                                        .predicates(dnf.getConjunctions()
                                                .get(i)
                                                .getPredicates())
                                        .build())
                                .toList())
                        .predicates(dnf.getConjunctions()
                                .stream()
                                .filter(conjunction -> conjunction.getPredicates()
                                        .size() == 1)
                                .map(conjunction -> conjunction.getPredicates()
                                        .get(0))
                                .toList())
                        .build();
            }

            @Override
            public UNFCriteria visit(CNFCriteria cnf) {
                // Tautological criteria
                if (cnf.getDisjunctions().isEmpty()){
                    return new UNFTautologicalCriteria(cnf.getId());
                }
                // 1 depth criteria
                if (cnf.getDisjunctions().size() == 1){
                    return UNFCriteria.builder()
                            .id(cnf.getId())
                            .type(CompositionType.OR)
                            .predicates(cnf.getDisjunctions()
                                    .get(0)
                                    .getPredicates())
                            .build();
                }
                // 2 depth criteria
                return UNFCriteria.builder()
                        .id(cnf.getId())
                        .type(CompositionType.AND)
                        .criterias(IntStream.range(0, cnf.getDisjunctions()
                                        .size())
                                .filter(i -> cnf.getDisjunctions()
                                        .get(i)
                                        .getPredicates()
                                        .size() > 1)
                                .mapToObj(i -> UNFCriteria.builder()
                                        .id(String.join(UNF_CRITERIA_SEPARATOR, cnf.getId(), String.valueOf(i)))
                                        .type(CompositionType.OR)
                                        .predicates(cnf.getDisjunctions()
                                                .get(i)
                                                .getPredicates())
                                        .build())
                                .toList())
                        .predicates(cnf.getDisjunctions()
                                .stream()
                                .filter(disjunction -> disjunction.getPredicates()
                                        .size() == 1)
                                .map(disjunction -> disjunction.getPredicates()
                                        .get(0))
                                .toList())
                        .build();
            }

            @Override
            public UNFCriteria visit(UNFCriteria unf) {
                return unf;
            }
        });

    }

    public static Criteria mergeCriteria(CompositionType compositionType,
            String id,
            Criteria... criterias) {
        return compositionType.accept(new Visitor<>() {
            @Override
            public Criteria visitAnd() {
                return CNFCriteria.builder()
                        .id(id)
                        .disjunctions(Arrays.stream(criterias)
                                .map(CriteriaUtils::getCNFCriteria)
                                .flatMap(c -> c.getDisjunctions()
                                        .stream())
                                .distinct()
                                .collect(Collectors.toList()))
                        .build();
            }

            @Override
            public Criteria visitOr() {
                return DNFCriteria.builder()
                        .id(id)
                        .conjunctions(Arrays.stream(criterias)
                                .map(CriteriaUtils::getDNFCriteria)
                                .flatMap(c -> c.getConjunctions()
                                        .stream())
                                .distinct()
                                .collect(Collectors.toList()))
                        .build();
            }
        });
    }

    private static <T extends Composition> Collection<T> getCompositions(List<? extends Composition> compositions,
            Function<Collection<Predicate>, T> transformingFunction) {
        // Separating out multi predicate compositions and single predicate compositions
        List<Predicate> commonPredicates = compositions.stream()
                .filter(composition -> composition.getPredicates()
                        .size() == 1)
                .map(composition -> composition.getPredicates()
                        .get(0))
                .distinct()
                .collect(Collectors.toList());
        List<? extends Composition> multiPredicateCompositions = compositions.stream()
                .filter(composition -> composition.getPredicates()
                        .size() > 1)
                .distinct()
                .collect(Collectors.toList());

        // If there are no multi predicate composition, just create a single composition with all predicates
        if (multiPredicateCompositions.isEmpty()) {
            return Lists.newArrayList(transformingFunction.apply(commonPredicates));
        }

        Set<T> resultantCompositions = new HashSet<>();

        int[] predicateIndex = new int[multiPredicateCompositions.size()];
        int predicateIndexUpdater = multiPredicateCompositions.size() - 1;
        while (predicateIndexUpdater != -1) {
            // Taking one predicate from each composition according to predicateIndex
            Set<Predicate> predicates = IntStream.range(0, multiPredicateCompositions.size())
                    .mapToObj(compositionNumber -> multiPredicateCompositions.get(compositionNumber)
                            .getPredicates()
                            .get(predicateIndex[compositionNumber]))
                    .collect(Collectors.toSet());
            // Adding all the common predicates
            predicates.addAll(commonPredicates);

            resultantCompositions.add(transformingFunction.apply(predicates));

            // updating predicateIndex
            predicateIndexUpdater = multiPredicateCompositions.size() - 1;
            while (predicateIndexUpdater >= 0
                    && predicateIndex[predicateIndexUpdater] + 1 == multiPredicateCompositions.get(
                            predicateIndexUpdater)
                    .getPredicates()
                    .size()) {
                predicateIndex[predicateIndexUpdater] = 0;
                predicateIndexUpdater--;
            }
            if (predicateIndexUpdater != -1) {
                predicateIndex[predicateIndexUpdater]++;
            }
        }
        // (A) ^ (A v B) = A
        // (A) v (A ^ B) = A
        Set<T> singlePredicateCompositions = resultantCompositions.stream()
                .filter(composition -> composition.getPredicates()
                        .size() == 1)
                .collect(Collectors.toSet());
        Set<Predicate> predicateFilters = singlePredicateCompositions.stream()
                .map(composition -> composition.getPredicates()
                        .get(0))
                .collect(Collectors.toSet());
        Set<T> filteredCompositions = resultantCompositions.stream()
                .filter(composition -> composition.getPredicates()
                        .size() > 1)
                .filter(composition -> composition.getPredicates()
                        .stream()
                        .noneMatch(predicateFilters::contains))
                .collect(Collectors.toSet());
        filteredCompositions.addAll(singlePredicateCompositions);

        return filteredCompositions;
    }
}
