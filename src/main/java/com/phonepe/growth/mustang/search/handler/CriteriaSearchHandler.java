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
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriteriaSearchHandler implements CriteriaForm.Visitor<Set<String>> {
    @NotNull
    private final IndexGroup index;
    @Valid
    @NotNull
    private final Query query;

    public Set<String> handle() {
        return Stream.of(visitDNF(), visitCNF()).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<String> visitDNF() {
        // TODO revisit
        final Set<String> result = Sets.newHashSet();
        final Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> table = index.getDnfInvertedIndex().getTable();
        final int start = 0;
        final int end = Math.min(query.getAssigment().size(), table.keySet().stream().mapToInt(x -> x).max().orElse(0));
        IntStream.rangeClosed(start, end).map(i -> end - i + start).boxed().forEach(K -> {
            final Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] PLists = getPostingListsDNF(table,
                    K);
            initializeCurrentEntriesDNF(PLists);
            /* Processing K =0 and K =1 are identical */
            if (K == 0) {
                K = 1;
            }
            if (PLists.length < K) {
                /* Too few posting lists for any conjunction to be satisfied */
                return;
            }
            int NextID = 0;
            while (getConjunctionPostingEntry(PLists[0].getValue().getValue(), PLists[0].getValue().getKey()) != null
                    && getConjunctionPostingEntry(PLists[K - 1].getValue().getValue(),
                            PLists[K - 1].getValue().getKey()) != null) {
                sortByCurrentEntriesDNF(PLists);
                /*
                 * Check if the first K posting lists have the same conjunction ID in their
                 * current entries
                 */
                if (PLists[0].getValue().getKey().equals(PLists[K - 1].getValue().getKey())) {
                    /* Reject conjunction if a ̸∈ predicate is violated */
                    if (PredicateType.EXCLUDED.equals(
                            getConjunctionPostingEntry(PLists[0].getValue().getValue(), PLists[0].getValue().getKey())
                                    .getType())) {
                        final Integer rejectId = PLists[0].getValue().getKey();
                        for (int L = 0; L <= K - 1; L++) {
                            if (PLists[L].getValue().getKey().equals(rejectId)) {
                                /* Skip to smallest ID where ID > RejectID */
                                PLists[L].getValue().setLeft(rejectId + 1);
                            } else {
                                break; // break out of this for loop
                            }
                        }
                        continue; // continue to next while loop iteration
                    } else {
                        /* conjunction is fully satisfied */
                        result.add(getConjunctionPostingEntry(PLists[0].getValue().getValue(),
                                PLists[0].getValue().getKey()).getEId());
                    }
                    /* NextID is the smallest possible ID after current ID */
                    NextID = PLists[K - 1].getValue().getKey() + 1;
                } else {
                    /* Skip first K-1 posting lists */
                    NextID = PLists[K - 1].getValue().getKey();
                }
                for (int L = 0; L <= K - 1; L++) {
                    PLists[L].getValue().setLeft(NextID);
                }
            }
        });

        return result;
    }

    @Override
    public Set<String> visitCNF() {
        // TODO revisit
        final Set<String> result = Sets.newHashSet();
        final Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> table = index.getCnfInvertedIndex().getTable();
        final Map<Integer, Integer[]> disjunctionCounters = ((CNFInvertedIndex<DisjunctionPostingEntry>) index
                .getCnfInvertedIndex()).getDisjunctionCounters();
        final int start = 0;
        final int end = table.keySet().stream().mapToInt(x -> x).max().orElse(0);
        IntStream.rangeClosed(start, end).map(i -> end - i + start).boxed().forEach(K -> {
            final Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] PLists = getPostingListsCNF(table,
                    K);
            initializeCurrentEntriesCNF(PLists);
            /* Processing K =0 and K =1 are identical */
            if (K == 0) {
                K = 1;
            }
            if (PLists.length < K) {
                /* Too few posting lists for any conjunction to be satisfied */
                return;
            }
            int NextID = 0;
            while (getDisjunctionPostingEntry(PLists[0].getValue().getValue(), PLists[0].getValue().getKey()) != null
                    && getDisjunctionPostingEntry(PLists[K - 1].getValue().getValue(),
                            PLists[K - 1].getValue().getKey()) != null) {
                sortByCurrentEntriesCNF(PLists);
                /*
                 * Check if the first K posting lists have the same conjunction ID in their
                 * current entries
                 */
                if (PLists[0].getValue().getKey().equals(PLists[K - 1].getValue().getKey())) {
                    /*
                     * For each disjunction in the current CNF, one counter is initialized to the
                     * negative number of ̸∈ predicates
                     */
                    final Integer[] counters = getCounters(disjunctionCounters, PLists[0].getValue().getKey());

                    for (int L = 0; L < PLists.length; L++) {
                        if (PLists[L].getValue().getKey().equals(PLists[0].getValue().getKey())) {
                            /* Ignore entries in the Z posting list */
                            if (getDisjunctionPostingEntry(PLists[L].getValue().getValue(),
                                    PLists[L].getValue().getKey()).getOrder() == -1) {
                                continue;
                            }
                            if (PredicateType.EXCLUDED
                                    .equals(getDisjunctionPostingEntry(PLists[L].getValue().getValue(),
                                            PLists[L].getValue().getKey()).getType())) {
                                counters[getDisjunctionPostingEntry(PLists[L].getValue().getValue(),
                                        PLists[L].getValue().getKey()).getOrder()]++;
                            } else {
                                /* Disjunction is satisfied */
                                counters[getDisjunctionPostingEntry(PLists[L].getValue().getValue(),
                                        PLists[L].getValue().getKey()).getOrder()] = 1;
                            }
                        } else {
                            break;
                        }
                    }
                    if (Arrays.stream(counters).allMatch(i -> i != 0)) {
                        result.add(getDisjunctionPostingEntry(PLists[K - 1].getValue().getValue(),
                                PLists[K - 1].getValue().getKey()).getEId());
                    }
                    /* NextID is the smallest possible ID after current ID */
                    NextID = PLists[K - 1].getValue().getKey() + 1;
                } else {
                    /* Skip first K-1 posting lists */
                    NextID = PLists[K - 1].getValue().getKey();
                }
                for (int L = 0; L <= K - 1; L++) {
                    PLists[L].getValue().setLeft(NextID);
                }
            }
        });

        return result;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] getPostingListsDNF(
            Map<Integer, Map<Key, TreeSet<ConjunctionPostingEntry>>> table, int k) {
        final Map<Key, TreeSet<ConjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());

        final Map.Entry[] array = query.getAssigment().entrySet().stream()
                .map(entry -> Key.builder().name(entry.getKey()).value(entry.getValue()).build())
                .filter(key -> map.containsKey(key)).collect(Collectors.toMap(x -> x,
                        x -> MutablePair.of(0, map.get(x)), (oldValue, newValue) -> newValue, LinkedHashMap::new))
                .entrySet().stream().toArray(Map.Entry[]::new);
        return ((Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[]) array);
    }

    private void sortByCurrentEntriesDNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<ConjunctionPostingEntry>>>[] pLists) {
        // TODO Fix NPE
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] getPostingListsCNF(
            Map<Integer, Map<Key, TreeSet<DisjunctionPostingEntry>>> table, int k) {
        final Map<Key, TreeSet<DisjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());

        final Map.Entry[] array = query.getAssigment().entrySet().stream()
                .map(entry -> Key.builder().name(entry.getKey()).value(entry.getValue()).build())
                .filter(key -> map.containsKey(key)).collect(Collectors.toMap(x -> x,
                        x -> MutablePair.of(0, map.get(x)), (oldValue, newValue) -> newValue, LinkedHashMap::new))
                .entrySet().stream().toArray(Map.Entry[]::new);
        return ((Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[]) array);
    }

    private void sortByCurrentEntriesCNF(
            Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>[] pLists) {
        // TODO Fix NPE
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>> idComparator = (e1,
                e2) -> (e1.getValue().getKey().compareTo(e2.getValue().getKey()));
        final Comparator<Map.Entry<Key, MutablePair<Integer, TreeSet<DisjunctionPostingEntry>>>> typeComparator = (e1,
                e2) -> (getDisjunctionPostingEntry(e1.getValue().getValue(), e1.getValue().getKey()).getType()
                        .compareTo(getDisjunctionPostingEntry(e2.getValue().getValue(), e2.getValue().getKey())
                                .getType()));
        Arrays.sort(pLists, Comparator.nullsLast(idComparator.thenComparing(typeComparator.reversed())));
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
