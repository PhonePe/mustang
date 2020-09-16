package com.phonepe.growth.mustang.index;

import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.core.impl.DNFInvertedIndex;

public interface InvertedIndexVisitor<T, U> {

    U visit(DNFInvertedIndex<T> index);

    U visit(CNFInvertedIndex<T> index);
}
