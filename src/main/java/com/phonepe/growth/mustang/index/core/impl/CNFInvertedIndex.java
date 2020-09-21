package com.phonepe.growth.mustang.index.core.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.core.InvertedIndex;
import com.phonepe.growth.mustang.index.core.InvertedIndexVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CNFInvertedIndex<T> extends InvertedIndex<T> {
    @Builder
    @JsonCreator
    public CNFInvertedIndex() {
        super(CriteriaForm.CNF);
    }

    @Override
    public <U> U accept(InvertedIndexVisitor<T, U> visitor) {
        return visitor.visit(this);
    }

}
