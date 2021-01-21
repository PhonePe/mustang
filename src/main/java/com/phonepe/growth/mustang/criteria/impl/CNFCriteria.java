package com.phonepe.growth.mustang.criteria.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.phonepe.growth.mustang.traverse.DebugResult;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.criteria.CriteriaVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
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
    public CNFCriteria(@JsonProperty("id") String id,
            @JsonProperty("disjunctions") @Singular List<Disjunction> disjunctions) {
        super(CriteriaForm.CNF, id);
        this.disjunctions = disjunctions;
    }

    @Override
    public boolean evaluate(RequestContext context) {
        return disjunctions.stream()
                .allMatch(disjunction -> disjunction.evaluate(context));
    }

    @Override
    public DebugResult debug(RequestContext context) {
        return DebugResult.builder()
                .result(evaluate(context))
                .form(CriteriaForm.CNF)
                .compositionResults(disjunctions.stream()
                        .map(disjunctions -> disjunctions.debug(context))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public double getScore(RequestContext context) {
        // score of a CNF is the sum of score of all its constituent disjunctions.
        return disjunctions.stream()
                .mapToDouble(disjunction -> disjunction.getScore(context))
                .sum();
    }

    @Override
    public <T> T accept(CriteriaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
