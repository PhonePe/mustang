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
package com.phonepe.mustang.search.matcher;

import com.google.common.collect.Maps;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.mustang.index.core.Key;
import com.phonepe.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.mustang.predicate.PredicateType;
import com.phonepe.mustang.search.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.MutablePair;

@Data
@Builder
@AllArgsConstructor
public class CNFMatcher {
    private static final Comparator<Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>> ID_COMPARATOR = (
            e1,
            e2) -> (ObjectUtils.compare(getIdSafely(e1), getIdSafely(e2), true));
    private static final Comparator<Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>> TYPE_COMPARATOR = (
            e1,
            e2) -> (ObjectUtils.compare(getTypeSafely(e1), getTypeSafely(e2), true));
    private static final Comparator<Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>> ORDER_COMPARATOR = (
            e1,
            e2) -> (ObjectUtils.compare(getOrderSafely(e1), getOrderSafely(e2), true));
    private final CNFInvertedIndex<DisjunctionPostingEntry> invertedIndex;
    private final Query query;
    private final Map<String, Criteria> allCriterias;
    private final Map<String, Object> pathValues;
    private final boolean score;

    private static DisjunctionPostingEntry getDisjunctionPostingEntry(
            final TreeMap<Integer, DisjunctionPostingEntry> map,
            final Integer iId) {
        return Objects.nonNull(iId)
               ? map.get(iId)
               : null;
    }

    private static Integer getIdSafely(
            final Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>> entry) {
        final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(entry.getValue()
                .getValue(),
                entry.getValue()
                        .getKey());
        return disjunctionPostingEntry != null ? disjunctionPostingEntry.getIId() : null;
    }

    private static PredicateType getTypeSafely(
            final Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>> entry) {
        final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(entry.getValue()
                .getValue(),
                entry.getValue()
                        .getKey());
        return disjunctionPostingEntry != null ? disjunctionPostingEntry.getType() : null;
    }

    private static Integer getOrderSafely(
            Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>> entry) {
        final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(entry.getValue()
                .getValue(),
                entry.getValue()
                        .getKey());
        return disjunctionPostingEntry != null ? disjunctionPostingEntry.getOrder() : Integer.MAX_VALUE;
    }

    public Map<String, Double> getMatches() {
        final Map<String, Double> result = Maps.newHashMap();
        final Map<Integer, Map<Key, TreeMap<Integer, DisjunctionPostingEntry>>> table = invertedIndex.getTable();
        final Map<Integer, Integer[]> disjunctionCounters = invertedIndex.getDisjunctionCounters();
        final int start = 0;
        final int end = table.keySet()
                .stream()
                .mapToInt(x -> x)
                .max()
                .orElse(0);
        IntStream.rangeClosed(start, end)
                .map(i -> end - i + start)
                .boxed()
                .forEach(k -> {
                    final TreeSet<Integer> links = invertedIndex.getLinkages()
                            .get(k);
                    final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] pLists = getPostingListsCNF(
                            table,
                            k);
                    initializeCurrentEntriesCNF(pLists);
                    /* Processing k = 0 and k = 1 are identical */
                    if (k == 0) {
                        k = 1;
                    }
                    if (pLists.length < k) {
                        /* Too few posting lists for any conjunction to be satisfied */
                        return;
                    }
                    int nextID = 0;
                    while (canContinue(pLists, k)) {
                        /*
                         * Check if the first k posting lists have the same conjunction ID in their
                         * current entries
                         */
                        if (sameConjunctionCheck(pLists, k - 1)) {
                            /*
                             * For each disjunction in the current CNF, one counter is initialized to the
                             * negative number of EXCLUDED predicates
                             */
                            final Integer[] counters = getCounters(disjunctionCounters,
                                    pLists[0].getValue()
                                            .getKey());

                            disjunctionEvaluationCheck(result, pLists, k, counters);

                            /* nextID is the smallest possible ID after current ID */
                            nextID = getNextHigherId(links, pLists[k - 1].getValue()
                                    .getKey());

                        } else {
                            /* Skip first k-1 posting lists */
                            nextID = getNextId(links, pLists[k - 1].getValue()
                                    .getKey(), nextID);
                        }
                        skipTo(pLists, nextID);
                    }
                });

        return result;

    }

    @SuppressWarnings("unchecked")
    private Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] getPostingListsCNF(
            final Map<Integer, Map<Key, TreeMap<Integer, DisjunctionPostingEntry>>> table,
            final int k) {
        final Map<Key, TreeMap<Integer, DisjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());
        return getMatchingKeys(map).collect(Collectors.toMap(x -> x, x -> MutablePair.of(0, map.get(x))))
                .entrySet()
                .toArray(Entry[]::new);
    }

    private Stream<Key> getMatchingKeys(final Map<Key, TreeMap<Integer, DisjunctionPostingEntry>> map) {
        return map.keySet()
                .stream()
                .filter(key -> key.getCaveat()
                        .visit(new CaveatEnforcer(key, pathValues.get(key.getName()))));
    }

    private void initializeCurrentEntriesCNF(
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] pLists) {
        Arrays.stream(pLists)
                .forEach(pList -> pList.getValue()
                        .setLeft(pList.getValue()
                                .getRight()
                                .firstEntry()
                                .getValue()
                                .getIId()));
        sortByCurrentEntriesCNF(pLists);
    }

    private boolean canContinue(
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] pLists,
            final int k) {
        return Objects.nonNull(pLists[k - 1].getValue()
                .getKey()) && Objects.nonNull(getDisjunctionPostingEntry(pLists[k - 1].getValue()
                .getValue(), pLists[k - 1].getValue()
                .getKey()));
    }

    private void sortByCurrentEntriesCNF(
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] pLists) {
        Arrays.sort(pLists,
                ID_COMPARATOR.thenComparing(TYPE_COMPARATOR)
                        .thenComparing(ORDER_COMPARATOR));
    }

    private boolean sameConjunctionCheck(
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] pLists,
            final Integer k) {
        return Objects.nonNull(getDisjunctionPostingEntry(pLists[0].getValue()
                .getValue(), pLists[0].getValue()
                .getKey())) && Objects.nonNull(getDisjunctionPostingEntry(pLists[k].getValue()
                .getValue(), pLists[k].getValue()
                .getKey())) && pLists[0].getValue()
                .getKey()
                .equals(pLists[k].getValue()
                        .getKey());
    }

    private void disjunctionEvaluationCheck(final Map<String, Double> result,
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] pLists,
            final Integer k,
            final Integer[] counters) {
        for (int l = 0; ((l < pLists.length) && sameConjunctionCheck(pLists, l)); l++) {
            /* Ignore entries in the Z posting list */
            final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(pLists[l].getValue()
                    .getValue(),
                    pLists[l].getValue()
                            .getKey());
            if (Objects.isNull(disjunctionPostingEntry) || disjunctionPostingEntry.getOrder() == -1) {
                continue;
            }
            if (PredicateType.EXCLUDED.equals(disjunctionPostingEntry.getType())) {
                counters[disjunctionPostingEntry.getOrder()]++;
            } else {
                /* Disjunction is satisfied */
                counters[disjunctionPostingEntry.getOrder()] = 1;
            }
        }
        if (Arrays.stream(counters)
                .allMatch(i -> i != 0)) {
            final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(pLists[k - 1].getValue()
                    .getValue(),
                    pLists[k - 1].getValue()
                            .getKey());
            if (Objects.nonNull(disjunctionPostingEntry)) {
                checkAndAdd(result, disjunctionPostingEntry);
            }
        }
    }

    private void checkAndAdd(final Map<String, Double> result, final DisjunctionPostingEntry postingEntry) {
        // Check to see if the current entry represents criteria's latest version.
        if (invertedIndex.getInternalIdFromCache(postingEntry.getEId())
                .equals(postingEntry.getIId())) {
            result.put(postingEntry.getEId(), computeScore(postingEntry.getEId()));
        }
    }

    private double computeScore(final String cId) {
        if (score) {
            return allCriterias.get(cId)
                    .getScore(query.getRequestContext());
        }
        return 0;
    }

    private int getNextHigherId(final TreeSet<Integer> links,
                                final Integer internalId) {
        return Optional.ofNullable(links.higher(internalId))
                .orElse(internalId + 1);
    }

    private int getNextId(final TreeSet<Integer> links,
                          final Integer internalId,
                          final int nextId) {
        if (nextId != internalId) {
            return internalId;
        }
        return getNextHigherId(links, internalId);
    }

    private Integer[] getCounters(final Map<Integer, Integer[]> disjunctionCounters,
                                  final int iId) {
        return Arrays.stream(disjunctionCounters.get(iId))
                .map(x -> -1 * x)
                .toArray(Integer[]::new);
    }

    private void skipTo(final Entry<Key, MutablePair<Integer, TreeMap<Integer, DisjunctionPostingEntry>>>[] pLists,
            final int nextID) {
        IntStream.range(0, pLists.length)
                .filter(l -> Objects.nonNull(pLists[l].getValue()
                        .getKey()) && pLists[l].getValue()
                        .getKey() < nextID)
                .forEach(l -> pLists[l].getValue()
                        .setLeft(pLists[l].getValue()
                                .getValue()
                                .navigableKeySet()
                                .ceiling(nextID)));
        sortByCurrentEntriesCNF(pLists);
    }

}
