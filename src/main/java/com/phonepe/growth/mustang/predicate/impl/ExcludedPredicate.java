package com.phonepe.growth.mustang.predicate.impl;

import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
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
    public ExcludedPredicate(@JsonProperty("lhs") String lhs,
            @JsonProperty("lhsNotAPath") boolean lhsNotAPath,
            @JsonProperty("weight") long weight,
            @JsonProperty("defaultResult") boolean defaultResult,
            Set<?> values) {
        super(PredicateType.EXCLUDED, lhs, lhsNotAPath, weight, defaultResult);
        this.values = values;
    }

    @Override
    public boolean evaluate(RequestContext context, Object lhsValue) {
        return !values.contains(lhsValue);
    }

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
