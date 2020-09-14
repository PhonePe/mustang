package com.phonepe.growth.mustang.index.cnf;

import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisjunctionPostingEntry {
    private String id;
    private PredicateType predicateType;
    private int disjunctionOrderId;
    private long score;
}