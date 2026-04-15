package com.phonepe.central.mustang.detail.impl;

import org.junit.Assert;
import org.junit.Test;

public class RangeDetailTest {

    @Test
    public void testValidateWithinRange() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .includeLowerBound(true)
                .includeUpperBound(true)
                .build();
        Assert.assertTrue(detail.validate(5));
    }

    @Test
    public void testValidateAtLowerBoundInclusive() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .includeLowerBound(true)
                .includeUpperBound(false)
                .build();
        Assert.assertTrue(detail.validate(1));
    }

    @Test
    public void testValidateAtLowerBoundExclusive() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .includeLowerBound(false)
                .includeUpperBound(false)
                .build();
        Assert.assertFalse(detail.validate(1));
    }

    @Test
    public void testValidateAtUpperBoundInclusive() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .includeLowerBound(false)
                .includeUpperBound(true)
                .build();
        Assert.assertTrue(detail.validate(10));
    }

    @Test
    public void testValidateAtUpperBoundExclusive() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .includeLowerBound(false)
                .includeUpperBound(false)
                .build();
        Assert.assertFalse(detail.validate(10));
    }

    @Test
    public void testValidateOutsideRange() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .includeLowerBound(true)
                .includeUpperBound(true)
                .build();
        Assert.assertFalse(detail.validate(11));
        Assert.assertFalse(detail.validate(0));
    }

    @Test
    public void testValidateWithNull() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .build();
        Assert.assertFalse(detail.validate(null));
    }

    @Test
    public void testValidateWithNonNumber() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1)
                .upperBound(10)
                .build();
        Assert.assertFalse(detail.validate("five"));
    }

    @Test
    public void testGreaterThanEquals() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(3)
                .includeLowerBound(true)
                .build();
        Assert.assertTrue(detail.validate(3));
        Assert.assertTrue(detail.validate(100));
        Assert.assertFalse(detail.validate(2));
    }

    @Test
    public void testLessThan() {
        RangeDetail detail = RangeDetail.builder()
                .upperBound(3)
                .build();
        Assert.assertTrue(detail.validate(2));
        Assert.assertFalse(detail.validate(3));
    }

    @Test
    public void testValidateWithDouble() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(0.5)
                .upperBound(1.5)
                .includeLowerBound(true)
                .includeUpperBound(true)
                .build();
        Assert.assertTrue(detail.validate(1.0));
    }

    @Test
    public void testNormalisedView() {
        RangeDetail detail = RangeDetail.builder()
                .lowerBound(1.0)
                .upperBound(10.0)
                .includeLowerBound(true)
                .includeUpperBound(false)
                .build();
        String normalised = detail.getNormalisedView();
        RangeDetail reconstructed = RangeDetail.of(normalised);
        Assert.assertEquals(detail.getLowerBound().doubleValue(), reconstructed.getLowerBound().doubleValue(), 0.0001);
        Assert.assertEquals(detail.getUpperBound().doubleValue(), reconstructed.getUpperBound().doubleValue(), 0.0001);
        Assert.assertEquals(detail.isIncludeLowerBound(), reconstructed.isIncludeLowerBound());
        Assert.assertEquals(detail.isIncludeUpperBound(), reconstructed.isIncludeUpperBound());
    }
}

