package com.phonepe.central.mustang.detail.impl;

import org.junit.Assert;
import org.junit.Test;

public class RegexDetailTest {

    @Test
    public void testValidateMatchingRegex() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertTrue(detail.validate("Apple"));
    }

    @Test
    public void testValidateNonMatchingRegex() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertFalse(detail.validate("Banana"));
    }

    @Test
    public void testValidateWithNull() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertFalse(detail.validate(null));
    }

    @Test
    public void testValidateWithNonString() {
        RegexDetail detail = RegexDetail.builder().regex("123").build();
        Assert.assertFalse(detail.validate(123));
    }

    @Test
    public void testIsValidRegexDetailWithValidRegex() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertTrue(detail.isValidRegexDetail());
    }

    @Test
    public void testIsValidRegexDetailWithInvalidRegex() {
        RegexDetail detail = RegexDetail.builder().regex("(.*([0-9])$").build();
        Assert.assertFalse(detail.isValidRegexDetail());
    }

    @Test
    public void testValidateComplexRegex() {
        RegexDetail detail = RegexDetail.builder().regex("^[a-z]+@[a-z]+\\.[a-z]+$").build();
        Assert.assertTrue(detail.validate("test@example.com"));
        Assert.assertFalse(detail.validate("INVALID"));
    }
}

