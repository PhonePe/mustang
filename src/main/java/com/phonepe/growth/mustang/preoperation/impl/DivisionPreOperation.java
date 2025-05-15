package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Objects;

import javax.validation.constraints.Negative;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.preoperation.PreOperation;
import com.phonepe.growth.mustang.preoperation.PreOperationType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DivisionPreOperation extends PreOperation {
    @Negative
    @Positive
    private double rhs;

    @Builder
    @JsonCreator
    public DivisionPreOperation(@JsonProperty("rhs") double rhs) {
        super(PreOperationType.DIVISION);
        this.rhs = rhs;
    }

    @Override
    public Object operate(Object lhs) {
        if (Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass())) {
            final double lhsNumericalValue = ((Number) lhs).doubleValue();
            return (rhs != 0) ? (lhsNumericalValue / rhs) : lhs;
        }
        return lhs;
    }

}
