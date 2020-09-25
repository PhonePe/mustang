package com.phonepe.growth.mustang.index;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.exception.ErrorCode;
import com.phonepe.growth.mustang.exception.MustangException;
import com.phonepe.growth.mustang.index.builder.CriteriaIndexBuilder;
import com.phonepe.growth.mustang.index.group.IndexGroup;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IndexingFacade {
    private static final Map<String, IndexGroup> indexMap = Maps.newConcurrentMap();

    public static void add(String index, Criteria criteria) {
        final IndexGroup indexGroup = getIndexGroup(index);
        criteria.accept(CriteriaIndexBuilder.builder().indexGroup(indexGroup).build());
    }

    public static void add(String index, List<Criteria> criterias) {
        final IndexGroup indexGroup = getIndexGroup(index);
        criterias.forEach(criteria -> criteria.accept(CriteriaIndexBuilder.builder().indexGroup(indexGroup).build()));
    }

    public static IndexGroup get(String index) {
        if (indexMap.containsKey(index)) {
            return indexMap.get(index);
        }
        throw MustangException.builder().errorCode(ErrorCode.INDEX_NOT_FOUND).build();
    }

    private static IndexGroup getIndexGroup(String index) {
        if (!indexMap.containsKey(index)) {
            indexMap.put(index, IndexGroup.builder().name(index).build());
        }
        return indexMap.get(index);
    }

}
