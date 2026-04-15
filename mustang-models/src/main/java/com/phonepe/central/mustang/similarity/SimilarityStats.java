package com.phonepe.central.mustang.similarity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityStats {
    private long overallScenarios;
    private long overlappingScenarios;
    private float similarityScore;
    private List<Similarity> similarities;
}
