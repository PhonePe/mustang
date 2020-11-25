package com.phonepe.growth.mustang.index.extractor;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.predicate.PredicateVisitor;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CNFPostingListsExtractor implements PredicateVisitor<Map<Key, TreeSet<DisjunctionPostingEntry>>> {
    private static final Comparator<Key> KEY_ORDER_COMPARATOR = (k1, k2) -> Integer.valueOf(k1.getOrder())
            .compareTo(k2.getOrder());
    private final Integer iId;
    private final String eId;
    private final int order;
    private final Map<Key, TreeSet<DisjunctionPostingEntry>> postingLists;
    private final Map<Key, AtomicInteger> cnfKeyFrequency;

    @Override
    public Map<Key, TreeSet<DisjunctionPostingEntry>> visit(IncludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    @Override
    public Map<Key, TreeSet<DisjunctionPostingEntry>> visit(ExcludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    private Map<Key, TreeSet<DisjunctionPostingEntry>> extractPostingLists(PredicateType pType,
            String lhs,
            Set<?> values) {
        final DisjunctionPostingEntry postingEntry = DisjunctionPostingEntry.builder()
                .iId(iId)
                .eId(eId)
                .type(pType)
                .order(order)
                .score(0)
                .build();
        return values.stream()
                .map(value -> {
                    final Set<Key> keys = postingLists.keySet()
                            .stream()
                            .filter(key -> key.getName()
                                    .equals(lhs)
                                    && key.getValue()
                                            .equals(value))
                            .sorted(KEY_ORDER_COMPARATOR)
                            .collect(Collectors.toSet());
                    if (keys.isEmpty()) {
                        return Key.builder()
                                .name(lhs)
                                .value(value)
                                .order(0)
                                .build();
                    }
                    final AtomicInteger counter = new AtomicInteger(0);
                    return keys.stream()
                            .sequential()
                            .filter(key -> {
                                counter.incrementAndGet();
                                return !postingLists.get(key)
                                        .contains(postingEntry);
                            })
                            .findFirst()
                            .orElse(Key.builder()
                                    .name(lhs)
                                    .value(value)
                                    .order(counter.get())
                                    .build());
                })
                .map(key -> {
                    final Key baseKey = Key.builder()
                            .name(key.getName())
                            .value(key.getValue())
                            .build();
                    cnfKeyFrequency.computeIfAbsent(baseKey, x -> new AtomicInteger())
                            .getAndIncrement();
                    return key;
                })
                .map(key -> Pair.of(key, postingEntry))
                .collect(Collectors.groupingBy(Pair::getKey,
                        Collectors.mapping(Pair::getValue, Collectors.toCollection(TreeSet::new))));
    }
}