package com.phonepe.growth.mustang.composition.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.composition.Composition;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.composition.CompositionVisitor;
import com.phonepe.growth.mustang.predicate.Predicate;

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
    public boolean evaluate(EvaluationContext context) {
        return getPredicates().stream().anyMatch(predicate -> predicate.evaluate(context));
    }

    @Override
    public <T> T accept(CompositionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public double getScore(EvaluationContext context) {
        // TODO impl
        return 0;
    }

}
