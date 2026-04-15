package com.phonepe.central.mustang.detail.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class EqualSetDetailTest {

    @Test
    public void testValidateEqualSets() {
        EqualSetDetail detail = EqualSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList("A", "B")));
    }

    @Test
    public void testValidateNotEqualSets() {
        EqualSetDetail detail = EqualSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList("A", "B", "C")));
    }

    @Test
    public void testValidateSubsetNotEqual() {
        EqualSetDetail detail = EqualSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B", "C")))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList("A", "B")));
    }

    @Test
    public void testValidateNonCollection() {
        EqualSetDetail detail = EqualSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A")))
                .build();
        Assert.assertFalse(detail.validate("A"));
    }
}

