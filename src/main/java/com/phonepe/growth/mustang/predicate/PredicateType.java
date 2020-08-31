package com.phonepe.growth.mustang.predicate;

import lombok.Getter;

public enum PredicateType {
    IN(PredicateType.IN_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitIn();
        }
    },
    NOT_IN(PredicateType.NOT_IN_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitNotIn();
        }
    },
    AND(PredicateType.AND_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitAnd();
        }
    },
    OR(PredicateType.OR_TEXT) {
        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitOr();
        }
    };

    public static final String IN_TEXT = "IN";
    public static final String NOT_IN_TEXT = "NOT_IN";
    public static final String AND_TEXT = "AND";
    public static final String OR_TEXT = "OR";

    @Getter
    private String value;

    private PredicateType(String value) {
        this.value = value;
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public interface Visitor<T> {

        T visitIn();

        T visitNotIn();

        T visitAnd();

        T visitOr();

    }

}
