package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Objects;

import javax.validation.constraints.Positive;

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
    public <T> T accept(PreOperationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object operate(Object lhs) {
        if (Objects.nonNull(lhs) && Number.class.isAssignableFrom(lhs.getClass())) {
            final long lhsNumericalValue = ((Number) lhs).longValue();
            return (rhs != 0) ? (lhsNumericalValue % rhs) : 0; // DO NOT WANT TO THROW EXCEPTION
        }
        return lhs;
    }

}
