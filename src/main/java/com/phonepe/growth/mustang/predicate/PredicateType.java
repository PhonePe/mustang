package com.phonepe.growth.mustang.predicate;

import lombok.Getter;

public enum PredicateType {
    INCLUDED(PredicateType.INCLUDED_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitIncluded();
        }
    },
    EXCLUDED(PredicateType.EXCLUDED_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitExcluded();
        }
    };

    public static final String INCLUDED_TEXT = "INCLUDED";
    public static final String EXCLUDED_TEXT = "EXCLUDED";

    @Getter
    private String value;

    private PredicateType(String value) {
        this.value = value;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {

        T visitIncluded();

        T visitExcluded();

    }

}
