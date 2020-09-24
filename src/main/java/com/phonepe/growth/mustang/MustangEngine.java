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

    public void index(String indexName, Criteria criteria) {
        IndexingFacade.add(indexName, criteria);
    }

    public void index(String indexName, List<Criteria> criterias) {
        IndexingFacade.add(indexName, criterias);
    }

    public List<String> search(String indexName, EvaluationContext context) {
        final Query query = QueryBuilder.buildQuery(mapper, context);
        return SearchFacade.search(indexName, query);
    }

    public List<Criteria> scan(List<Criteria> criterias, EvaluationContext context) {
        return Scanner.builder().criterias(criterias).context(context).build().scan();
    }

    public boolean evaluate(Criteria criteria, EvaluationContext context) {
        return criteria.evaluate(context);
    }

}
