package com.phonepe.growth.mustang.scan;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Scanner {
    @NotEmpty
    private List<Criteria> criterias;
    @Valid
    @NotNull
    private RequestContext context;

    public List<Criteria> scan() {
        return criterias.stream()
                .filter(criteria -> criteria.evaluate(context))
                .collect(Collectors.toList());
    }

}
