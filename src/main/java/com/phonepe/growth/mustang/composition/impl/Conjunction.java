package com.phonepe.growth.mustang.composition.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.composition.Composition;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.composition.CompositionVisitor;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Conjunction extends Composition {

    @Builder
    @JsonCreator
    public Conjunction(@JsonProperty("predicates") @Singular List<Predicate> predicates) {
        super(CompositionType.AND, predicates);
    }

    @Override
    public boolean evaluate(final EvaluationContext context) {
        return getPredicates().stream().allMatch(predicate -> predicate.evaluate(context));
    }

    @Override
    public double getScore(EvaluationContext context) {
        return getPredicates().stream().filter(predicate -> PredicateType.INCLUDED.equals(predicate.getType()))
                .mapToDouble(Predicate::getWeight) // TODO should consider weight from the context also
                .sum();
    }

    @Override
    public <T> T accept(CompositionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
