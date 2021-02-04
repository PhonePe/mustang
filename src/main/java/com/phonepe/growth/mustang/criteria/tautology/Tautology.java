package com.phonepe.growth.mustang.criteria.tautology;

import java.util.UUID;

import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;

public class Tautology {

    public static final Criteria getDNFTautology(final String criteriaId) {
        return DNFCriteria.builder()
                .id(criteriaId)
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs(UUID.randomUUID()
                                        .toString())
                                .lhsNotAPath(true)
                                .values(Sets.newHashSet(System.nanoTime()))
                                .build())
                        .build())
                .build();
    }

    public static final Criteria getCNFTautology(final String criteriaId) {
        return CNFCriteria.builder()
                .id(criteriaId)
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs(UUID.randomUUID()
                                        .toString())
                                .lhsNotAPath(true)
                                .values(Sets.newHashSet(System.nanoTime()))
                                .build())
                        .build())
                .build();
    }
}
