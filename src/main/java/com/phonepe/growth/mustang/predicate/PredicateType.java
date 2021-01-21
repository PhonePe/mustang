package com.phonepe.growth.mustang.predicate;

import lombok.Getter;

public enum PredicateType {
    EXCLUDED(PredicateType.EXCLUDED_TEXT),
    INCLUDED(PredicateType.INCLUDED_TEXT);

    public static final String EXCLUDED_TEXT = "EXCLUDED";
    public static final String INCLUDED_TEXT = "INCLUDED";

    @Getter
    private String value;

    private PredicateType(String value) {
        this.value = value;
    }

}
