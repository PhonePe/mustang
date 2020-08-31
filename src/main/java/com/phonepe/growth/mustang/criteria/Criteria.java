package com.phonepe.growth.mustang.criteria;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.EvaluationContext;
import com.phonepe.growth.mustang.criteria.impl.CNFExpression;
import com.phonepe.growth.mustang.criteria.impl.DNFExpression;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "form")
@JsonSubTypes({ @JsonSubTypes.Type(value = DNFExpression.class, name = CriteriaForm.DNF_TEXT),
        @JsonSubTypes.Type(value = CNFExpression.class, name = CriteriaForm.CNF_TEXT) })
public abstract class Criteria {
    @NotNull
    private CriteriaForm form;

    public abstract boolean process(EvaluationContext context);

    public abstract double getScore(EvaluationContext context);

    public abstract <T> T accept(CriteriaVisitor<T> visitor);

}
