package com.phonepe.growth.mustang.search;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.Indexer;
import com.phonepe.growth.mustang.json.FlattenedJson;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Searcher {
    @NotBlank
    private String indexName;
    @Valid
    @NotNull
    private EvaluationContext context;
    @Valid
    @NotNull
    private ObjectMapper mapper;

    public List<String> search() {
        final Map<String, Object> flattenedJson = FlattenedJson.builder().node(context.getNode()).mapper(mapper).build()
                .flatten();
        return Stream.of(CriteriaForm.values())
                .map(criteria -> criteria.accept(
                        CriteriaSearchHelper.builder().index(Indexer.get(indexName)).assignment(flattenedJson).build()))
                .flatMap(List::stream).collect(Collectors.toList());
    }

}
