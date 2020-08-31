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
public class Disjunction extends Composition {

    @Builder
    @JsonCreator
    public Disjunction(@JsonProperty("predicates") List<Predicate> predicates) {
        super(CompositionType.OR, predicates);
    }

    @Override
    public boolean process(EvaluationContext context) {
        return getPredicates().stream().filter(predicate -> predicate.process(context)).findFirst().isPresent();
    }

    @Override
    public <T> T accept(CompositionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public double score(EvaluationContext context) {
        // TODO
        // TODO Auto-generated method stub
        return 0;
    }

}
