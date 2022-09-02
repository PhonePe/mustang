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

import com.google.common.collect.Lists;
import com.phonepe.mustang.composition.Composition;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.composition.impl.Disjunction;
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.predicate.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CriteriaUtils {

    public static CNFCriteria getCNFCriteria(DNFCriteria dnfCriteria) {
        return CNFCriteria.builder()
                .id(dnfCriteria.getId())
                .disjunctions(getCompositions(dnfCriteria.getConjunctions(), predicates -> Disjunction.builder()
                        .predicates(predicates)
                        .build()))
                .build();
    }

    public static DNFCriteria getDNFCriteria(CNFCriteria cnfCriteria) {
        return DNFCriteria.builder()
                .id(cnfCriteria.getId())
                .conjunctions(getCompositions(cnfCriteria.getDisjunctions(), predicates -> Conjunction.builder()
                        .predicates(predicates)
                        .build()))
                .build();
    }

    private static <T> List<T> getCompositions(List<? extends Composition> compositions,
            Function<List<Predicate>, T> transformingFunction) {
        // Separating out multi predicate compositions and single predicate compositions
        List<Predicate> commonPredicates = compositions.stream()
                .filter(composition -> composition.getPredicates()
                        .size() == 1)
                .map(composition -> composition.getPredicates()
                        .get(0))
                .collect(Collectors.toList());
        List<Composition> multiPredicateCompositions = compositions.stream()
                .filter(composition -> composition.getPredicates()
                        .size() > 1)
                .collect(Collectors.toList());

        // If there are no multi predicate composition, just create a single composition with all predicates
        if (multiPredicateCompositions.isEmpty()) {
            return Lists.newArrayList(transformingFunction.apply(commonPredicates));
        }

        List<T> resultantCompositions = new ArrayList<>();

        int[] predicateIndex = new int[multiPredicateCompositions.size()];
        int predicateIndexUpdater = multiPredicateCompositions.size() - 1;
        while (predicateIndexUpdater != -1) {
            // Taking one predicate from each composition according to predicateIndex
            List<Predicate> predicates = IntStream.range(0, multiPredicateCompositions.size())
                    .mapToObj(compositionNumber -> multiPredicateCompositions.get(compositionNumber)
                            .getPredicates()
                            .get(predicateIndex[compositionNumber]))
                    .collect(Collectors.toList());
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
        return resultantCompositions;
    }
}

