package com.phonepe.growth.mustang.index.core;

import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "id", "predicateType", "order" })
@NoArgsConstructor
@AllArgsConstructor
public class DisjunctionPostingEntry implements Comparable<DisjunctionPostingEntry> {
    private String id;
    private PredicateType predicateType;
    private int order;
    private long score;

    @Override
    public int compareTo(DisjunctionPostingEntry o) {
        final int idc = id.compareTo(o.getId());
        if (idc != 0) {
            return idc;
        }
        return predicateType.compareTo(o.getPredicateType());
    }
}