package com.phonepe.growth.mustang.index;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.Criteria;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Indexer {
    private static final Map<String, IndexGroup> indexMap = Maps.newHashMap();

    public static void add(String index, Criteria criteria) {
        final IndexGroup indexGroup = getIndexGroup(index);
        criteria.accept(CriteriaIndexer.builder().indexGroup(indexGroup).build());
    }

    public static void add(String index, List<Criteria> criterias) {
        final IndexGroup indexGroup = getIndexGroup(index);
        criterias.forEach(criteria -> criteria.accept(CriteriaIndexer.builder().indexGroup(indexGroup).build()));

    }

    private static IndexGroup getIndexGroup(String index) {
        if (!indexMap.containsKey(index)) {
            indexMap.put(index, IndexGroup.builder().name(index).build());
        }
        return indexMap.get(index);
    }

}
