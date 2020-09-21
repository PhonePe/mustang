package com.phonepe.growth.mustang.predicate.impl;

import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.predicate.PredicateVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExcludedPredicate extends Predicate {
    @NotEmpty
    private Set<?> values;

    @Builder
    @JsonCreator
    public ExcludedPredicate(@JsonProperty("lhsPath") String lhsPath,
            @JsonProperty("isLhsPathJson") boolean isLhsPathJson, @JsonProperty("weight") long weight,
            @JsonProperty("defaultResult") boolean defaultResult, Set<?> values) {
        super(PredicateType.EXCLUDED, lhsPath, isLhsPathJson, weight, defaultResult);
        this.values = values;
    }

    @Override
    public boolean evaluate(EvaluationContext context, Object lhsValue) {
        return !values.contains(lhsValue);
    }

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
