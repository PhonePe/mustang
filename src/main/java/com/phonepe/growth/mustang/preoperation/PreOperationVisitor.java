package com.phonepe.growth.mustang.preoperation;

import com.phonepe.growth.mustang.preoperation.impl.AdditionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.DivisionPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.IdentityOperation;
import com.phonepe.growth.mustang.preoperation.impl.LengthPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.ModuloPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.MultiplicationPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SizePreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SubStringPreOperation;
import com.phonepe.growth.mustang.preoperation.impl.SubtractionPreOperation;

public interface PreOperationVisitor<T> {

    T visit(IdentityOperation operation);

    T visit(AdditionPreOperation operation);

    T visit(SubtractionPreOperation operation);

    T visit(MultiplicationPreOperation operation);

    T visit(DivisionPreOperation operation);

    T visit(ModuloPreOperation operation);

    T visit(SizePreOperation operation);

    T visit(LengthPreOperation operation);

    T visit(SubStringPreOperation operation);

}
