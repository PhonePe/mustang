package com.phonepe.growth.mustang.traverse;

import com.phonepe.growth.mustang.composition.CompositionType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompositionDebugResult {
    private boolean result;
    private CompositionType type;
    private List<PredicateDebugResult> predicateDebugResults;
}
