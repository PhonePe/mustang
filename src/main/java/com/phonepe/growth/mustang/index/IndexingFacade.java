package com.phonepe.growth.mustang.index;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.exception.ErrorCode;
import com.phonepe.growth.mustang.exception.MustangException;
import com.phonepe.growth.mustang.index.builder.CriteriaIndexBuilder;
import com.phonepe.growth.mustang.index.group.IndexGroup;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndexingFacade {
    private final Map<String, IndexGroup> indexMap = Maps.newConcurrentMap();

    public void add(final String index, final Criteria criteria) {
        final IndexGroup indexGroup = get(index);
        criteria.accept(CriteriaIndexBuilder.builder().indexGroup(indexGroup).build());
        indexGroup.getAllCriterias().put(criteria.getId(), criteria);
    }

    public void add(final String index, final List<Criteria> criterias) {
        final IndexGroup indexGroup = get(index);
        criterias.forEach(criteria -> {
            criteria.accept(CriteriaIndexBuilder.builder().indexGroup(indexGroup).build());
            indexGroup.getAllCriterias().put(criteria.getId(), criteria);
        });
    }

    public boolean replace(final String oldIndex, final String newIndex) {
        final boolean result = indexMap.replace(oldIndex, getIndexGroup(oldIndex), getIndexGroup(newIndex));
        getIndexGroup(oldIndex).setName(oldIndex);
        indexMap.remove(newIndex, getIndexGroup(newIndex));
        return result;
    }

    public IndexGroup getIndexGroup(final String index) {
        if (indexMap.containsKey(index)) {
            return indexMap.get(index);
        }
        throw MustangException.builder().errorCode(ErrorCode.INDEX_NOT_FOUND).build();
    }

    private IndexGroup get(final String index) {
        if (!indexMap.containsKey(index)) {
            indexMap.put(index, IndexGroup.builder().name(index).build());
        }
        return indexMap.get(index);
    }

}
