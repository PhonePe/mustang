package com.phonepe.growth.mustang.index.group;

import java.util.Map;

import org.hibernate.validator.constraints.NotBlank;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.InvertedIndex;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.core.impl.DNFInvertedIndex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "name" })
@NoArgsConstructor
@AllArgsConstructor
public class IndexGroup {
    @NotBlank
    private String name;
    private final InvertedIndex<ConjunctionPostingEntry> dnfInvertedIndex = DNFInvertedIndex
            .<ConjunctionPostingEntry>builder().build();
    private final InvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex = CNFInvertedIndex
            .<DisjunctionPostingEntry>builder().build();
    private final Map<String, Criteria> allCriterias = Maps.newConcurrentMap();

}
