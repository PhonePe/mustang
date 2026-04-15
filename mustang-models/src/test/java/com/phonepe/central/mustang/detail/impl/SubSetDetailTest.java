package com.phonepe.central.mustang.detail.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class SubSetDetailTest {

    @Test
    public void testValidateSubSet() {
        SubSetDetail detail = SubSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B", "C")))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList("A", "B")));
    }

    @Test
    public void testValidateNotSubSet() {
        SubSetDetail detail = SubSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B", "C")))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList("A", "D")));
    }

    @Test
    public void testValidateNonCollection() {
        SubSetDetail detail = SubSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertFalse(detail.validate("A"));
    }
}

