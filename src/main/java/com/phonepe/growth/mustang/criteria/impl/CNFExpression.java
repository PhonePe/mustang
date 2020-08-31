package com.phonepe.growth.mustang.criteria.impl;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.EvaluationContext;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.criteria.CriteriaVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CNFExpression extends Criteria {
    @Valid
    @NotEmpty
    private List<Disjunction> disjunctions;

    @Builder
    @JsonCreator
    public CNFExpression(@JsonProperty("disjucntions") List<Disjunction> disjunctions) {
        super(CriteriaForm.CNF);
        this.disjunctions = disjunctions;
    }

    @Override
    public boolean process(EvaluationContext context) {
        return disjunctions.stream().filter(disjunction -> !disjunction.process(context)).findFirst().isPresent();
    }

    @Override
    public double getScore(EvaluationContext context) {
        // score of a CNF is the sum of score of all its constituent disjunctions.
        return disjunctions.stream().mapToDouble(disjunction -> disjunction.score(context)).sum();
    }

    @Override
    public <T> T accept(CriteriaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
