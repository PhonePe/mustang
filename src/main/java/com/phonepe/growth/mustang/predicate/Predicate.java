package com.phonepe.growth.mustang.predicate;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.EvaluationContext;
import com.phonepe.growth.mustang.predicate.impl.InPredicate;
import com.phonepe.growth.mustang.predicate.impl.NotInPredicate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = InPredicate.class, name = PredicateType.IN_TEXT),
        @JsonSubTypes.Type(value = NotInPredicate.class, name = PredicateType.NOT_IN_TEXT), })
public abstract class Predicate {
    @NotNull
    private PredicateType type;
    @NotBlank
    private String lhsPath;
    private boolean isLhsPathJson;
    
    public abstract boolean process(EvaluationContext context);
    
    public abstract <T> T accept(PredicateVisitor<T> visitor);
}
