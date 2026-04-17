/*
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonepe.mustang.detail.impl;

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

