package com.phonepe.central.mustang.detail.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class EqualityDetailTest {

    @Test
    public void testValidateWithMatchingString() {
        EqualityDetail detail = EqualityDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B", "C")))
                .build();
        Assert.assertTrue(detail.validate("A"));
    }

    @Test
    public void testValidateWithNonMatchingString() {
        EqualityDetail detail = EqualityDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B", "C")))
                .build();
        Assert.assertFalse(detail.validate("D"));
    }

    @Test
    public void testValidateWithNumber() {
        Set<Object> values = new HashSet<>();
        values.add(1);
        values.add(2);
        values.add(3);
        EqualityDetail detail = EqualityDetail.builder().values(values).build();
        Assert.assertTrue(detail.validate(1.0));
    }

    @Test
    public void testValidateWithBoolean() {
        Set<Object> values = new HashSet<>();
        values.add(true);
        EqualityDetail detail = EqualityDetail.builder().values(values).build();
        Assert.assertTrue(detail.validate(true));
        Assert.assertFalse(detail.validate(false));
    }

    @Test
    public void testValidateWithNull() {
        EqualityDetail detail = EqualityDetail.builder()
                .values(new HashSet<>(Arrays.asList("A")))
                .build();
        Assert.assertFalse(detail.validate(null));
    }

    @Test
    public void testValidateWithList() {
        EqualityDetail detail = EqualityDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B", "C")))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList("A", "B")));
        Assert.assertFalse(detail.validate(Arrays.asList("A", "D")));
    }
}

