package com.phonepe.growth.mustang.search;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.json.FlattenedJson;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Searcher {
    @NotBlank
    private String index;
    @Valid
    @NotNull
    private EvaluationContext context;

    public List<Criteria> search() {
        // TODO
        Map<String, Object> flattenedJson = FlattenedJson.builder().node(context.getNode()).mapper(null).build()
                .flatten();

        return null;

    }

}
