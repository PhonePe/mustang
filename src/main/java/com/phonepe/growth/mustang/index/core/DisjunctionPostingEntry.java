package com.phonepe.growth.mustang.index.core;

import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "iId", "type", "order" })
@NoArgsConstructor
@AllArgsConstructor
public class DisjunctionPostingEntry implements Comparable<DisjunctionPostingEntry> {
    private Integer iId;
    private String eId;
    private PredicateType type;
    private int order;
    private long score;

    @Override
    public int compareTo(DisjunctionPostingEntry o) {
        final int idc = iId.compareTo(o.getIId());
        if (idc != 0) {
            return idc;
        }
        return type.compareTo(o.getType());
    }
}