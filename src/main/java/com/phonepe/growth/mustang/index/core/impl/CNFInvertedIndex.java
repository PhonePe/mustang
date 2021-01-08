package com.phonepe.growth.mustang.index.core.impl;

import java.util.Map;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.core.InvertedIndex;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CNFInvertedIndex<T> extends InvertedIndex<T> {
    private final Map<Integer, Integer[]> disjunctionCounters = Maps.newConcurrentMap();

    @Builder
    public CNFInvertedIndex() {
        super(CriteriaForm.CNF);
    }

}
