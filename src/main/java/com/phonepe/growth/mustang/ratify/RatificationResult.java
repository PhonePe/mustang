package com.phonepe.growth.mustang.ratify;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RatificationResult {
    private boolean ratified;
    private String indexName;
    private int combinations;
    private Set<RatificationDetail> anamolyDetails;
    private long timeTakenMs;

}
