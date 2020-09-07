package com.phonepe.growth.mustang.criteria.impl;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.EvaluationContext;
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
public class CNFCriteria extends Criteria {
    @Valid
    @NotEmpty
    private List<Disjunction> disjunctions;

    @Builder
    @JsonCreator
    public CNFCriteria(@JsonProperty("id") String id, @JsonProperty("disjunctions") List<Disjunction> disjunctions) {
        super(CriteriaForm.CNF, id);
        this.disjunctions = disjunctions;
    }

    @Override
    public boolean evaluate(EvaluationContext context) {
        return disjunctions.stream().filter(disjunction -> !disjunction.evaluate(context)).findFirst().isPresent();
    }

    @Override
    public long getScore(EvaluationContext context) {
        // score of a CNF is the sum of score of all its constituent disjunctions.
        return disjunctions.stream().mapToLong(disjunction -> disjunction.getScore(context)).sum();
    }

    @Override
    public <T> T accept(CriteriaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
