package com.phonepe.growth.mustang.search.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.InvertedIndex;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import static com.phonepe.growth.mustang.index.builder.CriteriaIndexBuilder.ZERO_SIZE_DISJUNCTION_ENTRY_KEY;

@Data
@Builder
@AllArgsConstructor
public class CNFMatcher {
    private final InvertedIndex<DisjunctionPostingEntry> invertedIndex;
    private final Query query;

    public Set<String> getMatches() {
        final Set<String> result = Sets.newHashSet();
        final Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> table = invertedIndex.getTable();
        final Map<Integer, Integer[]> disjunctionCounters = ((CNFInvertedIndex<DisjunctionPostingEntry>) invertedIndex)
                .getDisjunctionCounters();
        final int start = 0;
        final int end = table.keySet().stream().mapToInt(x -> x).max().orElse(0);
        IntStream.rangeClosed(start, end).map(i -> end - i + start).boxed().forEach(k -> {
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists = getPostingListsCNF(
                    table, k);
            initializeCurrentEntriesCNF(pLists);
            /* Processing K =0 and K =1 are identical */
            if (k == 0) {
                k = 1;
            }
            if (pLists.length < k) {
                /* Too few posting lis ts for any conjunction to be satisfied */
                return;
            }
            int nextID = 0;
            while (canContinue(pLists, k)) {
                sortByCurrentEntriesCNF(pLists);
                /*
                 * Check if the first K posting lists have the same conjunction ID in their
                 * current entries
                 */
                if (sameConjunctionCheck(k, pLists)) {
                    /*
                     * For each disjunction in the current CNF, one counter is initialized to the
                     * negative number of EXCLUDED predicates
                     */
                    final Integer[] counters = getCounters(disjunctionCounters, pLists[0].getValue().getKey());

                    disjunctionEvaluationCheck(result, pLists, k, counters);

                    /* NextID is the smallest possible ID after current ID */
                    nextID = pLists[k - 1].getValue().getKey() + 1;
                } else {
                    /* Skip first K-1 posting lists */
                    nextID = pLists[k - 1].getValue().getKey();
                }
                skipTo(k, pLists, nextID);
                if (!canContinue(pLists, k)) {
                    sortByCurrentEntriesCNF(pLists);
                }
            }
        });

        return result;

    }

    private boolean sameConjunctionCheck(Integer k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists) {
        if (Objects.nonNull(getDisjunctionPostingEntry(pLists[0].getValue().getValue(), pLists[0].getValue().getKey()))
                && Objects.nonNull(getDisjunctionPostingEntry(pLists[k - 1].getValue().getValue(),
                        pLists[k - 1].getValue().getKey()))) {
            return pLists[0].getValue().getKey().equals(pLists[k - 1].getValue().getKey());
        }
        return false;
    }

    private void skipTo(final int k,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists, final int nextID) {
        IntStream.rangeClosed(0, k).boxed().forEach(l -> {
            if (pLists.length > l) {
                pLists[l].getValue().setLeft(nextID);
            }
        });
    }

    private void disjunctionEvaluationCheck(final Set<String> result,
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists, final Integer k,
            final Integer[] counters) {
        for (int l = 0; l < pLists.length; l++) {
            if (pLists[l].getValue().getKey().equals(pLists[0].getValue().getKey())) {
                /* Ignore entries in the Z posting list */
                final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(
                        pLists[l].getValue().getValue(), pLists[l].getValue().getKey());
                if (Objects.isNull(disjunctionPostingEntry) || disjunctionPostingEntry.getOrder() == -1) {
                    continue;
                }
                if (PredicateType.EXCLUDED.equals(disjunctionPostingEntry.getType())) {
                    counters[disjunctionPostingEntry.getOrder()]++;
                } else {
                    /* Disjunction is satisfied */
                    counters[disjunctionPostingEntry.getOrder()] = 1;
                }
            } else {
                break;
            }
        }
        if (Arrays.stream(counters).allMatch(i -> i != 0)) {
            final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(
                    pLists[k - 1].getValue().getValue(), pLists[k - 1].getValue().getKey());
            if (Objects.nonNull(disjunctionPostingEntry)) {
                result.add(disjunctionPostingEntry.getEId());
            }
        }
        if (!canContinue(pLists, k)) {
            sortByCurrentEntriesCNF(pLists);
        }
    }

    private boolean canContinue(final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists,
            final int k) {
        return getDisjunctionPostingEntry(pLists[k - 1].getValue().getValue(),
                pLists[k - 1].getValue().getKey()) != null;
    }

    @SuppressWarnings("unchecked")
    private Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] getPostingListsCNF(
            Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> table, int k) {
        final Map<Key, TreeSet<DisjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());

        return map.entrySet().stream().map(entry -> {
            final Key key = entry.getKey();
            if (key.getValue().equals(query.getAssigment().getOrDefault(key.getName(), null))
                    || (k == 0 && key.getName().equals(ZERO_SIZE_DISJUNCTION_ENTRY_KEY))) {
                return key;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toMap(x -> x, x -> MutablePair.of(0, map.get(x)),
                (oldValue, newValue) -> newValue, LinkedHashMap::new)).entrySet().stream().toArray(Map.Entry[]::new);
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
        final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(e1.getValue().getValue(),
                e1.getValue().getKey());
        if (Objects.nonNull(disjunctionPostingEntry)) {
            return disjunctionPostingEntry.getIId();
        }
        return null;
    }

    private PredicateType getTypeSafely(Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>> e1) {
        final DisjunctionPostingEntry disjunctionPostingEntry = getDisjunctionPostingEntry(e1.getValue().getValue(),
                e1.getValue().getKey());
        if (Objects.nonNull(disjunctionPostingEntry)) {
            return disjunctionPostingEntry.getType();
        }
        return null;
    }

    private void initializeCurrentEntriesCNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists) {
        Arrays.stream(pLists).forEach(pList -> pList.getValue().setLeft(pList.getValue().getRight().first().getIId()));
    }

    private DisjunctionPostingEntry getDisjunctionPostingEntry(Collection<DisjunctionPostingEntry> set, Integer iId) {
        return set.stream().filter(x -> x.getIId().equals(iId)).findFirst().orElse(null);

    }

    private Integer[] getCounters(final Map<Integer, Integer[]> disjunctionCounters, int iId) {
        return Arrays.stream(disjunctionCounters.get(iId)).map(x -> -1 * x).toArray(Integer[]::new);
    }

}
