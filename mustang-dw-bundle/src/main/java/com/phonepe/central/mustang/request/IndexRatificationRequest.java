package com.phonepe.central.mustang.request;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexRatificationRequest {
    @NotBlank
    private String indexName;
    private boolean fullFledged;
}
