package com.phonepe.growth.mustang.index.core;

import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "iId", "type" })
@NoArgsConstructor
@AllArgsConstructor
public class ConjunctionPostingEntry implements Comparable<ConjunctionPostingEntry> {
    private Integer iId;
    private String eId;
    private PredicateType type;
    private long score;

    @Override
    public int compareTo(ConjunctionPostingEntry o) {
        final int idc = iId.compareTo(o.getIId());
        if (idc != 0) {
            return idc;
        }
        return type.compareTo(o.getType());
    }
}