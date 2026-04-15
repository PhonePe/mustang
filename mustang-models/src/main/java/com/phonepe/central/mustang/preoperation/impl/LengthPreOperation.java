package com.phonepe.central.mustang.preoperation.impl;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.phonepe.central.mustang.preoperation.PreOperation;
import com.phonepe.central.mustang.preoperation.PreOperationType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LengthPreOperation extends PreOperation {

    @Builder
    @JsonCreator
    public LengthPreOperation() {
        super(PreOperationType.LENGTH);
    }

    @Override
    public Object operate(Object value) {
        if (canApply(value)) {
            return String.valueOf(value)
                    .length();
        }
        return value;
    }

    @Override
    public boolean canApply(Object value) {
        return Objects.nonNull(value) && String.class.isAssignableFrom(value.getClass());
    }

}
