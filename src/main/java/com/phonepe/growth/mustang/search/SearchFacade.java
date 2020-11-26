package com.phonepe.growth.mustang.search;

import java.util.Comparator;
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
        final Map<String, Double> result = CriteriaSearchHandler.builder()
                .indexGroup(indexingFacade.getIndexGroup(indexName))
                .query(query)
                .build()
                .handle();
        if (topN == -1) {
            return result.keySet();
        }
        return result.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
