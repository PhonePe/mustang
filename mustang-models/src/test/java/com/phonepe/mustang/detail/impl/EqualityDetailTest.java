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

