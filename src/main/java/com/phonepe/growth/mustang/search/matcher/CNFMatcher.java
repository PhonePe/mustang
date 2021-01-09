package com.phonepe.growth.mustang.search.matcher;

import static com.phonepe.growth.mustang.index.builder.CriteriaIndexBuilder.ZERO_SIZE_DISJUNCTION_ENTRY_KEY;

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
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.InvertedIndex;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CNFMatcher {
    private final InvertedIndex<DisjunctionPostingEntry> invertedIndex;
    private final Query query;
    private final Map<String, Criteria> allCriterias;

    public Map<String, Double> getMatches() {
        final Map<String, Double> result = Maps.newHashMap();
        final Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> table = invertedIndex.getTable();
        final Map<Integer, Integer[]> disjunctionCounters = ((CNFInvertedIndex<DisjunctionPostingEntry>) invertedIndex)
                .getDisjunctionCounters();
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
                    final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists = getPostingListsCNF(
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
                        sortByCurrentEntriesCNF(pLists);
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
    private Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] getPostingListsCNF(
            Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> table,
            int k) {
        final Map<Key, TreeSet<DisjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());
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

    private Optional<Key> getMatchingKey(int k, Entry<Key, TreeSet<DisjunctionPostingEntry>> entry) {
        final Key key = entry.getKey();
        if (key.getValue()
                .equals(query.getAssigment()
                        .getOrDefault(key.getName(), null))
                || (k == 0 && key.getName()
                        .equals(ZERO_SIZE_DISJUNCTION_ENTRY_KEY))) {
            return Optional.of(key);
        }
        return Optional.empty();
    }

    private void initializeCurrentEntriesCNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists) {
        Arrays.stream(pLists)
                .forEach(pList -> pList.getValue()
                        .setLeft(pList.getValue()
                                .getRight()
                                .first()
                                .getIId()));
    }

    private boolean canContinue(final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists,
            final int k) {
        return getDisjunctionPostingEntry(pLists[k - 1].getValue()
                .getValue(),
                pLists[k - 1].getValue()
                        .getKey()).isPresent();
    }

    private Optional<DisjunctionPostingEntry> getDisjunctionPostingEntry(Set<DisjunctionPostingEntry> set,
            Integer iId) {
        return set.stream()
                .filter(x -> x.getIId()
                        .equals(iId))
                .findFirst();

    }

    private void sortByCurrentEntriesCNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists) {
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>> idComparator = (e1,
                e2) -> (ObjectUtils.compare(getIdSafely(e1), getIdSafely(e2), true));
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>> typeComparator = (e1,
                e2) -> (ObjectUtils.compare(getTypeSafely(e1), getTypeSafely(e2), true));
        Arrays.sort(pLists, idComparator.thenComparing(typeComparator));
    }

    private Integer getIdSafely(Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>> e1) {
        final Optional<DisjunctionPostingEntry> disjunctionPostingEntry = getDisjunctionPostingEntry(e1.getValue()
                .getValue(),
                e1.getValue()
                        .getKey());
        return disjunctionPostingEntry.isPresent() ? disjunctionPostingEntry.get()
                .getIId() : null;
    }

    private PredicateType getTypeSafely(Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>> e1) {
        final Optional<DisjunctionPostingEntry> disjunctionPostingEntry = getDisjunctionPostingEntry(e1.getValue()
                .getValue(),
                e1.getValue()
                        .getKey());
        return disjunctionPostingEntry.isPresent() ? disjunctionPostingEntry.get()
                .getType() : null;
    }

    private boolean sameConjunctionCheck(
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists,
            Integer k) {
        if (getDisjunctionPostingEntry(pLists[0].getValue()
                .getValue(),
                pLists[0].getValue()
                        .getKey()).isPresent()
                && getDisjunctionPostingEntry(pLists[k].getValue()
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

    private void disjunctionEvaluationCheck(final Map<String, Double> result,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists,
            final Integer k,
            final Integer[] counters) {
        for (int l = 0; ((l < pLists.length) && sameConjunctionCheck(pLists, l)); l++) {
            /* Ignore entries in the Z posting list */
            final Optional<DisjunctionPostingEntry> disjunctionPostingEntry = getDisjunctionPostingEntry(
                    pLists[l].getValue()
                            .getValue(),
                    pLists[l].getValue()
                            .getKey());
            if (!disjunctionPostingEntry.isPresent() || disjunctionPostingEntry.get()
                    .getOrder() == -1) {
                continue;
            }
            if (PredicateType.EXCLUDED.equals(disjunctionPostingEntry.get()
                    .getType())) {
                counters[disjunctionPostingEntry.get()
                        .getOrder()]++;
            } else {
                /* Disjunction is satisfied */
                counters[disjunctionPostingEntry.get()
                        .getOrder()] = 1;
            }
        }
        if (Arrays.stream(counters)
                .allMatch(i -> i != 0)) {
            final Optional<DisjunctionPostingEntry> disjunctionPostingEntryOptional = getDisjunctionPostingEntry(
                    pLists[k - 1].getValue()
                            .getValue(),
                    pLists[k - 1].getValue()
                            .getKey());
            disjunctionPostingEntryOptional
                    .ifPresent(postingEntry -> result.put(postingEntry.getEId(), computeScore(postingEntry.getEId())));
        }
        preEmptiveSortCheck(pLists, k);
    }

    private double computeScore(final String cId) {
        return allCriterias.get(cId)
                .getScore(query.getContext());
    }

    private void preEmptiveSortCheck(
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists,
            final Integer k) {
        // preemptive sort if possible to continue
        if (!canContinue(pLists, k)) {
            sortByCurrentEntriesCNF(pLists);
        }
    }

    private Integer[] getCounters(final Map<Integer, Integer[]> disjunctionCounters, int iId) {
        return Arrays.stream(disjunctionCounters.get(iId))
                .map(x -> -1 * x)
                .toArray(Integer[]::new);
    }

    private void skipTo(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists,
            final int nextID) {
        IntStream.rangeClosed(0, k)
                .boxed()
                .filter(l -> l < pLists.length)
                .forEach(l -> pLists[l].getValue()
                        .setLeft(nextID));
        preEmptiveSortCheck(pLists, k);
    }

}
