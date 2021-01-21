package com.phonepe.growth.mustang.debug;

import com.phonepe.growth.mustang.criteria.CriteriaForm;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DebugResult {
    private boolean result;
    private CriteriaForm form;
    private String id;
    private List<CompositionDebugResult> compositionDebugResults;
}
