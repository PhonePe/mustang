package com.phonepe.growth.mustang.composition.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.Composition;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.composition.CompositionVisitor;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.PredicateType;

import com.phonepe.growth.mustang.traverse.CompositionResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Disjunction extends Composition {

    @Builder
    @JsonCreator
    public Disjunction(@JsonProperty("predicates") @Singular List<Predicate> predicates) {
        super(CompositionType.OR, predicates);
    }

    @Override
    public boolean evaluate(RequestContext context) {
        return getPredicates().stream()
                .anyMatch(predicate -> predicate.evaluate(context));
    }

    @Override
    public CompositionResult debug(RequestContext context) {
        return CompositionResult.builder()
                .result(evaluate(context))
                .type(CompositionType.OR)
                .predicateResults(getPredicates().stream()
                        .map(predicate -> predicate.debug(context))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public double getScore(RequestContext context) {
        return getPredicates().stream()
                .filter(predicate -> PredicateType.INCLUDED.equals(predicate.getType()))
                .mapToDouble(predicate -> predicate.getWeight() * getWeigthFromContext(context, predicate))
                .max()
                .orElse(0);
    }

    @Override
    public <T> T accept(CompositionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
