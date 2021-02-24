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
package com.phonepe.growth.mustang.index.builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.Pair;

import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.entry.extractor.CNFPostingListsExtractor;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.index.operation.CriteriaIndexOperation;
import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CNFIndexer {
    public static final String ZERO_SIZE_DISJUNCTION_ENTRY_KEY = "ZZZ";
    @NotNull
    private final CNFCriteria criteria;
    @Valid
    @NotNull
    private final IndexGroup indexGroup;
    @NotNull
    private final CriteriaIndexOperation indexOperation;

    public void index() {
        final int disjunctionSize = criteria.getDisjunctions()
                .size();
        final CNFInvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex = indexGroup.getCnfInvertedIndex();
        final Map<Integer, Integer[]> disjunctionCounters = cnfInvertedIndex.getDisjunctionCounters();
        final Pair<Boolean, Integer> operationMeta = indexOperation
                .accept(new IndexOperationMetaExtractor(cnfInvertedIndex, criteria.getId()));

        if (Boolean.TRUE.equals(operationMeta.getLeft())) {
            final Integer internalId = operationMeta.getRight();
            final Integer[] disjunctionCounter = disjunctionCounters.computeIfAbsent(internalId,
                    x -> new Integer[disjunctionSize]);
            final Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> indexTable = cnfInvertedIndex.getTable();
            final int kSize = criteria.getDisjunctions()
                    .stream()
                    .filter(disjunction -> !isDisjunctionWithExcludedPredicate(disjunction))
                    .mapToInt(e -> 1)
                    .sum();

            IntStream.range(0, disjunctionSize)
                    .boxed()
                    .forEach(i -> {
                        final Disjunction disjunction = criteria.getDisjunctions()
                                .get(i);
                        final List<Map<Key, TreeSet<DisjunctionPostingEntry>>> postingLists = disjunction
                                .getPredicates()
                                .stream()
                                .map(predicate -> predicate.accept(CNFPostingListsExtractor.builder()
                                        .iId(internalId)
                                        .eId(criteria.getId())
                                        .order(i)
                                        .postingLists(indexTable.getOrDefault(kSize, Collections.emptyMap()))
                                        .cnfKeyFrequency(indexGroup.getCnfKeyFrequency())
                                        .build()))
                                .collect(Collectors.toList());

                        if (kSize == 0) {
                            // Zero size handling
                            final Key key = Key.builder()
                                    .name(ZERO_SIZE_DISJUNCTION_ENTRY_KEY)
                                    .value(0)
                                    .upperBoundScore(0)
                                    .build();
                            final Map<Key, TreeSet<DisjunctionPostingEntry>> map = postingLists.stream()
                                    .flatMap(m -> m.entrySet()
                                            .stream())
                                    .map(Map.Entry::getValue)
                                    .flatMap(TreeSet::stream)
                                    .distinct()
                                    .map(entry -> DisjunctionPostingEntry.builder()
                                            .iId(entry.getIId())
                                            .eId(entry.getEId())
                                            .type(PredicateType.INCLUDED)
                                            .order(-1)
                                            .score(0)
                                            .build())
                                    .distinct()
                                    .map(entry -> Pair.of(key, entry))
                                    .collect(Collectors.groupingBy(Pair::getKey,
                                            Collectors.mapping(Pair::getValue, Collectors.toCollection(TreeSet::new))));
                            postingLists.add(map);
                        }

                        postingLists.add(indexTable.getOrDefault(kSize, Collections.emptyMap()));
                        indexTable.put(kSize, CriteriaIndexBuilder.compactPostingLists(postingLists));
                        disjunctionCounter[i] = getExcludedPredicateCountFromDisjunction(disjunction);
                    });
        }
    }

    private boolean isDisjunctionWithExcludedPredicate(Disjunction disjunction) {
        return disjunction.getPredicates()
                .stream()
                .anyMatch(predicate -> PredicateType.EXCLUDED.equals(predicate.getType()));
    }

    private int getExcludedPredicateCountFromDisjunction(Disjunction disjunction) {
        return disjunction.getPredicates()
                .stream()
                .filter(predicate -> PredicateType.EXCLUDED.equals(predicate.getType()))
                .mapToInt(e -> 1)
                .sum();
    }

    private static class IndexOperationMetaExtractor implements CriteriaIndexOperation.Visitor<Pair<Boolean, Integer>> {
        private final CNFInvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex;
        private final String criteriaId;

        private IndexOperationMetaExtractor(final CNFInvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex,
                final String criteriaId) {
            this.cnfInvertedIndex = cnfInvertedIndex;
            this.criteriaId = criteriaId;
        }

        @Override
        public Pair<Boolean, Integer> visitAdd() {
            return Pair.of(true, cnfInvertedIndex.getInternalIdFromCache(criteriaId));
        }

        @Override
        public Pair<Boolean, Integer> visitUpdate() {
            return Pair.of(true, cnfInvertedIndex.getNextInternalIdFromCache(criteriaId));
            // TODO initiate cleanup
        }

        @Override
        public Pair<Boolean, Integer> visitDelete() {
            return Pair.of(false, cnfInvertedIndex.getNextInternalIdFromCache(criteriaId));
            // TODO initiate cleanup
        }
    }

}
