package com.phonepe.central.mustang.detail.impl;

import org.junit.Assert;
import org.junit.Test;

public class ComparisionInferenceTest {

    @Test
    public void testVisitAboveWhenLessThanZero() {
        // comparisionResult < 0 means base < lhs, i.e. lhs is above base
        ComparisionInference inference = new ComparisionInference(-1, false);
        Assert.assertTrue(inference.visitAbove());
    }

    @Test
    public void testVisitAboveWhenEqualNoExclude() {
        ComparisionInference inference = new ComparisionInference(0, false);
        Assert.assertTrue(inference.visitAbove());
    }

    @Test
    public void testVisitAboveWhenEqualExclude() {
        ComparisionInference inference = new ComparisionInference(0, true);
        Assert.assertFalse(inference.visitAbove());
    }

    @Test
    public void testVisitAboveWhenGreaterThanZero() {
        // comparisionResult > 0 means base > lhs, i.e. lhs is below base
        ComparisionInference inference = new ComparisionInference(1, false);
        Assert.assertFalse(inference.visitAbove());
    }

    @Test
    public void testVisitBelowWhenGreaterThanZero() {
        ComparisionInference inference = new ComparisionInference(1, false);
        Assert.assertTrue(inference.visitBelow());
    }

    @Test
    public void testVisitBelowWhenEqualNoExclude() {
        ComparisionInference inference = new ComparisionInference(0, false);
        Assert.assertTrue(inference.visitBelow());
    }

    @Test
    public void testVisitBelowWhenEqualExclude() {
        ComparisionInference inference = new ComparisionInference(0, true);
        Assert.assertFalse(inference.visitBelow());
    }

    @Test
    public void testVisitBelowWhenLessThanZero() {
        ComparisionInference inference = new ComparisionInference(-1, false);
        Assert.assertFalse(inference.visitBelow());
    }
}

