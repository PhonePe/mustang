package com.phonepe.growth.mustang.index.core;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "id", "predicateType" })
@NoArgsConstructor
@AllArgsConstructor
public class ConjunctionPostingEntry implements Comparable<ConjunctionPostingEntry> {
    @NotBlank
    private String id;
    @NotNull
    private PredicateType predicateType;
    private long score;

    @Override
    public int compareTo(ConjunctionPostingEntry o) {
        final int idc = id.compareTo(o.getId());
        if (idc != 0) {
            return idc;
        }
        return predicateType.compareTo(o.getPredicateType());
    }
}