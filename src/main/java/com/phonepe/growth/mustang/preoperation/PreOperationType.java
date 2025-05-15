package com.phonepe.growth.mustang.preoperation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PreOperationType {

    IDENTITY(PreOperationType.IDENTITY_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitNoOp();
        }

    },
    ADDITION(PreOperationType.ADDITION_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitAddition();
        }

    },
    SUBTRACTION(PreOperationType.SUBTRACTION_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitSubtraction();
        }

    },
    MULTIPLICATION(PreOperationType.MULTIPLCATION_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitMultiplication();
        }

    },
    DIVISION(PreOperationType.DIVISION_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitDivision();
        }

    },
    MODULO(PreOperationType.MODULO_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitModulo();
        }

    },

    SIZE(PreOperationType.SIZE_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitSize();
        }

    },

    LENGTH(PreOperationType.LENGTH_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitLength();
        }

    },
    SUBSTRING(PreOperationType.SUBSTRING_TEXT) {

        @Override
        public <T> T visit(Visitor<T> visitor) {
            return visitor.visitSubstring();
        }

    };

    @Getter
    private String value;

    public static final String IDENTITY_TEXT = "IDENTITY";
    public static final String ADDITION_TEXT = "ADDITION";
    public static final String SUBTRACTION_TEXT = "SUBTRACTION";
    public static final String MULTIPLCATION_TEXT = "MULTIPLCATION";
    public static final String DIVISION_TEXT = "DIVISION";
    public static final String MODULO_TEXT = "MODULO";
    public static final String SIZE_TEXT = "SIZE";
    public static final String LENGTH_TEXT = "LENGTH";
    public static final String SUBSTRING_TEXT = "SUBSTRING";

    public abstract <T> T visit(Visitor<T> visitor);

    public interface Visitor<T> {

        T visitNoOp();

        T visitAddition();

        T visitSubtraction();

        T visitMultiplication();

        T visitDivision();

        T visitModulo();

        T visitSize();

        T visitLength();

        T visitSubstring();

    }

}
