package com.phonepe.growth.mustang.index.dnf;

import java.util.Map;
import java.util.Set;

import com.phonepe.growth.mustang.index.Key;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DNFInvertedIndex {
    private Map<Integer, Map<Key, Set<ConjunctionPostingEntry>>> indexTable;
}
