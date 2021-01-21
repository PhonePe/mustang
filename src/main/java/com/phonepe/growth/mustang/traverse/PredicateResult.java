package com.phonepe.growth.mustang.traverse;

import com.phonepe.growth.mustang.predicate.PredicateType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class PredicateResult {
    private boolean result;
    private PredicateType type;
    private String lhs;
    private Object lhsValue;
    private Set<?> values;
}
