package com.phonepe.growth.mustang.composition;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.EvaluationContext;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.predicate.impl.InPredicate;
import com.phonepe.growth.mustang.predicate.impl.NotInPredicate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = InPredicate.class, name = PredicateType.IN_TEXT),
        @JsonSubTypes.Type(value = NotInPredicate.class, name = PredicateType.NOT_IN_TEXT), })
public abstract class Composition {
    @NotNull
    private CompositionType type;
    @Size(min = 2)
    private List<Predicate> predicates;

    public abstract boolean process(EvaluationContext context);

    public abstract double score(EvaluationContext context);

    public abstract <T> T accept(CompositionVisitor<T> visitor);
}
