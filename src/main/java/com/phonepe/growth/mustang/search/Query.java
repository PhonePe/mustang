package com.phonepe.growth.mustang.search;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.common.RequestContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Query {
    @Valid
    @NotEmpty
    private Map<String, Object> assigment;
    @NotNull
    private RequestContext context;

}
