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
 *
 */
package com.phonepe.mustang.index.builder;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.detail.Caveat;
import com.phonepe.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.mustang.index.core.Key;
import com.phonepe.mustang.index.core.impl.DNFInvertedIndex;
import com.phonepe.mustang.index.entry.extractor.DNFPostingListsExtractor;
import com.phonepe.mustang.index.group.IndexGroup;
import com.phonepe.mustang.index.operation.IndexOperation;
import com.phonepe.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DNFIndexer {
    public static final String ZERO_SIZE_CONJUNCTION_ENTRY_KEYNAME = "ZZZ";
    private static final String CONJUNCTION_ENTRY_ID_FORMAT = "%s#%s";
    private static final Comparator<TreeMap<Integer, ConjunctionPostingEntry>> POSTING_ENTRY_COMPARATOR = (e1,
            e2) -> (ObjectUtils.compare(e1.firstEntry()
                    .getValue(),
                    e2.firstEntry()
                            .getValue(),
                    true));
    @NotNull
    private final DNFCriteria criteria;
    @Valid
    @NotNull
    private final IndexGroup indexGroup;
    @NotNull
    private final IndexOperation operation;

    public void index() {
        final DNFInvertedIndex<ConjunctionPostingEntry> dnfInvertedIndex = indexGroup.getDnfInvertedIndex();
        final Map<Integer, Map<Key, TreeMap<Integer, ConjunctionPostingEntry>>> indexTable = dnfInvertedIndex
                .getTable();
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

                        dnfInvertedIndex.getLinkages()
                                .computeIfAbsent(kSize, x -> Sets.newTreeSet())
                                .add(iId);

                        final List<Map<Key, TreeMap<Integer, ConjunctionPostingEntry>>> postingLists = conjunction
                                .getPredicates()
                                .stream()
                                .map(predicate -> predicate.accept(DNFPostingListsExtractor.builder()
                                        .iId(iId)
                                        .eId(criteria.getId())
                                        .dnfKeyFrequency(indexGroup.getDnfKeyFrequency())
                                        .allPaths(indexGroup.getAllPaths())
                                        .build()))
                                .collect(Collectors.toList());

                        if (kSize == 0) {
                            // ZERO size handling
                            final Key key = Key.builder()
                                    .name(ZERO_SIZE_CONJUNCTION_ENTRY_KEYNAME)
                                    .caveat(Caveat.NONE)
                                    .value(0)
                                    .upperBoundScore(0)
                                    .build();
                            final Map<Key, TreeMap<Integer, ConjunctionPostingEntry>> zPostingLists = postingLists
                                    .stream()
                                    .flatMap(m -> m.entrySet()
                                            .stream())
                                    .map(Map.Entry::getValue)
                                    .flatMap(x -> x.entrySet()
                                            .stream())
                                    .map(Map.Entry::getValue)
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
                                            Collectors.mapping(Pair::getValue,
                                                    Collectors.toMap(ConjunctionPostingEntry::getIId,
                                                            x -> x,
                                                            (o, n) -> n,
                                                            TreeMap::new))));
                            postingLists.add(zPostingLists);
                        }

                        postingLists.add(indexTable.getOrDefault(kSize, Collections.emptyMap()));
                        indexTable.put(kSize, CriteriaIndexBuilder.compactPostingLists(postingLists));
                    }
                });

        dnfInvertedIndex.getActiveIds()
                .put(criteria.getId(), newIIds);

        // Keep the index sorted.
        indexTable.entrySet()
                .forEach(x -> x.setValue(sortPostingLists(x.getValue())));
    }

    private LinkedHashMap<Key, TreeMap<Integer, ConjunctionPostingEntry>> sortPostingLists(
            Map<Key, TreeMap<Integer, ConjunctionPostingEntry>> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(POSTING_ENTRY_COMPARATOR))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> n, LinkedHashMap::new));

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
        }

        @Override
        public Pair<Boolean, Integer> visitDelete() {
            return Pair.of(false, cnfInvertedIndex.getNextInternalIdFromCache(conjunctionId));
        }
    }

}
