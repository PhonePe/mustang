package com.phonepe.growth.mustang;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.index.IndexingFacade;
import com.phonepe.growth.mustang.scan.Scanner;
import com.phonepe.growth.mustang.search.Query;
import com.phonepe.growth.mustang.search.QueryBuilder;
import com.phonepe.growth.mustang.search.SearchFacade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MustangEngine {
    @Valid
    @NotNull
    private ObjectMapper mapper;
    private final IndexingFacade indexingFacde = IndexingFacade.builder().build();
    private final SearchFacade searchFacade = SearchFacade.builder().indexingFacade(indexingFacde).build();

    public synchronized void index(final String indexName, final Criteria criteria) {
        indexingFacde.add(indexName, criteria);
    }

    public synchronized void index(final String indexName, final List<Criteria> criterias) {
        indexingFacde.add(indexName, criterias);
    }

    public synchronized boolean replace(final String oldIndex, final String newIndex) {
        return indexingFacde.replace(oldIndex, newIndex);
    }

    public Set<String> search(final String indexName, final EvaluationContext context) {
        return search(indexName, context, -1);
    }

    public Set<String> search(final String indexName, final EvaluationContext context, final int topN) {
        final Query query = QueryBuilder.buildQuery(mapper, context);
        final Set<String> results = searchFacade.search(indexName, query);
        if (topN == -1) {
            return results;
        }
        return searchFacade.trimTopN(results, indexName, context, topN);
    }

    public List<Criteria> scan(final List<Criteria> criterias, final EvaluationContext context) {
        return Scanner.builder().criterias(criterias).context(context).build().scan();
    }

    public boolean evaluate(final Criteria criteria, final EvaluationContext context) {
        return criteria.evaluate(context);
    }

    public double score(final Criteria criteria, final EvaluationContext context) {
        if (criteria.evaluate(context)) {
            return criteria.getScore(context);
        }
        return -1.0; // negative score to indicate unmatched criteria.
    }

    public List<Pair<String, Double>> score(final List<Criteria> criterias, final EvaluationContext context) {
        return criterias.stream().sequential().map(criteria -> {
            if (criteria.evaluate(context)) {
                return Pair.of(criteria.getId(), criteria.getScore(context));
            }
            return Pair.of(criteria.getId(), -1.0); // negative score to indicate unmatched criteria.
        }).collect(Collectors.toList());
    }

}
