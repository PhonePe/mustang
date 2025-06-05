package com.phonepe.growth.mustang.debug;

import java.util.Collections;
import java.util.List;

import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.criteria.CriteriaForm;

import lombok.Builder;
import lombok.Data;

@Data
public class UNFDebugResult extends DebugResult {

    private CompositionType type;
    private List<PredicateDebugResult> predicateDebugResults;
    private List<DebugResult> debugResults;

    @Builder(builderMethodName = "UNFDebugResultBuilder")
    public UNFDebugResult(boolean result,
            CriteriaForm form,
            String id,
            CompositionType type,
            List<PredicateDebugResult> predicateDebugResults,
            List<DebugResult> debugResults) {
        super(result, form, id, Collections.emptyList());
        this.type = type;
        this.predicateDebugResults = predicateDebugResults;
        this.debugResults = debugResults;
    }

}
