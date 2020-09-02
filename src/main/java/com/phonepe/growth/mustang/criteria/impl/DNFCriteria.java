package com.phonepe.growth.mustang.criteria.impl;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.EvaluationContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
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
public class DNFCriteria extends Criteria {
    @Valid
    @NotEmpty
    private List<Conjunction> conjunctions;

    @Builder
    @JsonCreator
    public DNFCriteria(@JsonProperty("conjunctions") List<Conjunction> conjunctions) {
        super(CriteriaForm.DNF);
        this.conjunctions = conjunctions;
    }

    @Override
    public boolean evaluate(EvaluationContext context) {
        return conjunctions.stream().filter(conjunction -> conjunction.evaluate(context)).findFirst().isPresent();
    }

    @Override
    public long getScore(EvaluationContext context) {
        // score of a DNF is the max of scores of its constituent conjunctions.
        return conjunctions.stream().map(conjunction -> conjunction.score(context)).max(Double::compare).get();
    }

    @Override
    public <T> T accept(CriteriaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
