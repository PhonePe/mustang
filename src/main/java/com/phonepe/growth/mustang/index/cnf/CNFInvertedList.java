package com.phonepe.growth.mustang.index.cnf;

import java.util.List;
import java.util.Map;

import com.phonepe.growth.mustang.index.Key;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CNFInvertedList {
    private Map<Integer, List<Map<Key, List<DisjunctionPostingEntry>>>> indexTable;
}
