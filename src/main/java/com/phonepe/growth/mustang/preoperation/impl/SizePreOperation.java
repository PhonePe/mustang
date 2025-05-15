package com.phonepe.growth.mustang.preoperation.impl;

import java.util.List;
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
public class SizePreOperation extends PreOperation {

    @Builder
    @JsonCreator
    public SizePreOperation(PreOperationType type) {
        super(PreOperationType.SIZE);
    }

    @Override
    public <T> T accept(PreOperationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object operate(Object value) {
        if (Objects.nonNull(value) && List.class.isAssignableFrom(value.getClass())) {
            return ((List<?>) value).size();
        }
        return value; // TODO revisit
    }

}
