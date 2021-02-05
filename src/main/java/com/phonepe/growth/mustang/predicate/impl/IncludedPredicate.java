package com.phonepe.growth.mustang.predicate.impl;

import java.util.Set;

import javax.validation.constraints.NotEmpty;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.phonepe.growth.mustang.debug.PredicateDebugResult;

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
public class IncludedPredicate extends Predicate {
    @NotEmpty
    private Set<?> values;

    @Builder
    @JsonCreator
    public IncludedPredicate(@JsonProperty("lhs") String lhs,
            @JsonProperty("lhsNotAPath") boolean lhsNotAPath,
            @JsonProperty("weight") long weight,
            @JsonProperty("defaultResult") boolean defaultResult,
            @JsonProperty("values") Set<?> values) {
        super(PredicateType.INCLUDED, lhs, lhsNotAPath, weight, defaultResult);
        this.values = values;
    }

    @Override
    protected boolean evaluate(RequestContext context, Object lhsValue) {
        return values.contains(lhsValue);
    }

    @Override
    public PredicateDebugResult debug(RequestContext context) {
        Object lhsValue;
        try {
            lhsValue = this.isLhsNotAPath() ? this.getLhs()
                    : JsonPath.read(context.getNode()
                            .toString(), this.getLhs());
        } catch (PathNotFoundException e) {
            lhsValue = null;
        }
        return PredicateDebugResult.builder()
                .result(evaluate(context))
                .type(this.getType())
                .lhs(this.getLhs())
                .lhsValue(lhsValue)
                .values(values)
                .build();
    }

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
