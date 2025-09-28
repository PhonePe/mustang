package com.phonepe.growth.mustang.similarity;

import java.util.List;

import com.phonepe.growth.mustang.criteria.Criteria;

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
    private List<Criteria> similarCriterias;
}
