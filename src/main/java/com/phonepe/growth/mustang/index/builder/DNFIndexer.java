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
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.DNFInvertedIndex;
import com.phonepe.growth.mustang.index.entry.extractor.DNFPostingListsExtractor;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.index.operation.IndexOperation;
import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DNFIndexer {
    public static final String ZERO_SIZE_CONJUNCTION_ENTRY_KEY = "ZZZ";
    private static final String CONJUNCTION_ENTRY_ID_FORMAT = "%s#%s";
    @NotNull
    private final DNFCriteria criteria;
    @Valid
    @NotNull
    private final IndexGroup indexGroup;
    @NotNull
    private final IndexOperation operation;

    public void index() {
        final DNFInvertedIndex<ConjunctionPostingEntry> dnfInvertedIndex = indexGroup.getDnfInvertedIndex();
        final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> indexTable = dnfInvertedIndex.getTable();
        final Set<Integer> newIIds = Sets.newHashSet();

        IntStream.range(0,
                criteria.getConjunctions()
                        .size())
                .boxed()
                .forEach(j -> {
                    final Conjunction conjunction = criteria.getConjunctions()
                            .get(j);
                    final Pair<Boolean, Integer> operationMeta = operation
                            .accept(new IndexOperationMetaExtractor(dnfInvertedIndex,
                                    String.format(CONJUNCTION_ENTRY_ID_FORMAT, criteria.getId(), j)));
                    final Integer iId = operationMeta.getRight();
                    newIIds.add(iId);
                    if (Boolean.TRUE.equals(operationMeta.getLeft())) {
                        final int kSize = conjunction.getPredicates()
                                .stream()
                                .filter(predicate -> PredicateType.INCLUDED.equals(predicate.getType()))
                                .mapToInt(e -> 1)
                                .sum();

                        final List<Map<Key, TreeSet<ConjunctionPostingEntry>>> postingLists = conjunction
                                .getPredicates()
                                .stream()
                                .map(predicate -> predicate.accept(DNFPostingListsExtractor.builder()
                                        .iId(iId)
                                        .eId(criteria.getId())
                                        .dnfKeyFrequency(indexGroup.getDnfKeyFrequency())
                                        .build()))
                                .collect(Collectors.toList());

                        if (kSize == 0) {
                            // ZERO size handling
                            final Key key = Key.builder()
                                    .name(ZERO_SIZE_CONJUNCTION_ENTRY_KEY)
                                    .value(0)
                                    .upperBoundScore(0)
                                    .build();
                            final Map<Key, TreeSet<ConjunctionPostingEntry>> map = postingLists.stream()
                                    .flatMap(m -> m.entrySet()
                                            .stream())
                                    .map(Map.Entry::getValue)
                                    .flatMap(TreeSet::stream)
                                    .distinct()
                                    .map(entry -> ConjunctionPostingEntry.builder()
                                            .iId(entry.getIId())
                                            .eId(entry.getEId())
                                            .type(PredicateType.INCLUDED)
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
                    }
                });
        final Set<Integer> oldIIds = dnfInvertedIndex.getActiveIds()
                .put(criteria.getId(), newIIds);
        // TODO handle cleanup
    }

    private static class IndexOperationMetaExtractor implements IndexOperation.Visitor<Pair<Boolean, Integer>> {
        private final DNFInvertedIndex<ConjunctionPostingEntry> cnfInvertedIndex;
        private final String conjunctionId;

        private IndexOperationMetaExtractor(final DNFInvertedIndex<ConjunctionPostingEntry> dnfInvertedIndex,
                final String conjunctionId) {
            this.cnfInvertedIndex = dnfInvertedIndex;
            this.conjunctionId = conjunctionId;
        }

        @Override
        public Pair<Boolean, Integer> visitAdd() {
            return Pair.of(true, cnfInvertedIndex.getInternalIdFromCache(conjunctionId));
        }

        @Override
        public Pair<Boolean, Integer> visitUpdate() {
            return Pair.of(true, cnfInvertedIndex.getNextInternalIdFromCache(conjunctionId));
            // TODO initiate cleanup
        }

        @Override
        public Pair<Boolean, Integer> visitDelete() {
            return Pair.of(false, cnfInvertedIndex.getNextInternalIdFromCache(conjunctionId));
            // TODO initiate cleanup
        }
    }

}
