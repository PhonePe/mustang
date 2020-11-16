package com.phonepe.growth.mustang.search;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.criteria.Criteria;
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

    public Set<String> search(final String indexName, final Query query) {
        return CriteriaSearchHandler.builder().index(indexingFacade.getIndexGroup(indexName)).query(query).build()
                .handle();
    }

    public Set<String> trimTopN(final Set<String> results, final String indexName, final EvaluationContext context,
            final int topN) {
        return results.stream().map(cId -> indexingFacade.getIndexGroup(indexName).getAllCriterias().get(cId))
                .sorted((c1, c2) -> Double.valueOf(c2.getScore(context)).compareTo(c1.getScore(context))) // descending
                .limit(topN).map(Criteria::getId).collect(Collectors.toSet());
    }

}
