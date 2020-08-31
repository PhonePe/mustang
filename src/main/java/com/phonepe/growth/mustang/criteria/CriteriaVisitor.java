package com.phonepe.growth.mustang.criteria;

import com.phonepe.growth.mustang.criteria.impl.CNFExpression;
import com.phonepe.growth.mustang.criteria.impl.DNFExpression;

public interface CriteriaVisitor<T> {

    T visit(DNFExpression dnf);

    T visit(CNFExpression cnf);

}
