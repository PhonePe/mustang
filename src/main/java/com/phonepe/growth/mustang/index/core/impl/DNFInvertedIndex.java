package com.phonepe.growth.mustang.index.core.impl;

import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.core.InvertedIndex;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DNFInvertedIndex<T> extends InvertedIndex<T> {

    @Builder
    public DNFInvertedIndex() {
        super(CriteriaForm.DNF);
    }
}
