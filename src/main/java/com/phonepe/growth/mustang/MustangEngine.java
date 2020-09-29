package com.phonepe.growth.mustang;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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

    public List<String> search(final String indexName, final EvaluationContext context) {
        return search(indexName, context, -1);
    }

    public List<String> search(final String indexName, final EvaluationContext context, final int topN) {
        final Query query = QueryBuilder.buildQuery(mapper, context, topN);
        return searchFacade.search(indexName, query);
    }

    public List<Criteria> scan(final List<Criteria> criterias, final EvaluationContext context) {
        return Scanner.builder().criterias(criterias).context(context).build().scan();
    }

    public boolean evaluate(final Criteria criteria, final EvaluationContext context) {
        return criteria.evaluate(context);
    }

}
