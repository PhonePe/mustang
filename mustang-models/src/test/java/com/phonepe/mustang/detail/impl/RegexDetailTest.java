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

public class RegexDetailTest {

    @Test
    public void testValidateMatchingRegex() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertTrue(detail.validate("Apple"));
    }

    @Test
    public void testValidateNonMatchingRegex() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertFalse(detail.validate("Banana"));
    }

    @Test
    public void testValidateWithNull() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertFalse(detail.validate(null));
    }

    @Test
    public void testValidateWithNonString() {
        RegexDetail detail = RegexDetail.builder().regex("123").build();
        Assert.assertFalse(detail.validate(123));
    }

    @Test
    public void testIsValidRegexDetailWithValidRegex() {
        RegexDetail detail = RegexDetail.builder().regex("A.*").build();
        Assert.assertTrue(detail.isValidRegexDetail());
    }

    @Test
    public void testIsValidRegexDetailWithInvalidRegex() {
        RegexDetail detail = RegexDetail.builder().regex("(.*([0-9])$").build();
        Assert.assertFalse(detail.isValidRegexDetail());
    }

    @Test
    public void testValidateComplexRegex() {
        RegexDetail detail = RegexDetail.builder().regex("^[a-z]+@[a-z]+\\.[a-z]+$").build();
        Assert.assertTrue(detail.validate("test@example.com"));
        Assert.assertFalse(detail.validate("INVALID"));
    }
}

