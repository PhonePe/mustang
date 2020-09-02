package com.phonepe.growth.mustang.composition.impl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.EvaluationContext;
import com.phonepe.growth.mustang.composition.Composition;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.composition.CompositionVisitor;
import com.phonepe.growth.mustang.predicate.Predicate;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Conjunction extends Composition {

    @Builder
    @JsonCreator
    public Conjunction(@JsonProperty("predicates") List<Predicate> predicates) {
        super(CompositionType.AND, predicates);
    }

    @Override
    public boolean evaluate(final EvaluationContext context) {
        // short-circuited implementation looking for a single false
        return !getPredicates().stream().filter(predicate -> !predicate.evaluate(context)).findFirst().isPresent();
    }

    @Override
    public <T> T accept(CompositionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public long score(EvaluationContext context) {
        // TODO impl
        return 0;
    }

}
