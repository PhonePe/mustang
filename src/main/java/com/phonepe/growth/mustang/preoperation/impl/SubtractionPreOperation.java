package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.preoperation.PreOperation;
import com.phonepe.growth.mustang.preoperation.PreOperationType;
import com.phonepe.growth.mustang.preoperation.PreOperationVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SubtractionPreOperation extends PreOperation {
    private double rhs;

    @Builder
    @JsonCreator
    public SubtractionPreOperation(@JsonProperty("rhs") double rhs) {
        super(PreOperationType.SUBTRACTION);
        this.rhs = rhs;
    }

    @Override
    public <T> T accept(PreOperationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object operate(Object lhs) {
        if (Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass())) {
            final double lhsNumericalValue = ((Number) lhs).doubleValue();
            return lhsNumericalValue - rhs;
        }
        // TODO revisit
        return lhs;
    }

}
