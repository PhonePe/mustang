package com.phonepe.growth.mustang.index.util;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.validator.constraints.NotBlank;

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
public class CnfPredicateVisitorImpl implements PredicateVisitor<Map<Key, Set<DisjunctionPostingEntry>>> {
    @NotBlank
    private final String id;
    private final int order;

    @Override
    public Map<Key, Set<DisjunctionPostingEntry>> visit(IncludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    @Override
    public Map<Key, Set<DisjunctionPostingEntry>> visit(ExcludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    private Map<Key, Set<DisjunctionPostingEntry>> extractPostingLists(PredicateType predicateType, String lhs,
            Set<?> values) {
        return values.stream().map(value -> Key.builder().name(lhs).value(value).build()).map(key -> {
            final DisjunctionPostingEntry postingEntry = DisjunctionPostingEntry.builder().id(id)
                    .predicateType(predicateType).order(order).score(0).build();
            return Pair.of(key, postingEntry);
        }).collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet())));
    }
}