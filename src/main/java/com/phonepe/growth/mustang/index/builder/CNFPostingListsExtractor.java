package com.phonepe.growth.mustang.index.builder;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
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
    private final Integer iId;
    private final String eId;
    private final int order;

    @Override
    public Map<Key, TreeSet<DisjunctionPostingEntry>> visit(IncludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    @Override
    public Map<Key, TreeSet<DisjunctionPostingEntry>> visit(ExcludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    private Map<Key, TreeSet<DisjunctionPostingEntry>> extractPostingLists(PredicateType pType, String lhs,
            Set<?> values) {
        return values.stream().map(value -> Key.builder().name(lhs).value(value).order(order).build())
                .map(key -> Pair.of(key,
                        DisjunctionPostingEntry.builder().iId(iId).eId(eId).type(pType).order(order).score(0).build()))
                .collect(Collectors.groupingBy(Pair::getKey,
                        Collectors.mapping(Pair::getValue, Collectors.toCollection(TreeSet::new))));
    }
}