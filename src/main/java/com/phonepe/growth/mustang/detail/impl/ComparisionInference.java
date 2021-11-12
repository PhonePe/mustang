package com.phonepe.growth.mustang.detail.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class ComparisionInference implements CheckType.Visitor<Boolean> {
    private final int comparisionResult;
    private final boolean exclude;

    @Override
    public Boolean visitAbove() {
        return exclude ? comparisionResult < 0 : comparisionResult <= 0;
    }

    @Override
    public Boolean visitBelow() {
        return exclude ? comparisionResult > 0 : comparisionResult >= 0;
    }
}