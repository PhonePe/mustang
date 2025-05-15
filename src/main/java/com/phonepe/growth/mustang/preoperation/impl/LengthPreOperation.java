package com.phonepe.growth.mustang.preoperation.impl;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
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
public class LengthPreOperation extends PreOperation {

    @Builder
    @JsonCreator
    public LengthPreOperation() {
        super(PreOperationType.LENGTH);
    }

    @Override
    public <T> T accept(PreOperationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object operate(Object value) {
        if (Objects.nonNull(value) && String.class.isAssignableFrom(value.getClass())) {
            return value.toString()
                    .length();
        }
        // TODO revisit
        return value;
    }

}
