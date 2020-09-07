package com.phonepe.growth.mustang.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.validator.constraints.NotBlank;

import com.phonepe.growth.mustang.criteria.CriteriaVisitor;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.index.dnf.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.predicate.PredicateVisitor;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaIndexer implements CriteriaVisitor<Void> {
    private static final class PredicatorVisitorImpl
            implements PredicateVisitor<Map<Key, Set<ConjunctionPostingEntry>>> {
        private String cId;

        public PredicatorVisitorImpl(String cId) {
            this.cId = cId;
        }

        @Override
        public Map<Key, Set<ConjunctionPostingEntry>> visit(IncludedPredicate predicate) {
            return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
        }

        @Override
        public Map<Key, Set<ConjunctionPostingEntry>> visit(ExcludedPredicate predicate) {
            return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
        }

        private Map<Key, Set<ConjunctionPostingEntry>> extractPostingLists(PredicateType predicateType, String lhs,
                Set<?> values) {
            return values.stream().map(value -> Key.builder().name(lhs).value(value).build()).map(key -> {
                final ConjunctionPostingEntry postingEntry = ConjunctionPostingEntry.builder().id(cId)
                        .predicateType(predicateType).score(0).build();
                return Pair.of(key, postingEntry);
            }).collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet())));
        }
    }

    @Valid
    @NotNull
    private IndexGroup indexGroup;
    @NotBlank
    private String cId;

    @Override
    public Void visit(DNFCriteria dnf) {
        dnf.getConjunctions().forEach(conjunction -> {
            final Map<Integer, Map<Key, Set<ConjunctionPostingEntry>>> indexTable = indexGroup.getDnf().getIndexTable();
            final int kSize = conjunction.getPredicates().stream()
                    .filter(predicate -> PredicateType.INCLUDED.equals(predicate.getType())).mapToInt(e -> 1).sum();

            final List<Map<Key, Set<ConjunctionPostingEntry>>> postingLists = IntStream
                    .range(0, conjunction.getPredicates().size()).boxed().map(i -> {
                        final Predicate predicate = conjunction.getPredicates().get(i);
                        return predicate.accept(new PredicatorVisitorImpl(cId + "#" + i));
                    }).collect(Collectors.toList());
            if (kSize == 0) {
                // TODO zero size handling
            }
            
            postingLists.add(indexTable.getOrDefault(kSize, Collections.emptyMap()));
            indexTable.put(kSize, compactDNFPostingLists(postingLists));

        });
        return null;
    }

    @Override
    public Void visit(CNFCriteria cnf) {
        // TODO Auto-generated method stub
        return null;
    }

    private <T, S> Map<T, Set<S>> compactDNFPostingLists(List<Map<T, Set<S>>> maps) {
        final List<Map.Entry<T, Set<S>>> tempResult = maps.stream().collect(() -> new ArrayList<>(),
                (set, map) -> set.addAll(map.entrySet()), (set1, set2) -> set1.addAll(set2));
        return tempResult.stream().collect(Collectors.groupingBy(Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.reducing(Collections.emptySet(), (set1, set2) -> {
                    final Set<S> combined = new TreeSet<>(set1);
                    combined.addAll(set2);
                    return combined;
                }))));
    }

}