package com.phonepe.growth.mustang.search.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Sets;
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

    public Set<String> getMatches() {
        final Set<String> result = Sets.newHashSet();
        final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> table = invertedInex.getTable();
        final int start = 0;
        final int end = Math.min(query.getAssigment().size(), table.keySet().stream().mapToInt(x -> x).max().orElse(0));
        IntStream.rangeClosed(start, end).map(i -> end - i + start).boxed().forEach(k -> {
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists = getPostingListsDNF(
                    table, k);
            initializeCurrentEntriesDNF(pLists);
            /* Processing K =0 and K =1 are identical */
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
                 * Check if the first K posting lists have the same conjunction ID in their
                 * current entries
                 */
                if (pLists[0].getValue().getKey().equals(pLists[k - 1].getValue().getKey())) {
                    /* Reject conjunction if EXCLUDED predicate is violated */
                    if (PredicateType.EXCLUDED.equals(
                            getConjunctionPostingEntry(pLists[0].getValue().getValue(), pLists[0].getValue().getKey())
                                    .getType())) {
                        conjunctionRejectionCheck(k, pLists, pLists[0].getValue().getKey());

                        continue; // continue to next while loop iteration
                    } else {
                        /* conjunction is fully satisfied */
                        result.add(getConjunctionPostingEntry(pLists[0].getValue().getValue(),
                                pLists[0].getValue().getKey()).getEId());
                    }
                    /* NextID is the smallest possible ID after current ID */
                    nextID = pLists[k - 1].getValue().getKey() + 1;
                } else {
                    /* Skip first K-1 posting lists */
                    nextID = pLists[k - 1].getValue().getKey();
                }
                skipTo(k, pLists, nextID);
            }
        });

        return result;

    }

    private void skipTo(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists, int nextID) {
        IntStream.range(0, k).boxed().forEach(l -> pLists[l].getValue().setLeft(nextID));
    }

    private void conjunctionRejectionCheck(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            final Integer rejectId) {
        for (int l = 0; l <= k - 1; l++) {
            if (pLists[l].getValue().getKey().equals(rejectId)) {
                /* Skip to smallest ID where ID > RejectID */
                pLists[l].getValue().setLeft(rejectId + 1);
            } else {
                break; // break out of this for loop
            }
        }
    }

    private boolean canContinue(final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists,
            final int k) {
        return getConjunctionPostingEntry(pLists[0].getValue().getValue(), pLists[0].getValue().getKey()) != null
                && getConjunctionPostingEntry(pLists[k - 1].getValue().getValue(),
                        pLists[k - 1].getValue().getKey()) != null;
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] getPostingListsDNF(
            final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> table, final int k) {
        final Map<Key, TreeSet<ConjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());

        return query.getAssigment().entrySet().stream()
                .map(entry -> Key.builder().name(entry.getKey()).value(entry.getValue()).build())
                .filter(map::containsKey).collect(Collectors.toMap(x -> x, x -> MutablePair.of(0, map.get(x)),
                        (oldValue, newValue) -> newValue, LinkedHashMap::new))
                .entrySet().stream().toArray(Map.Entry[]::new);
    }

    private void sortByCurrentEntriesDNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists) {
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>> idComparator = (e1,
                e2) -> (e1.getValue().getKey().compareTo(e2.getValue().getKey()));
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>> typeComparator = (e1,
                e2) -> (getConjunctionPostingEntry(e1.getValue().getValue(), e1.getValue().getKey()).getType()
                        .compareTo(getConjunctionPostingEntry(e2.getValue().getValue(), e2.getValue().getKey())
                                .getType()));
        Arrays.sort(pLists, Comparator.nullsLast(idComparator.thenComparing(typeComparator.reversed())));
    }

    private void initializeCurrentEntriesDNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists) {
        Arrays.stream(pLists).forEach(pList -> pList.getValue().setLeft(pList.getValue().getRight().first().getIId()));
    }

    private ConjunctionPostingEntry getConjunctionPostingEntry(Collection<ConjunctionPostingEntry> set, Integer iId) {
        return set.stream().filter(x -> x.getIId().equals(iId)).findFirst().orElse(null);

    }

}
