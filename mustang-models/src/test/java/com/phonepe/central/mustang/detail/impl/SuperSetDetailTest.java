package com.phonepe.central.mustang.detail.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class SuperSetDetailTest {

    @Test
    public void testValidateSuperSet() {
        SuperSetDetail detail = SuperSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList("A", "B", "C")));
    }

    @Test
    public void testValidateNotSuperSet() {
        SuperSetDetail detail = SuperSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList("A")));
    }

    @Test
    public void testValidateNonCollection() {
        SuperSetDetail detail = SuperSetDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertFalse(detail.validate("A"));
    }
}

