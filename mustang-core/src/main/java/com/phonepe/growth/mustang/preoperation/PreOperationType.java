package com.phonepe.growth.mustang.preoperation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PreOperationType {

    IDENTITY(PreOperationType.IDENTITY_TEXT),
    ADDITION(PreOperationType.ADDITION_TEXT),
    SUBTRACTION(PreOperationType.SUBTRACTION_TEXT),
    MULTIPLICATION(PreOperationType.MULTIPLCATION_TEXT),
    DIVISION(PreOperationType.DIVISION_TEXT),
    MODULO(PreOperationType.MODULO_TEXT),
    BINARY_CONVERSION(PreOperationType.BINARY_CONVERSION_TEXT),

    SIZE(PreOperationType.SIZE_TEXT),

    LENGTH(PreOperationType.LENGTH_TEXT),
    SUBSTRING(PreOperationType.SUBSTRING_TEXT),

    TO_DATETIME(PreOperationType.TO_DATETIME_TEXT),
    FROM_DATETIME(PreOperationType.FROM_DATETIME_TEXT);

    @Getter
    private String value;

    public static final String IDENTITY_TEXT = "IDENTITY";
    public static final String ADDITION_TEXT = "ADDITION";
    public static final String SUBTRACTION_TEXT = "SUBTRACTION";
    public static final String MULTIPLCATION_TEXT = "MULTIPLCATION";
    public static final String DIVISION_TEXT = "DIVISION";
    public static final String MODULO_TEXT = "MODULO";
    public static final String BINARY_CONVERSION_TEXT = "BINARY_CONVERSION";
    public static final String SIZE_TEXT = "SIZE";
    public static final String LENGTH_TEXT = "LENGTH";
    public static final String SUBSTRING_TEXT = "SUBSTRING";
    public static final String TO_DATETIME_TEXT = "TO_DATETIME";
    public static final String FROM_DATETIME_TEXT = "FROM_DATETIME";

}
