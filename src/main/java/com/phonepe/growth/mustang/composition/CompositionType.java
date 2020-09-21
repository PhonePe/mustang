package com.phonepe.growth.mustang.composition;

import lombok.Getter;

public enum CompositionType {
    AND(CompositionType.AND_TEXT) {
        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visitAnd();
        }
    },
    OR(CompositionType.OR_TEXT) {
        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visitOr();
        }
    };

    public static final String AND_TEXT = "AND";
    public static final String OR_TEXT = "OR";

    @Getter
    private String value;

    private CompositionType(String value) {
        this.value = value;
    }

    protected abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {

        T visitAnd();

        T visitOr();
    }

}
