package com.phonepe.growth.mustang.composition;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.predicate.Predicate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(name = CompositionType.AND_TEXT, value = Conjunction.class),
        @JsonSubTypes.Type(name = CompositionType.OR_TEXT, value = Disjunction.class), })
public abstract class Composition {
    @NotNull
    private final CompositionType type;
    @Size(min = 2)
    private List<Predicate> predicates;

    public abstract boolean evaluate(EvaluationContext context);

    public abstract double getScore(EvaluationContext context);

    public abstract <T> T accept(CompositionVisitor<T> visitor);
}
