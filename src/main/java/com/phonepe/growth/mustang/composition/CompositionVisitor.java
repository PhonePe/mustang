package com.phonepe.growth.mustang.composition;

import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;

public interface CompositionVisitor<T> {

    T visit(Conjunction conjunction);

    T visit(Disjunction disjunction);

}
