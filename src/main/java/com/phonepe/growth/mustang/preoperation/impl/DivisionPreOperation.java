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
    private long rhs;

    @Builder
    @JsonCreator
    public DivisionPreOperation(@JsonProperty("rhs") long rhs) {
        super(PreOperationType.DIVISION);
        this.rhs = rhs;
    }

    @Override
    public Object operate(Object lhs) {
        if (Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass())) {
            final long lhsNumericalValue = ((Number) lhs).longValue();
            return (long) ((rhs != 0) ? (lhsNumericalValue / rhs) : lhsNumericalValue);
        }
        return lhs;
    }

}
