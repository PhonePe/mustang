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
package com.phonepe.growth.mustang.index.builder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.criteria.CriteriaVisitor;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.criteria.tautology.CNFTautologicalCriteria;
import com.phonepe.growth.mustang.criteria.tautology.DNFTautologicalCriteria;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.index.operation.IndexOperation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaIndexBuilder implements CriteriaVisitor<Void> {
    @Valid
    @NotNull
    private IndexGroup indexGroup;
    @NotNull
    private IndexOperation operation;

    @Override
    public Void visit(DNFCriteria dnf) {
        if (dnf instanceof DNFTautologicalCriteria || dnf.getConjunctions()
                .isEmpty()) {
            indexGroup.getTautologicalCriterias()
                    .put(dnf.getId(), dnf);
        } else {
            final DNFIndexer dnfIndexer = DNFIndexer.builder()
                    .criteria(dnf)
                    .indexGroup(indexGroup)
                    .operation(operation)
                    .build();
            dnfIndexer.index();
        }
        return null;
    }

    @Override
    public Void visit(CNFCriteria cnf) {
        if (cnf instanceof CNFTautologicalCriteria || cnf.getDisjunctions()
                .isEmpty()) {
            indexGroup.getTautologicalCriterias()
                    .put(cnf.getId(), cnf);
        } else {
            final CNFIndexer cnfIndexer = CNFIndexer.builder()
                    .criteria(cnf)
                    .indexGroup(indexGroup)
                    .operation(operation)
                    .build();
            cnfIndexer.index();
        }
        return null;
    }

    public static <T, S> Map<T, TreeMap<Integer, S>> compactPostingLists(List<Map<T, TreeMap<Integer, S>>> maps) {
        final List<Map.Entry<T, TreeMap<Integer, S>>> tempResult = maps.stream()
                .collect(ArrayList::new, (set, map) -> set.addAll(map.entrySet()), (set1, set2) -> set1.addAll(set2));
        return tempResult.stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey, LinkedHashMap::new,
                    Collectors.mapping(Map.Entry::getValue, Collectors.reducing(new TreeMap<>(), (s1, s2) -> {
                        final TreeMap<Integer, S> combined = new TreeMap<>(s1);
                        combined.putAll(s2);
                        return combined;
                    }))));
    }

}
