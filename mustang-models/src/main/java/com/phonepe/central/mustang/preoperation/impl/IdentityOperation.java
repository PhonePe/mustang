package com.phonepe.central.mustang.preoperation.impl;

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
public class IdentityOperation extends PreOperation {

    @Builder
    @JsonCreator
    public IdentityOperation() {
        super(PreOperationType.IDENTITY);
    }

    @Override
    public Object operate(Object operand) {
        return operand;
    }

    @Override
    public boolean canApply(Object lhsOperand) {
        return true;
    }

}
