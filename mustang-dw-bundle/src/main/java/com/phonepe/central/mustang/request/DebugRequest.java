package com.phonepe.central.mustang.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebugRequest {
    @Valid
    @NotNull
    private Criteria criteria;
    @Valid
    @NotNull
    private RequestContext requestContext;

}
