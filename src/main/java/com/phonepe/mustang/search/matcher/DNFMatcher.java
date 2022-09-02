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
import com.phonepe.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.mustang.index.core.Key;
import com.phonepe.mustang.index.core.impl.DNFInvertedIndex;
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
public class DNFMatcher {

    private static final Comparator<Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>> COMPARATOR = (
            e1,
            e2) -> (ObjectUtils.compare(getPostingEntry(e1), getPostingEntry(e2), true));
    private final DNFInvertedIndex<ConjunctionPostingEntry> invertedIndex;
    private final Query query;
    private final Map<String, Criteria> allCriterias;
    private final Map<String, Object> pathValues;
    private final boolean score;

    private static ConjunctionPostingEntry getConjunctionPostingEntry(
            final TreeMap<Integer, ConjunctionPostingEntry> map,
            final Integer iId) {
        return Objects.nonNull(iId)
               ? map.get(iId)
               : null;
    }

    private static ConjunctionPostingEntry getPostingEntry(
            final Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>> entry) {
        return getConjunctionPostingEntry(entry.getValue()
                .getValue(),
                entry.getValue()
                        .getKey());
    }

    public Map<String, Double> getMatches() {
        final Map<String, Double> result = Maps.newHashMap();
        final Map<Integer, Map<Key, TreeMap<Integer, ConjunctionPostingEntry>>> table = invertedIndex.getTable();
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
                    final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>[] pLists = getPostingListsDNF(
                            table,
                            k);
                    initializeCurrentEntriesDNF(pLists);

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
                            /* Reject conjunction if EXCLUDED predicate is violated */
                            final ConjunctionPostingEntry conjunctionPostingEntry = getConjunctionPostingEntry(
                                    pLists[0].getValue()
                                            .getValue(),
                                    pLists[0].getValue()
                                            .getKey());
                            if (!conjunctionRejectionCheck(conjunctionPostingEntry)) {
                                /* conjunction is fully satisfied */
                                checkAndAdd(result, conjunctionPostingEntry);
                            }
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
    private Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>[] getPostingListsDNF(
            final Map<Integer, Map<Key, TreeMap<Integer, ConjunctionPostingEntry>>> table,
            final int k) {
        final Map<Key, TreeMap<Integer, ConjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());
        return getMatchingKeys(map).collect(Collectors.toMap(x -> x, x -> MutablePair.of(0, map.get(x))))
                .entrySet()
                .toArray(Entry[]::new);
    }

    private Stream<Key> getMatchingKeys(final Map<Key, TreeMap<Integer, ConjunctionPostingEntry>> map) {
        return map.keySet()
                .stream()
                .filter(key -> key.getCaveat()
                        .visit(new CaveatEnforcer(key, pathValues.get(key.getName()))));
    }

    private void initializeCurrentEntriesDNF(
            Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>[] pLists) {
        Arrays.stream(pLists)
                .forEach(pList -> pList.getValue()
                        .setLeft(pList.getValue()
                                .getRight()
                                .firstEntry()
                                .getValue()
                                .getIId()));
        sortByCurrentEntriesDNF(pLists);
    }

    private boolean canContinue(
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>[] pLists,
            final int k) {
        return Objects.nonNull(pLists[k - 1].getValue()
                .getKey()) && Objects.nonNull(getConjunctionPostingEntry(pLists[k - 1].getValue()
                .getValue(), pLists[k - 1].getValue()
                .getKey()));
    }

    private boolean conjunctionRejectionCheck(final ConjunctionPostingEntry conjunctionPostingEntry) {
        return Objects.isNull(conjunctionPostingEntry)
                || PredicateType.EXCLUDED.equals(conjunctionPostingEntry.getType());
    }

    private void sortByCurrentEntriesDNF(
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>[] pLists) {
        Arrays.sort(pLists, COMPARATOR);
    }

    private boolean sameConjunctionCheck(
            final Map.Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>[] pLists,
            final Integer k) {
        return Objects.nonNull(getConjunctionPostingEntry(pLists[0].getValue()
                .getValue(), pLists[0].getValue()
                .getKey())) && Objects.nonNull(getConjunctionPostingEntry(pLists[k].getValue()
                .getValue(), pLists[k].getValue()
                .getKey())) && pLists[0].getValue()
                .getKey()
                .equals(pLists[k].getValue()
                        .getKey());
    }

    private void checkAndAdd(final Map<String, Double> result,
                             final ConjunctionPostingEntry postingEntry) {
        // Check to see if the current entry is part of criteria's latest version.
        if (invertedIndex.getActiveIds()
                .get(postingEntry.getEId())
                .contains(postingEntry.getIId())) {
            result.put(postingEntry.getEId(), computeScore(postingEntry.getEId()));
        }
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

    private double computeScore(final String cId) {
        if (score) {
            return allCriterias.get(cId)
                    .getScore(query.getRequestContext());
        }
        return 0;
    }

    private void skipTo(final Entry<Key, MutablePair<Integer, TreeMap<Integer, ConjunctionPostingEntry>>>[] pLists,
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
        sortByCurrentEntriesDNF(pLists);
    }

}
