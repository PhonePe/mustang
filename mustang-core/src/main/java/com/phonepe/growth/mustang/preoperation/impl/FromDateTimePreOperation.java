package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Date;
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
public class FromDateTimePreOperation extends PreOperation {
    // TODO

    @Builder
    @JsonCreator
    public FromDateTimePreOperation() {
        super(PreOperationType.FROM_DATETIME);
    }

    @Override
    public Object operate(Object lhs) {
        return null;
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs) && Date.class.isAssignableFrom(lhs.getClass());
    }

}
