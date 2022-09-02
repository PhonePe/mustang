/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.search.handler;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Maps;
import com.phonepe.mustang.criteria.CriteriaForm;
import com.phonepe.mustang.index.group.IndexGroup;
import com.phonepe.mustang.json.JsonUtils;
import com.phonepe.mustang.search.Query;
import com.phonepe.mustang.search.matcher.CNFMatcher;
import com.phonepe.mustang.search.matcher.DNFMatcher;
import com.phonepe.mustang.search.matcher.Matches;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriteriaSearchHandler implements CriteriaForm.Visitor<Matches> {
    @NotNull
    private final IndexGroup indexGroup;
    @Valid
    @NotNull
    private final Query query;
    private final boolean score;
    private final Map<String, Object> pathValues = Maps.newHashMap();

    public Map<String, Double> handle() {
        extractValuesForPaths();
        final Map<String, Double> searchResults = Stream.of(CriteriaForm.values())
                .map(cForm -> cForm.accept(this))
                .map(Matches::getProbables)
                .flatMap(map -> map.entrySet()
                        .stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> o));
        final Map<String, Double> tautologicalResults = indexGroup.getTautologicalCriterias()
                .keySet()
                .stream()
                .collect(Collectors.toMap(x -> x, x -> 0.0));
        return Stream.of(searchResults, tautologicalResults)
                .flatMap(map -> map.entrySet()
                        .stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> o));
    }

    @Override
    public Matches visitDNF() {
        return Matches.builder()
                .probables(DNFMatcher.builder()
                        .invertedIndex(indexGroup.getDnfInvertedIndex())
                        .query(query)
                        .allCriterias(indexGroup.getAllCriterias())
                        .pathValues(pathValues)
                        .score(score)
                        .build()
                        .getMatches())
                .build();
    }

    @Override
    public Matches visitCNF() {
        return Matches.builder()
                .probables(CNFMatcher.builder()
                        .invertedIndex(indexGroup.getCnfInvertedIndex())
                        .query(query)
                        .allCriterias(indexGroup.getAllCriterias())
                        .pathValues(pathValues)
                        .score(score)
                        .build()
                        .getMatches())
                .build();
    }

    private void extractValuesForPaths() {
        indexGroup.getAllPaths()
                .entrySet()
                .forEach(entry -> pathValues.put(entry.getKey(),
                        JsonUtils.getNodeValue(query.getParsedContext(), entry.getValue(), null)));
    }

}
