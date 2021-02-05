package com.phonepe.growth.mustang.search.matcher;

import static com.phonepe.growth.mustang.index.builder.CriteriaIndexBuilder.ZERO_SIZE_CONJUNCTION_ENTRY_KEY;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
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
import com.phonepe.growth.mustang.index.core.InvertedIndex;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DNFMatcher {

    private final InvertedIndex<ConjunctionPostingEntry> invertedInex;
    private final Query query;
    private final Map<String, Criteria> allCriterias;

    public Map<String, Double> getMatches() {
        final Map<String, Double> result = Maps.newHashMap();
        final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> table = invertedInex.getTable();
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
                                result.put(conjunctionPostingEntry.get()
                                        .getEId(),
                                        computeScore(conjunctionPostingEntry.get()
                                                .getEId()));
                            }
                            /* nextID is the smallest possible ID after current ID */
                            nextID = pLists[k - 1].getValue()
                                    .getKey() + 1;
                        } else {
                            /* Skip first k-1 posting lists */
                            nextID = pLists[k - 1].getValue()
                                    .getKey();
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
        IntStream.rangeClosed(0, k)
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

    private double computeScore(final String cId) {
        return allCriterias.get(cId)
                .getScore(query.getContext());
    }

    private void skipTo(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            int nextID) {
        IntStream.rangeClosed(0, k)
                .boxed()
                .filter(l -> l < pLists.length)
                .forEach(l -> pLists[l].getValue()
                        .setLeft(nextID));

        preEmptiveSortCheck(k, pLists);
    }

}
