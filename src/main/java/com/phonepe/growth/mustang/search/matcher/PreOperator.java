package com.phonepe.growth.mustang.search.matcher;

import com.phonepe.growth.mustang.preoperation.PreOperationVisitor;
import com.phonepe.growth.mustang.preoperation.impl.AdditionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.DivisionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.EpochDatePreOperation;
import com.phonepe.growth.mustang.preoperation.impl.EpochDiffPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.IdentityOperation;
import com.phonepe.growth.mustang.preoperation.impl.LengthPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.ModuloPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.MultiplicationPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SizePreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SubStringPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SubtractionPreOperation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreOperator implements PreOperationVisitor<Object> {
    private Object value;

    @Override
    public Object visit(IdentityOperation operation) {
        return value;
    }

    @Override
    public Object visit(AdditionPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(SubtractionPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(MultiplicationPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(DivisionPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(ModuloPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(SizePreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(LengthPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(SubStringPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(EpochDatePreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visit(EpochDiffPreOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

}
