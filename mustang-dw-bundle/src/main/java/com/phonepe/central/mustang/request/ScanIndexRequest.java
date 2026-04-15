package com.phonepe.central.mustang.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.phonepe.central.mustang.common.RequestContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanIndexRequest {
    @NotBlank
    private String indexName;
    @Valid
    @NotNull
    private RequestContext requestContext;
}
