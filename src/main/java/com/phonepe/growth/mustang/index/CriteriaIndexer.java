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

import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.CriteriaVisitor;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.index.dnf.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.cnf.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.predicate.PredicateVisitor;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
//TODO refac
public class CriteriaIndexer implements CriteriaVisitor<Void> {
    private static final String CONJUNCTION_ENTRY_ID_FORMAT = "%s#%s";
    private static final String ZERO_SIZE_CONJUNCTION_ENTRY_KEY = "ZZZ";
    private static final String ZERO_SIZE_DISJUNCTION_ENTRY_KEY = "ZZZ";
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
                        return conjunction.getPredicates().get(i).accept(
                                new DnfPredicatorVisitorImpl(String.format(CONJUNCTION_ENTRY_ID_FORMAT, cId, i)));
                    }).collect(Collectors.toList());
            if (kSize == 0) {
                final Key key = Key.builder().name(ZERO_SIZE_CONJUNCTION_ENTRY_KEY).value(0).upperBoundScore(0).build();
                final Map<Key, Set<ConjunctionPostingEntry>> map = postingLists.stream()
                        .flatMap(m -> m.entrySet().stream()).map(Map.Entry::getValue).flatMap(m -> m.stream())
                        .distinct()
                        .map(entry -> ConjunctionPostingEntry.builder().id(entry.getId())
                                .predicateType(PredicateType.INCLUDED).score(0).build())
                        .distinct().map(entry -> Pair.of(key, entry)).collect(Collectors.groupingBy(Pair::getKey,
                                Collectors.mapping(Pair::getValue, Collectors.toSet())));
                postingLists.add(map);
            }

            postingLists.add(indexTable.getOrDefault(kSize, Collections.emptyMap()));
            indexTable.put(kSize, compactPostingLists(postingLists));

        });
        return null;
    }

    @Override
    public Void visit(CNFCriteria cnf) {
        final Map<Integer, Map<Key, Set<DisjunctionPostingEntry>>> indexTable = indexGroup.getCnf().getIndexTable();
        final int kSize = cnf.getDisjunctions().stream().filter(disjunction -> {
            return !disjunction.getPredicates().stream()
                    .filter(predicate -> PredicateType.EXCLUDED.equals(predicate.getType())).findAny().isPresent();
        }).mapToInt(e -> 1).sum();

        IntStream.range(0, cnf.getDisjunctions().size()).boxed().forEach(i -> {
            final Disjunction disjunction = cnf.getDisjunctions().get(i);
            final List<Map<Key, Set<DisjunctionPostingEntry>>> postingLists = disjunction.getPredicates().stream()
                    .map(predicate -> {
                        return predicate.accept(new CnfPredicateVisitorImpl(cnf.getId(), i));
                    }).collect(Collectors.toList());

            if (kSize == 0) {
                final Key key = Key.builder().name(ZERO_SIZE_DISJUNCTION_ENTRY_KEY).value(0).upperBoundScore(0).build();
                final Map<Key, Set<DisjunctionPostingEntry>> map = postingLists.stream()
                        .flatMap(m -> m.entrySet().stream()).map(Map.Entry::getValue).flatMap(m -> m.stream())
                        .distinct()
                        .map(entry -> DisjunctionPostingEntry.builder().id(entry.getId())
                                .predicateType(PredicateType.INCLUDED).disjunctionOrderId(-1).score(0).build())
                        .distinct().map(entry -> Pair.of(key, entry)).collect(Collectors.groupingBy(Pair::getKey,
                                Collectors.mapping(Pair::getValue, Collectors.toSet())));
                postingLists.add(map);
            }

            postingLists.add(indexTable.getOrDefault(indexTable, Collections.emptyMap()));
            indexTable.put(kSize, compactPostingLists(postingLists));
        });
        return null;
    }

    private <T, S> Map<T, Set<S>> compactPostingLists(List<Map<T, Set<S>>> maps) {
        final List<Map.Entry<T, Set<S>>> tempResult = maps.stream().collect(() -> new ArrayList<>(),
                (set, map) -> set.addAll(map.entrySet()), (set1, set2) -> set1.addAll(set2));
        return tempResult.stream().collect(Collectors.groupingBy(Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.reducing(Collections.emptySet(), (s1, s2) -> {
                    final Set<S> combined = new TreeSet<>(s1);
                    combined.addAll(s2);
                    return combined;
                }))));
    }

    private static final class CnfPredicateVisitorImpl
            implements PredicateVisitor<Map<Key, Set<DisjunctionPostingEntry>>> {
        private String id;
        private int orderId;

        public CnfPredicateVisitorImpl(String id, int disjunctionOrderId) {
            this.id = id;
            this.orderId = disjunctionOrderId;
        }

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
                        .predicateType(predicateType).disjunctionOrderId(orderId).score(0).build();
                return Pair.of(key, postingEntry);
            }).collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet())));
        }
    }

    private static final class DnfPredicatorVisitorImpl
            implements PredicateVisitor<Map<Key, Set<ConjunctionPostingEntry>>> {
        private String id;

        public DnfPredicatorVisitorImpl(String id) {
            this.id = id;
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
                final ConjunctionPostingEntry postingEntry = ConjunctionPostingEntry.builder().id(id)
                        .predicateType(predicateType).score(0).build();
                return Pair.of(key, postingEntry);
            }).collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet())));
        }
    }

}