/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.search;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.index.IndexingFacade;
import com.phonepe.growth.mustang.search.handler.CriteriaSearchHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SearchFacade {
    @NotNull
    private final IndexingFacade indexingFacade;

    public Set<String> search(final String indexName, final Query query, final int topN) {
        final boolean score = topN != -1;
        final Map<String, Double> result = CriteriaSearchHandler.builder()
                .indexGroup(indexingFacade.getIndexGroup(indexName))
                .query(query)
                .score(score)
                .build()
                .handle();
        if (!score) {
            return result.keySet();
        }

        return result.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(topN == -1 ? result.size() : topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
