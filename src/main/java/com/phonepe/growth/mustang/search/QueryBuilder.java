package com.phonepe.growth.mustang.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.json.FlattenedJson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryBuilder {

    public static Query buildQuery(ObjectMapper mapper, final RequestContext context) {
        return Query.builder()
                .assigment(FlattenedJson.builder().node(context.getNode()).mapper(mapper).build().flatten())
                .context(context)
                .build();
    }

}
