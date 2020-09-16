package com.phonepe.growth.mustang.index.core;

import org.hibernate.validator.constraints.NotBlank;

import com.phonepe.growth.mustang.index.InvertedIndex;
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
    @Builder.Default
    private InvertedIndex<ConjunctionPostingEntry> dnfInvertedIndex = DNFInvertedIndex
            .<ConjunctionPostingEntry>builder().build();
    @Builder.Default
    private InvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex = CNFInvertedIndex
            .<DisjunctionPostingEntry>builder().build();;

}
