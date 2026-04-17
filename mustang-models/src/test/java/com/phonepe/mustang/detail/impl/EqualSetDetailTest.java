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

