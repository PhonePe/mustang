package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Objects;

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
public class ModuloPreOperation extends PreOperation {
    @Positive
    private long rhs;

    @Builder
    @JsonCreator
    public ModuloPreOperation(@JsonProperty("rhs") long rhs) {
        super(PreOperationType.MODULO);
        this.rhs = rhs;
    }

    @Override
    public Object operate(Object lhs) {
        if (canApply(lhs)) {
            return (long) (((Number) lhs).longValue() % rhs);
        }
        return lhs;
    }

    @Override
    public boolean canApply(Object lhs) {
        return Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass()) && rhs != 0;
    }

}
