package com.phonepe.growth.mustang;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.debug.DebugResult;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.index.IndexingFacade;
import com.phonepe.growth.mustang.scan.Scanner;
import com.phonepe.growth.mustang.search.Query;
import com.phonepe.growth.mustang.search.QueryBuilder;
import com.phonepe.growth.mustang.search.SearchFacade;
import com.phonepe.growth.mustang.search.ranking.RankingStrategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MustangEngine {
    @Valid
    @NotNull
    private ObjectMapper mapper;
    @Builder.Default
    private RankingStrategy rankingStrategy = RankingStrategy.EXPLICIT_WEIGHTS;
    private final IndexingFacade indexingFacde = IndexingFacade.builder()
            .build();
    private final SearchFacade searchFacade = SearchFacade.builder()
            .indexingFacade(indexingFacde)
            .build();

    public synchronized void index(final String indexName, final Criteria criteria) {
        indexingFacde.add(indexName, criteria);
    }

    public synchronized void index(final String indexName, final List<Criteria> criterias) {
        indexingFacde.add(indexName, criterias);
    }

    public synchronized void replace(final String oldIndex, final String newIndex) {
        indexingFacde.replace(oldIndex, newIndex);
    }

    public Set<String> search(final String indexName, final RequestContext context) {
        return search(indexName, context, -1);
    }

    public Set<String> search(final String indexName, final RequestContext context, final int topN) {
        final Query query = QueryBuilder.buildQuery(mapper, context);
        return searchFacade.search(indexName, query, topN);
    }

    public List<Criteria> scan(final List<Criteria> criterias, final RequestContext context) {
        return Scanner.builder()
                .criterias(criterias)
                .context(context)
                .build()
                .scan();
    }

    public boolean evaluate(final Criteria criteria, final RequestContext context) {
        return criteria.evaluate(context);
    }

    public DebugResult debug(final Criteria criteria, final RequestContext context) {
        return criteria.debug(context);
    }

    public double score(final Criteria criteria, final RequestContext context) {
        if (criteria.evaluate(context)) {
            return criteria.getScore(context);
        }
        return -1.0; // negative score to indicate unmatched criteria.
    }

    public List<Pair<String, Double>> score(final List<Criteria> criterias, final RequestContext context) {
        return criterias.stream()
                .sequential()
                .map(criteria -> {
                    if (criteria.evaluate(context)) {
                        return Pair.of(criteria.getId(), criteria.getScore(context));
                    }
                    return Pair.of(criteria.getId(), -1.0); // negative score to indicate unmatched criteria.
                })
                .collect(Collectors.toList());
    }

}
