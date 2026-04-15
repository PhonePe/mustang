package com.phonepe.central.mustang.detail.impl;

import org.junit.Assert;
import org.junit.Test;

public class NonExistenceDetailTest {

    @Test
    public void testValidateWithNull() {
        NonExistenceDetail detail = NonExistenceDetail.builder().build();
        Assert.assertTrue(detail.validate(null));
    }

    @Test
    public void testValidateWithNonNull() {
        NonExistenceDetail detail = NonExistenceDetail.builder().build();
        Assert.assertFalse(detail.validate("present"));
    }

    @Test
    public void testValidateWithNumber() {
        NonExistenceDetail detail = NonExistenceDetail.builder().build();
        Assert.assertFalse(detail.validate(0));
    }
}

