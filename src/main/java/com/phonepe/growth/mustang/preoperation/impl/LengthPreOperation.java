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
public class LengthPreOperation extends PreOperation {

    @Builder
    @JsonCreator
    public LengthPreOperation() {
        super(PreOperationType.LENGTH);
    }

    @Override
    public Object operate(Object value) {
        if (Objects.nonNull(value) && String.class.isAssignableFrom(value.getClass())) {
            return value.toString()
                    .length();
        }
        return value;
    }

}
