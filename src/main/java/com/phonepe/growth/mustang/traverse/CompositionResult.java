package com.phonepe.growth.mustang.traverse;

import com.phonepe.growth.mustang.composition.CompositionType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompositionResult {
    private boolean result;
    private CompositionType type;
    private List<PredicateResult> predicateResults;
}
