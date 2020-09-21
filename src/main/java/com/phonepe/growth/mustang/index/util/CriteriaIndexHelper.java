package com.phonepe.growth.mustang.index.util;

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
import com.phonepe.growth.mustang.index.IndexGroup;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaIndexHelper implements CriteriaVisitor<Void> {
    private static final String CONJUNCTION_ENTRY_ID_FORMAT = "%s#%s";
    private static final String ZERO_SIZE_CONJUNCTION_ENTRY_KEY = "ZZZ";
    private static final String ZERO_SIZE_DISJUNCTION_ENTRY_KEY = "ZZZ";
    @Valid
    @NotNull
    private IndexGroup indexGroup;
    @NotBlank
    private String criteriaId;

    @Override
    public Void visit(DNFCriteria dnf) {
        dnf.getConjunctions().forEach(conjunction -> {
            final Map<Integer, Map<Key, Set<ConjunctionPostingEntry>>> indexTable = indexGroup.getDnfInvertedIndex()
                    .getTable();
            final int kSize = conjunction.getPredicates().stream()
                    .filter(predicate -> PredicateType.INCLUDED.equals(predicate.getType())).mapToInt(e -> 1).sum();

            final List<Map<Key, Set<ConjunctionPostingEntry>>> postingLists = IntStream
                    .range(0, conjunction.getPredicates().size()).boxed().map(i -> {
                        return conjunction.getPredicates().get(i).accept(DnfPredicatorVisitorImpl.builder()
                                .id(String.format(CONJUNCTION_ENTRY_ID_FORMAT, criteriaId, i)).build());
                    }).collect(Collectors.toList());
            if (kSize == 0) {
                // ZERO size handling
                final Key key = Key.builder().name(ZERO_SIZE_CONJUNCTION_ENTRY_KEY).value(0).upperBoundScore(0).build();
                final Map<Key, Set<ConjunctionPostingEntry>> map = postingLists.stream()
                        .flatMap(m -> m.entrySet().stream())
                        .map(Map.Entry::getValue)
                        .flatMap(m -> m.stream())
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
        final Map<Integer, Map<Key, Set<DisjunctionPostingEntry>>> indexTable = indexGroup.getCnfInvertedIndex()
                .getTable();
        final int kSize = cnf.getDisjunctions().stream()
                .filter(disjunction -> !isDisjunctionWithExcludedPredicate(disjunction)).mapToInt(e -> 1).sum();

        IntStream.range(0, cnf.getDisjunctions().size()).boxed().forEach(i -> {
            final Disjunction disjunction = cnf.getDisjunctions().get(i);
            final List<Map<Key, Set<DisjunctionPostingEntry>>> postingLists = disjunction.getPredicates().stream()
                    .map(predicate -> {
                        return predicate.accept(CnfPredicateVisitorImpl.builder().id(cnf.getId()).order(i).build());
                    }).collect(Collectors.toList());

            if (kSize == 0) {
                // Zero size handling
                final Key key = Key.builder().name(ZERO_SIZE_DISJUNCTION_ENTRY_KEY).value(0).upperBoundScore(0).build();
                final Map<Key, Set<DisjunctionPostingEntry>> map = postingLists.stream()
                        .flatMap(m -> m.entrySet().stream()).map(Map.Entry::getValue).flatMap(m -> m.stream())
                        .distinct()
                        .map(entry -> DisjunctionPostingEntry.builder().id(entry.getId())
                                .predicateType(PredicateType.INCLUDED).order(-1).score(0).build())
                        .distinct().map(entry -> Pair.of(key, entry)).collect(Collectors.groupingBy(Pair::getKey,
                                Collectors.mapping(Pair::getValue, Collectors.toSet())));
                postingLists.add(map);
            }

            postingLists.add(indexTable.getOrDefault(indexTable, Collections.emptyMap()));
            indexTable.put(kSize, compactPostingLists(postingLists));
        });
        return null;
    }

    private boolean isDisjunctionWithExcludedPredicate(Disjunction disjunction) {
        return disjunction.getPredicates().stream()
                .filter(predicate -> PredicateType.EXCLUDED.equals(predicate.getType())).findAny().isPresent();
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

}