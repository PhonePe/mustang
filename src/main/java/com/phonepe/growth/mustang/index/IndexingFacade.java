/**
 * Copyright (c) 2022 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.growth.mustang.index;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.exception.ErrorCode;
import com.phonepe.growth.mustang.exception.MustangException;
import com.phonepe.growth.mustang.index.builder.CriteriaIndexBuilder;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.index.operation.IndexOperation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndexingFacade {
    private final Map<String, IndexGroup> indexMap = Maps.newConcurrentMap();

    public void add(final String index, final Criteria criteria) {
        final IndexGroup indexGroup = get(index);
        if (indexGroup.getAllCriterias()
                .containsKey(criteria.getId())) {
            throw MustangException.builder()
                    .errorCode(ErrorCode.INDEX_GENERATION_ERROR)
                    .build();
        }
        criteria.accept(CriteriaIndexBuilder.builder()
                .indexGroup(indexGroup)
                .operation(IndexOperation.ADD)
                .build());
        indexGroup.getAllCriterias()
                .put(criteria.getId(), criteria);
    }

    public void add(final String index, final List<Criteria> criterias) {
        final IndexGroup indexGroup = get(index);
        criterias.forEach(criteria -> {
            if (indexGroup.getAllCriterias()
                    .containsKey(criteria.getId())) {
                throw MustangException.builder()
                        .errorCode(ErrorCode.INDEX_GENERATION_ERROR)
                        .build();
            }
            criteria.accept(CriteriaIndexBuilder.builder()
                    .indexGroup(indexGroup)
                    .operation(IndexOperation.ADD)
                    .build());
            indexGroup.getAllCriterias()
                    .put(criteria.getId(), criteria);
        });
    }

    public void update(final String index, final Criteria criteria) {
        final IndexGroup indexGroup = get(index);
        criteria.accept(CriteriaIndexBuilder.builder()
                .indexGroup(indexGroup)
                .operation(IndexOperation.UPDATE)
                .build());
        indexGroup.getAllCriterias()
                .put(criteria.getId(), criteria);
    }

    public void delete(final String index, final Criteria criteria) {
        final IndexGroup indexGroup = get(index);
        if (indexGroup.getAllCriterias()
                .containsKey(criteria.getId())) {
            criteria.accept(CriteriaIndexBuilder.builder()
                    .indexGroup(indexGroup)
                    .operation(IndexOperation.DELETE)
                    .build());
            indexGroup.getAllCriterias()
                    .remove(criteria.getId());
        } else {
            throw MustangException.builder()
                    .errorCode(ErrorCode.INDEX_NOT_FOUND)
                    .build();
        }
    }

    public void replace(final String oldIndex, final String newIndex) {
        if (indexMap.containsKey(newIndex)) {
            if (indexMap.containsKey(oldIndex)) {
                indexMap.replace(oldIndex, getIndexGroup(oldIndex), getIndexGroup(newIndex));
            } else {
                indexMap.put(oldIndex, getIndexGroup(newIndex));
            }
            getIndexGroup(oldIndex).setName(oldIndex);
            indexMap.remove(newIndex, getIndexGroup(newIndex));
        } else {
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

    public String exportIndexGroup(final String index, final ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(getIndexGroup(index).getAllCriterias()
                    .values());
        } catch (JsonProcessingException e) {
            throw MustangException.builder()
                    .errorCode(ErrorCode.INDEX_EXPORT_ERROR)
                    .cause(e)
                    .build();
        }
    }

    public String snapshot(final String index, final ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(getIndexGroup(index));
        } catch (JsonProcessingException e) {
            throw MustangException.builder()
                    .errorCode(ErrorCode.INTERNAL_ERROR)
                    .cause(e)
                    .build();
        }
    }

    public IndexGroup importIndexGroup(final String indexName, final String groupDetails, final ObjectMapper mapper) {
        if (indexMap.containsKey(indexName)) {
            throw MustangException.builder()
                    .errorCode(ErrorCode.INDEX_GROUP_EXISTS)
                    .build();
        }
        try {
            final List<Criteria> criterias = mapper.readValue(groupDetails, new TypeReference<List<Criteria>>() {
            });
            add(indexName, criterias);
        } catch (IOException e) {
            throw MustangException.builder()
                    .errorCode(ErrorCode.INDEX_IMPORT_ERROR)
                    .cause(e)
                    .build();
        }
        return getIndexGroup(indexName);
    }

    private IndexGroup get(final String index) {
        return indexMap.computeIfAbsent(index, x -> IndexGroup.builder()
                .name(index)
                .build());
    }

}
