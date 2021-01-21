package com.phonepe.growth.mustang.traverse;

import com.phonepe.growth.mustang.criteria.CriteriaForm;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DebugResult {
    private boolean result;
    private CriteriaForm form;
    private List<CompositionDebugResult> compositionDebugResults;
}
