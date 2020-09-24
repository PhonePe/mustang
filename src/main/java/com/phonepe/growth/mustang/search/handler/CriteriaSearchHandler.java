package com.phonepe.growth.mustang.search.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriteriaSearchHandler implements CriteriaForm.Visitor<List<String>> {
    @NotNull
    private final IndexGroup index;
    @Valid
    @NotNull
    private final Query query;

    public List<String> handle() {
        return Stream.of(visitDNF(), visitCNF()).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<String> visitDNF() {
        // TODO revisit
        final List<String> result = Lists.newArrayList();
        final Map<Integer, Map<Key, Set<ConjunctionPostingEntry>>> table = index.getDnfInvertedIndex().getTable();
        final int start = 0, end = Math.min(query.getAssigment().size(), table.size() - 1);
        IntStream.range(start, end).map(i -> end - i + start - 1).boxed().forEach(K -> {
            Map.Entry<Key, MutablePair<Integer, List<ConjunctionPostingEntry>>>[] PLists = getPostingLists(table, K);
            /* Processing K =0 and K =1 are identical */
            if (K == 0) {
                K = 1;
            }
            if (PLists.length < K) {
                /* Too few posting lists for any conjunction to be satisfied */
                return;
            }
            int NextID = 0;
            while (PLists[K - 1].getValue().getValue().size() > PLists[K - 1].getValue().getKey()) {
                sortByCurrentEntries(PLists);
                /*
                 * Check if the first K posting lists have the same conjunction ID in their
                 * current entries
                 */
                if (PLists[0].getValue().getValue().get(PLists[0].getValue().getKey()).getIId()
                        .equals(PLists[K - 1].getValue().getValue().get(PLists[K - 1].getValue().getKey()).getIId())) {
                    /* Reject conjunction if a ̸∈ predicate is violated */
                    if (PredicateType.EXCLUDED
                            .equals(PLists[0].getValue().getValue().get(PLists[0].getValue().getKey()).getType())) {
                        final Integer rejectId = PLists[0].getValue().getValue().get(PLists[0].getValue().getKey())
                                .getIId();
                        for (int L = K; L < PLists.length - 1; L--) {
                            if (PLists[0].getValue().getValue().get(PLists[0].getValue().getKey()).getIId()
                                    .equals(rejectId)) {
                                /* Skip to smallest ID where ID > RejectID */
                                PLists[L].getValue().setLeft(rejectId + 1);
                            } else {
                                break; // break out of this for loop
                            }
                        }
                        continue; // continue to next while loop iteration
                    } else {
                        /* conjunction is fully satisfied */
                        result.add(PLists[K - 1].getValue().getValue().get(PLists[K - 1].getValue().getKey()).getEId());
                    }
                    /* NextID is the smallest possible ID after current ID */
                    NextID = PLists[K - 1].getValue().getValue().get(PLists[K - 1].getValue().getKey()).getIId() + 1;
                } else {
                    /* Skip first K-1 posting lists */
                    NextID = PLists[K - 1].getValue().getValue().get(PLists[K - 1].getValue().getKey()).getIId();
                }
                for (int L = 0; L < K - 1; L++) {
                    PLists[L].getValue().setLeft(NextID);
                }
            }

        });

        return result;
    }

    @Override
    public List<String> visitCNF() {
        // TODO implement
//        Map<Integer, Map<Key, Set<DisjunctionPostingEntry>>> table = index.getCnfInvertedIndex().getTable();
        return Collections.emptyList();
    }

//    @SuppressWarnings("unchecked")
//    private Map.Entry<Key, MutablePair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>>[] getPostingLists(
//            Map<Integer, Map<Key, Set<ConjunctionPostingEntry>>> table, int k) {
//        final Map<Key, Set<ConjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());
//        final LinkedHashMap<Key, Pair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>> collect = query
//                .getAssigment().entrySet().stream()
//                .map(entry -> Key.builder().name(entry.getKey()).value(entry.getValue()).build())
//                .filter(key -> map.containsKey(key))
//                .collect(Collectors.toMap(key -> key,
//                        key -> Pair.of(map.get(key).toArray(new ConjunctionPostingEntry[0])[0], map.get(key)),
//                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//        return (Map.Entry<Key, MutablePair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>>[]) collect.entrySet()
//                .toArray();
//    }

    @SuppressWarnings("unchecked")
    private Map.Entry<Key, MutablePair<Integer, List<ConjunctionPostingEntry>>>[] getPostingLists(
            Map<Integer, Map<Key, Set<ConjunctionPostingEntry>>> table, int k) {
        final Map<Key, Set<ConjunctionPostingEntry>> map = table.getOrDefault(k, Collections.emptyMap());
        final LinkedHashMap<Key, Pair<Integer, List<ConjunctionPostingEntry>>> collect = query.getAssigment().entrySet()
                .stream().map(entry -> Key.builder().name(entry.getKey()).value(entry.getValue()).build())
                .filter(key -> map.containsKey(key))
                .map(key -> Pair.of(key, new ArrayList<ConjunctionPostingEntry>(map.get(key))))
                .collect(Collectors.toMap(Pair::getKey, x -> MutablePair.of(0, x.getValue()),
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return (Map.Entry<Key, MutablePair<Integer, List<ConjunctionPostingEntry>>>[]) collect.entrySet().toArray();
    }

//    private Map<Key, Pair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>> sortByCurrentEntries(
//            Map<Key, Pair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>> PLists) {
//        Comparator<Pair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>> idComparator = (e1, e2) -> e1.getKey()
//                .getId().compareTo(e2.getKey().getId());
//        Comparator<Pair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>> predicateTypeComparator = (e1, e2) -> e1
//                .getKey().getPredicateType().compareTo(e2.getKey().getPredicateType());
//        return PLists.entrySet().stream()
//                .sorted(Map.Entry.<Key, Pair<ConjunctionPostingEntry, Set<ConjunctionPostingEntry>>>comparingByValue(
//                        idComparator.thenComparing(predicateTypeComparator.reversed())))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
//                        LinkedHashMap::new));
//    }

    private void sortByCurrentEntries(Map.Entry<Key, MutablePair<Integer, List<ConjunctionPostingEntry>>>[] PLists) {
        final Comparator<Map.Entry<Key, MutablePair<Integer, List<ConjunctionPostingEntry>>>> idComparator = (e1,
                e2) -> (e1.getValue().getValue().get(e1.getValue().getKey()).getIId()
                        .compareTo(e2.getValue().getValue().get(e2.getValue().getKey()).getIId()));
        final Comparator<Map.Entry<Key, MutablePair<Integer, List<ConjunctionPostingEntry>>>> typeComparator = (e1,
                e2) -> (e1.getValue().getValue().get(e1.getValue().getKey()).getType()
                        .compareTo(e2.getValue().getValue().get(e1.getValue().getKey()).getType()));
        Arrays.sort(PLists, Comparator.nullsLast(idComparator.thenComparing(typeComparator.reversed())));
    }

}
