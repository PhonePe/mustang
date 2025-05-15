package com.phonepe.growth.mustang.preoperation.impl;

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
public class EpochDiffPreOperation extends PreOperation {

    @Builder
    @JsonCreator
    public EpochDiffPreOperation() {
        super(PreOperationType.EPOCH_DIFF);
    }

    @Override
    public <T> T accept(PreOperationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object operate(Object value) {
        // TODO Auto-generated method stub
        return null;
    }

}
