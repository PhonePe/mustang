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
public class DisjunctionPostingEntry {
    private String id;
    private PredicateType predicateType;
    private int order;
    private long score;
}