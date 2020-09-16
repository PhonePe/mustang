package com.phonepe.growth.mustang;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.index.Indexer;
import com.phonepe.growth.mustang.scan.Scanner;
import com.phonepe.growth.mustang.search.Searcher;

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
        Indexer.add(indexName, criteria);
    }

    public void index(String indexName, List<Criteria> criterias) {
        Indexer.add(indexName, criterias);
    }

    public List<String> search(String indexName, EvaluationContext context) {
        return Searcher.builder().indexName(indexName).context(context).mapper(mapper).build().search();
    }

    public List<Criteria> scan(List<Criteria> criterias, EvaluationContext context) {
        return Scanner.builder().criterias(criterias).context(context).build().scan();
    }

    public boolean evaluate(Criteria criteria, EvaluationContext context) {
        return criteria.evaluate(context);
    }

}
