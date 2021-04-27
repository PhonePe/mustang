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
package com.phonepe.growth.mustang.search.matcher;

import static com.phonepe.growth.mustang.index.builder.DNFIndexer.ZERO_SIZE_CONJUNCTION_ENTRY_KEY;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.DNFInvertedIndex;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DNFMatcher {

    private final DNFInvertedIndex<ConjunctionPostingEntry> invertedIndex;
    private final Query query;
    private final Map<String, Criteria> allCriterias;

    public Map<String, Double> getMatches() {
        final Map<String, Double> result = Maps.newHashMap();
        final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> table = invertedIndex.getTable();
        final int start = 0;
        final int end = Math.min(query.getAssigment()
                .size(),
                table.keySet()
                        .stream()
                        .mapToInt(x -> x)
                        .max()
                        .orElse(0));
        IntStream.rangeClosed(start, end)
                .map(i -> end - i + start)
                .boxed()
                .forEach(k -> {
                    final TreeSet<Integer> links = invertedIndex.getLinkages()
                            .get(k);
                    final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists = getPostingListsDNF(
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
                        sortByCurrentEntriesDNF(pLists);
                        /*
                         * Check if the first k posting lists have the same conjunction ID in their
                         * current entries
                         */
                        if (sameConjunctionCheck(pLists, k - 1)) {
                            /* Reject conjunction if EXCLUDED predicate is violated */
                            final Optional<ConjunctionPostingEntry> conjunctionPostingEntry = getConjunctionPostingEntry(
                                    pLists[0].getValue()
                                            .getValue(),
                                    pLists[0].getValue()
                                            .getKey());
                            if (conjunctionRejectionCheck(conjunctionPostingEntry)) {
                                conjunctionRejectionSkip(k,
                                        pLists,
                                        pLists[0].getValue()
                                                .getKey());
                                continue; // continue to next while loop iteration
                            } else {
                                /* conjunction is fully satisfied */
                                checkAndAdd(result, conjunctionPostingEntry.get());
                            }
                            /* nextID is the smallest possible ID after current ID */
                            nextID = getNextHigherId(k,
                                    pLists,
                                    links,
                                    pLists[k - 1].getValue()
                                            .getKey());

                        } else {
                            /* Skip first k-1 posting lists */
                            nextID = getNextId(k,
                                    pLists,
                                    links,
                                    pLists[k - 1].getValue()
                                            .getKey(),
                                    nextID);
                        }
                        skipTo(k, pLists, nextID);
                    }
                });
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] getPostingListsDNF(
            final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> table,
            final int k) {
        final Map<Key, TreeSet<ConjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());
        return map.entrySet()
                .stream()
                .map(entry -> getMatchingKey(k, entry))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(x -> x, x -> MutablePair.of(0, map.get(x))))
                .entrySet()
                .stream()
                .toArray(Map.Entry[]::new);
    }

    private Optional<Key> getMatchingKey(final int k, final Entry<Key, TreeSet<ConjunctionPostingEntry>> entry) {
        final Key key = entry.getKey();
        if (key.getValue()
                .equals(query.getAssigment()
                        .getOrDefault(key.getName(), null))
                || (k == 0 && key.getName()
                        .equals(ZERO_SIZE_CONJUNCTION_ENTRY_KEY))) {
            return Optional.of(key);
        }
        return Optional.empty();
    }

    private void initializeCurrentEntriesDNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists) {
        Arrays.stream(pLists)
                .forEach(pList -> pList.getValue()
                        .setLeft(pList.getValue()
                                .getRight()
                                .first()
                                .getIId()));
    }

    private boolean canContinue(final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            final int k) {
        return getConjunctionPostingEntry(pLists[k - 1].getValue()
                .getValue(),
                pLists[k - 1].getValue()
                        .getKey()).isPresent();
    }

    private Optional<ConjunctionPostingEntry> getConjunctionPostingEntry(Set<ConjunctionPostingEntry> set,
            Integer iId) {
        return set.stream()
                .filter(x -> x.getIId()
                        .equals(iId))
                .findFirst();
    }

    private boolean conjunctionRejectionCheck(final Optional<ConjunctionPostingEntry> conjunctionPostingEntry) {
        return !conjunctionPostingEntry.isPresent() || PredicateType.EXCLUDED.equals(conjunctionPostingEntry.get()
                .getType());
    }

    private void sortByCurrentEntriesDNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists) {
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>> idComparator = (e1,
                e2) -> (ObjectUtils.compare(getIdSafely(e1), getIdSafely(e2), true));
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>> typeComparator = (e1,
                e2) -> (ObjectUtils.compare(getTypeSafely(e1), getTypeSafely(e2), true));
        Arrays.sort(pLists, idComparator.thenComparing(typeComparator));
    }

    private Integer getIdSafely(Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>> entry) {
        final Optional<ConjunctionPostingEntry> conjunctionPostingEntry = getConjunctionPostingEntry(entry.getValue()
                .getValue(),
                entry.getValue()
                        .getKey());
        if (conjunctionPostingEntry.isPresent()) {
            return conjunctionPostingEntry.get()
                    .getIId();
        }
        return null;
    }

    private PredicateType getTypeSafely(Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>> entry) {
        final Optional<ConjunctionPostingEntry> conjunctionPostingEntry = getConjunctionPostingEntry(entry.getValue()
                .getValue(),
                entry.getValue()
                        .getKey());
        if (conjunctionPostingEntry.isPresent()) {
            return conjunctionPostingEntry.get()
                    .getType();
        }
        return null;
    }

    private boolean sameConjunctionCheck(
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            Integer k) {
        if (getConjunctionPostingEntry(pLists[0].getValue()
                .getValue(),
                pLists[0].getValue()
                        .getKey()).isPresent()
                && getConjunctionPostingEntry(pLists[k].getValue()
                        .getValue(),
                        pLists[k].getValue()
                                .getKey()).isPresent()) {
            return pLists[0].getValue()
                    .getKey()
                    .equals(pLists[k].getValue()
                            .getKey());
        }
        return false;
    }

    private void conjunctionRejectionSkip(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            final Integer rejectId) {
        IntStream.rangeClosed(0, Math.max(k, pLists.length))
                .boxed()
                .filter(l -> l < pLists.length)
                .filter(l -> pLists[l].getValue()
                        .getKey()
                        .equals(rejectId))
                .forEach(l -> pLists[l].getValue()
                        .setLeft(rejectId + 1));

        preEmptiveSortCheck(k, pLists);
    }

    private void preEmptiveSortCheck(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists) {
        // preemptive sort if possible to continue
        if (!canContinue(pLists, k)) {
            sortByCurrentEntriesDNF(pLists);
        }
    }

    private void checkAndAdd(final Map<String, Double> result, final ConjunctionPostingEntry postingEntry) {
        // Check to see if the current entry is part of criteria's latest version.
        if (invertedIndex.getActiveIds()
                .get(postingEntry.getEId())
                .contains(postingEntry.getIId())) {
            result.put(postingEntry.getEId(), computeScore(postingEntry.getEId()));
        }
    }

    private int getNextHigherId(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            final TreeSet<Integer> links,
            final Integer internalId) {
        final NavigableSet<Integer> nextIds = links.tailSet(internalId, false);
        final Optional<Integer> nextId = nextIds.stream()
                .sequential()
                .map(id -> {
                    skipTo(k, pLists, id);
                    if (canContinue(pLists, k)) {
                        return Optional.of(id);
                    }
                    return Optional.empty();
                })
                .filter(Optional::isPresent)
                .map(o -> (Integer) o.get())
                .findFirst();
        return nextId.orElse(internalId + 1);
    }

    private int getNextId(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            final TreeSet<Integer> links,
            final Integer internalId,
            final int nextId) {
        if (nextId != internalId) {
            return internalId;
        }
        return getNextHigherId(k, pLists, links, internalId);
    }

    private double computeScore(final String cId) {
        return allCriterias.get(cId)
                .getScore(query.getContext());
    }

    private void skipTo(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            int nextID) {
        IntStream.rangeClosed(0, Math.max(k, pLists.length))
                .boxed()
                .filter(l -> l < pLists.length)
                .forEach(l -> pLists[l].getValue()
                        .setLeft(nextID));

        preEmptiveSortCheck(k, pLists);
    }

}
