package com.phonepe.growth.mustang.preoperation.impl;

import java.math.BigDecimal;
import java.util.Objects;

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
public class SubtractionPreOperation extends PreOperation {
    private BigDecimal rhs;

    @Builder
    @JsonCreator
    public SubtractionPreOperation(@JsonProperty("rhs") double rhs) {
        super(PreOperationType.SUBTRACTION);
        this.rhs = BigDecimal.valueOf(rhs);
    }

    @Override
    public Object operate(Object lhs) {
        if (canApply(lhs)) {
            return BigDecimal.valueOf(((Number) lhs).doubleValue())
                    .subtract(rhs);
        }
        return lhs;
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass());
    }

}
