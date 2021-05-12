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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.entry.extractor.CNFPostingListsExtractor;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.index.operation.IndexOperation;
import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CNFIndexer {
    public static final String ZERO_SIZE_DISJUNCTION_ENTRY_KEY = "ZZZ";
    private static final Comparator<TreeSet<DisjunctionPostingEntry>> ID_COMPARATOR = (e1,
            e2) -> (ObjectUtils.compare(e1.first()
                    .getIId(),
                    e2.first()
                            .getIId(),
                    true));
    private static final Comparator<TreeSet<DisjunctionPostingEntry>> TYPE_COMPARATOR = (e1,
            e2) -> (ObjectUtils.compare(e1.first()
                    .getType(),
                    e2.first()
                            .getType(),
                    true));
    @NotNull
    private final CNFCriteria criteria;
    @Valid
    @NotNull
    private final IndexGroup indexGroup;
    @NotNull
    private final IndexOperation operation;

    public void index() {
        final int disjunctionSize = criteria.getDisjunctions()
                .size();
        final CNFInvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex = indexGroup.getCnfInvertedIndex();
        final Map<Integer, Integer[]> disjunctionCounters = cnfInvertedIndex.getDisjunctionCounters();
        final Pair<Boolean, Integer> operationMeta = operation
                .accept(new IndexOperationMetaExtractor(cnfInvertedIndex, criteria.getId()));
        final Integer internalId = operationMeta.getRight();
        final Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> indexTable = cnfInvertedIndex.getTable();

        if (Boolean.TRUE.equals(operationMeta.getLeft())) {
            final Integer[] disjunctionCounter = disjunctionCounters.computeIfAbsent(internalId,
                    x -> new Integer[disjunctionSize]);
            final int kSize = criteria.getDisjunctions()
                    .stream()
                    .filter(disjunction -> !isDisjunctionWithExcludedPredicate(disjunction))
                    .mapToInt(e -> 1)
                    .sum();

            cnfInvertedIndex.getLinkages()
                    .computeIfAbsent(kSize, x -> Sets.newTreeSet())
                    .add(internalId);

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
                            final Map<Key, TreeSet<DisjunctionPostingEntry>> zPostingLists = postingLists.stream()
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
                            postingLists.add(zPostingLists);
                        }

                        postingLists.add(indexTable.getOrDefault(kSize, Collections.emptyMap()));
                        indexTable.put(kSize, CriteriaIndexBuilder.compactPostingLists(postingLists));
                        disjunctionCounter[i] = getExcludedPredicateCountFromDisjunction(disjunction);
                    });
        }

        // TODO handle cleanup

        // Keep the index sorted.
        indexTable.entrySet()
                .forEach(x -> {
                    sortPostingLists(x.getValue());
                });
    }

    private void sortPostingLists(Map<Key, TreeSet<DisjunctionPostingEntry>> map) {
        map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(ID_COMPARATOR.thenComparing(TYPE_COMPARATOR)))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
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

    private static class IndexOperationMetaExtractor implements IndexOperation.Visitor<Pair<Boolean, Integer>> {
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
        }

        @Override
        public Pair<Boolean, Integer> visitDelete() {
            return Pair.of(false, cnfInvertedIndex.getNextInternalIdFromCache(criteriaId));
        }
    }

}
