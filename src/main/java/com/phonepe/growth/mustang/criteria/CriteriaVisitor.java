package com.phonepe.growth.mustang.criteria;

import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;

public interface CriteriaVisitor<T> {

    T visit(DNFCriteria dnf);

    T visit(CNFCriteria cnf);

}
