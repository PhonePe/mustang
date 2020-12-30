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
        criteria.accept(CriteriaIndexBuilder.builder()
                .indexGroup(indexGroup)
                .build());
        indexGroup.getAllCriterias()
                .put(criteria.getId(), criteria);
    }

    public void add(final String index, final List<Criteria> criterias) {
        final IndexGroup indexGroup = get(index);
        criterias.forEach(criteria -> {
            criteria.accept(CriteriaIndexBuilder.builder()
                    .indexGroup(indexGroup)
                    .build());
            indexGroup.getAllCriterias()
                    .put(criteria.getId(), criteria);
        });
    }

    public void replace(final String oldIndex, final String newIndex) {
        final boolean oldIndexExists = indexMap.containsKey(oldIndex);
        final boolean newIndexExists = indexMap.containsKey(newIndex);
        if (oldIndexExists && newIndexExists) {
            if (indexMap.replace(oldIndex, getIndexGroup(oldIndex), getIndexGroup(newIndex))) {
                getIndexGroup(oldIndex).setName(oldIndex);
                indexMap.remove(newIndex, getIndexGroup(newIndex));
            }
        } else if (!oldIndexExists && newIndexExists) {
            indexMap.put(oldIndex, getIndexGroup(newIndex));
            getIndexGroup(oldIndex).setName(oldIndex);
            indexMap.remove(newIndex, getIndexGroup(newIndex));
        } else if (oldIndexExists && !newIndexExists) {
            indexMap.remove(oldIndex);
        }
    }

    public IndexGroup getIndexGroup(final String index) {
        if (indexMap.containsKey(index)) {
            return indexMap.get(index);
        }
        throw MustangException.builder()
                .errorCode(ErrorCode.INDEX_NOT_FOUND)
                .build();
    }

    private IndexGroup get(final String index) {
        return indexMap.computeIfAbsent(index,
                x -> IndexGroup.builder()
                        .name(index)
                        .build());
    }

}
