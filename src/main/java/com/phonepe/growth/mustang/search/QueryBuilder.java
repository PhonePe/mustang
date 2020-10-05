package com.phonepe.growth.mustang.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.json.FlattenedJson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryBuilder {

    public static Query buildQuery(ObjectMapper mapper, final EvaluationContext context, int topN) {
        return Query.builder()
                .assigment(FlattenedJson.builder().node(context.getNode()).mapper(mapper).build().flatten())
                .topN(topN)
                .build();
    }

}
