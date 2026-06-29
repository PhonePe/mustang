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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IntersectionDetailTest {

    @Test
    public void testValidateIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList("B", "C")));
    }

    @Test
    public void testValidateFullIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList("A", "B")));
    }

    @Test
    public void testValidateNoIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList("C", "D")));
    }

    @Test
    public void testValidateNonCollection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList("A", "B")))
                .build();
        Assert.assertFalse(detail.validate("A"));
    }

    @Test
    public void testValidateIntegerIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList(1, 2, 3)))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList(3, 4, 5)));
    }

    @Test
    public void testValidateIntegerNoIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList(1, 2, 3)))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList(4, 5, 6)));
    }

    @Test
    public void testValidateFloatingPointIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList(1.5, 2.5, 3.5)))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList(3.5, 4.5)));
    }

    @Test
    public void testValidateFloatingPointNoIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList(1.5, 2.5, 3.5)))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList(4.5, 5.5)));
    }

    @Test
    public void testValidateBooleanIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(List.of(true)))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList(true, false)));
    }

    @Test
    public void testValidateBooleanNoIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(List.of(true)))
                .build();
        Assert.assertFalse(detail.validate(List.of(false)));
    }

    @Test
    public void testValidateMixedTypeNoIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList(1, "A", true)))
                .build();
        Assert.assertFalse(detail.validate(Arrays.asList(2, "B", false)));
    }

    @Test
    public void testValidateMixedTypeIntersection() {
        IntersectionDetail detail = IntersectionDetail.builder()
                .values(new HashSet<>(Arrays.asList(1, "A", true)))
                .build();
        Assert.assertTrue(detail.validate(Arrays.asList(2, "A", false)));
    }
}


