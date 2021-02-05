package com.phonepe.growth.mustang.predicate;

import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;

public interface PredicateVisitor<T> {

    T visit(IncludedPredicate predicate);

    T visit(ExcludedPredicate predicate);

}
