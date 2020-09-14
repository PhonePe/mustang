package com.phonepe.growth.mustang.index.cnf;

import java.util.Map;
import java.util.Set;

import com.phonepe.growth.mustang.index.Key;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CNFInvertedIndex {
    private Map<Integer, Map<Key, Set<DisjunctionPostingEntry>>> indexTable;
}
