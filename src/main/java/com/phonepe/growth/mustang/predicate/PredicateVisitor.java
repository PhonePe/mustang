package com.phonepe.growth.mustang.predicate;

import com.phonepe.growth.mustang.predicate.impl.InPredicate;
import com.phonepe.growth.mustang.predicate.impl.NotInPredicate;

public interface PredicateVisitor<T> {

    T visit(InPredicate predicate);

    T visit(NotInPredicate predicate);

}
