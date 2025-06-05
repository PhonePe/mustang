package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.phonepe.growth.mustang.preoperation.PreOperation;
import com.phonepe.growth.mustang.preoperation.PreOperationType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BinaryConversionPreOperation extends PreOperation {
    private static final String FORMAT = "%32s";
    private static final String SPACE = " ";
    private static final String ZERO = "0";

    @Builder
    @JsonCreator
    public BinaryConversionPreOperation() {
        super(PreOperationType.BINARY_CONVERSION);
    }

    @Override
    public Object operate(Object lhs) {
        if (canApply(lhs)) {
            return String.format(FORMAT, Integer.toBinaryString(((Number) lhs).intValue()))
                    .replace(SPACE, ZERO);
        }
        return lhs;
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs) && Integer.class.isAssignableFrom(lhs.getClass());
    }

}
