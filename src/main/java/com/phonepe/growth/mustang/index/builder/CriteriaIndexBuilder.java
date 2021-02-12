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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.Pair;

import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.CriteriaVisitor;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.InvertedIndex;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.entry.extractor.CNFPostingListsExtractor;
import com.phonepe.growth.mustang.index.entry.extractor.DNFPostingListsExtractor;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaIndexBuilder implements CriteriaVisitor<Void> {
    public static final String ZERO_SIZE_CONJUNCTION_ENTRY_KEY = "ZZZ";
    public static final String ZERO_SIZE_DISJUNCTION_ENTRY_KEY = "ZZZ";
    private static final String CONJUNCTION_ENTRY_ID_FORMAT = "%s#%s";
    @Valid
    @NotNull
    private IndexGroup indexGroup;

    @Override
    public Void visit(DNFCriteria dnf) {
        IntStream.range(0,
                dnf.getConjunctions()
                        .size())
                .boxed()
                .forEach(j -> {
                    final Conjunction conjunction = dnf.getConjunctions()
                            .get(j);
                    final InvertedIndex<ConjunctionPostingEntry> dnfInvertedIndex = indexGroup.getDnfInvertedIndex();
                    final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> indexTable = dnfInvertedIndex
                            .getTable();
                    final int kSize = conjunction.getPredicates()
                            .stream()
                            .filter(predicate -> PredicateType.INCLUDED.equals(predicate.getType()))
                            .mapToInt(e -> 1)
                            .sum();

                    final List<Map<Key, TreeSet<ConjunctionPostingEntry>>> postingLists = conjunction.getPredicates()
                            .stream()
                            .map(predicate -> predicate.accept(DNFPostingListsExtractor.builder()
                                    .iId(dnfInvertedIndex.getInternalIdFromCache(
                                            String.format(CONJUNCTION_ENTRY_ID_FORMAT, dnf.getId(), j)))
                                    .eId(dnf.getId())
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
                    indexTable.put(kSize, compactPostingLists(postingLists));
                });

        return null;
    }

    @Override
    public Void visit(CNFCriteria cnf) {
        final int disjunctionSize = cnf.getDisjunctions()
                .size();
        final InvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex = indexGroup.getCnfInvertedIndex();
        final Map<Integer, Integer[]> disjunctionCounters = ((CNFInvertedIndex<DisjunctionPostingEntry>) cnfInvertedIndex)
                .getDisjunctionCounters();
        final Integer[] disjunctionCounter = disjunctionCounters.computeIfAbsent(
                cnfInvertedIndex.getInternalIdFromCache(cnf.getId()),
                x -> new Integer[disjunctionSize]);
        final Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> indexTable = cnfInvertedIndex.getTable();
        final int kSize = cnf.getDisjunctions()
                .stream()
                .filter(disjunction -> !isDisjunctionWithExcludedPredicate(disjunction))
                .mapToInt(e -> 1)
                .sum();

        IntStream.range(0, disjunctionSize)
                .boxed()
                .forEach(i -> {
                    final Disjunction disjunction = cnf.getDisjunctions()
                            .get(i);
                    final List<Map<Key, TreeSet<DisjunctionPostingEntry>>> postingLists = disjunction.getPredicates()
                            .stream()
                            .map(predicate -> predicate.accept(CNFPostingListsExtractor.builder()
                                    .iId(cnfInvertedIndex.getInternalIdFromCache(cnf.getId()))
                                    .eId(cnf.getId())
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
                    indexTable.put(kSize, compactPostingLists(postingLists));
                    disjunctionCounter[i] = getExcludedPredicateCountFromDisjunction(disjunction);
                });
        return null;
    }

    private boolean isDisjunctionWithExcludedPredicate(Disjunction disjunction) {
        return disjunction.getPredicates()
                .stream()
                .anyMatch(predicate -> PredicateType.EXCLUDED.equals(predicate.getType()));
    }

    private <T, S> Map<T, TreeSet<S>> compactPostingLists(List<Map<T, TreeSet<S>>> maps) {
        final List<Map.Entry<T, TreeSet<S>>> tempResult = maps.stream()
                .collect(ArrayList::new, (set, map) -> set.addAll(map.entrySet()), (set1, set2) -> set1.addAll(set2));
        return tempResult.stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.reducing(new TreeSet<>(), (s1, s2) -> {
                            final TreeSet<S> combined = new TreeSet<>(s1);
                            combined.addAll(s2);
                            return combined;
                        }))));
    }

    private int getExcludedPredicateCountFromDisjunction(Disjunction disjunction) {
        return disjunction.getPredicates()
                .stream()
                .filter(predicate -> PredicateType.EXCLUDED.equals(predicate.getType()))
                .mapToInt(e -> 1)
                .sum();
    }

}
