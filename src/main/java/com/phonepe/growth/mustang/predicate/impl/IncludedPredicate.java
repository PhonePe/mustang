package com.phonepe.growth.mustang.predicate.impl;

import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.EvaluationContext;
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
public class IncludedPredicate extends Predicate {
    @NotEmpty
    private Set<?> values;

    @Builder
    @JsonCreator
    public IncludedPredicate(@JsonProperty("lhsPath") String lhsPath,
            @JsonProperty("isLhsPathJson") boolean isLhsPathJson, @JsonProperty("defaultResult") boolean defaultResult,
            @JsonProperty("values") Set<?> values) {
        super(PredicateType.INCLUDED, lhsPath, isLhsPathJson, defaultResult);
        this.values = values;
    }

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    protected boolean evaluate(EvaluationContext context, Object lhsValue) {
        return values.contains(lhsValue);
    }

}
