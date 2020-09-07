package com.phonepe.growth.mustang.index.dnf;

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
public class ConjunctionPostingEntry {
    private String id;
    private PredicateType predicateType;
    private long score;
}