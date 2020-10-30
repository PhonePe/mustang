package com.phonepe.growth.mustang.predicate;

import lombok.Getter;

public enum PredicateType {
    EXCLUDED(PredicateType.EXCLUDED_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitExcluded();
        }
    },
    INCLUDED(PredicateType.INCLUDED_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitIncluded();
        }
    };

    public static final String EXCLUDED_TEXT = "EXCLUDED";
    public static final String INCLUDED_TEXT = "INCLUDED";

    @Getter
    private String value;

    private PredicateType(String value) {
        this.value = value;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {

        T visitExcluded();

        T visitIncluded();

    }

}
