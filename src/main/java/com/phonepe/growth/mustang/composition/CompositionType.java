package com.phonepe.growth.mustang.composition;

import lombok.Getter;

public enum CompositionType {
    AND(CompositionType.AND_TEXT),
    OR(CompositionType.OR_TEXT);

    public static final String AND_TEXT = "AND";
    public static final String OR_TEXT = "OR";

    @Getter
    private String value;

    private CompositionType(String value) {
        this.value = value;
    }

}
