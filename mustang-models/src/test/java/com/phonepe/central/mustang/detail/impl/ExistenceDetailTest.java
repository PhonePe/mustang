package com.phonepe.central.mustang.detail.impl;

import org.junit.Assert;
import org.junit.Test;

public class ExistenceDetailTest {

    @Test
    public void testValidateWithNonNull() {
        ExistenceDetail detail = ExistenceDetail.builder().build();
        Assert.assertTrue(detail.validate("present"));
    }

    @Test
    public void testValidateWithNull() {
        ExistenceDetail detail = ExistenceDetail.builder().build();
        Assert.assertFalse(detail.validate(null));
    }

    @Test
    public void testValidateWithNumber() {
        ExistenceDetail detail = ExistenceDetail.builder().build();
        Assert.assertTrue(detail.validate(42));
    }
}

