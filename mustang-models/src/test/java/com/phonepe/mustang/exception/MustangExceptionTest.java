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

package com.phonepe.mustang.exception;

import org.junit.Assert;
import org.junit.Test;

public class MustangExceptionTest {

    @Test
    public void testMustangExceptionBuilder() {
        RuntimeException cause = new RuntimeException("test");
        MustangException ex = MustangException.builder()
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .cause(cause)
                .build();
        Assert.assertEquals(ErrorCode.INTERNAL_ERROR, ex.getErrorCode());
        Assert.assertEquals(cause, ex.getCause());
    }

    @Test
    public void testPropagateWithMustangException() {
        MustangException original = MustangException.builder()
                .errorCode(ErrorCode.INDEX_NOT_FOUND)
                .build();
        MustangException result = MustangException.propagate(original);
        Assert.assertSame(original, result);
    }

    @Test
    public void testPropagateWithWrappedMustangException() {
        MustangException inner = MustangException.builder()
                .errorCode(ErrorCode.INDEX_NOT_FOUND)
                .build();
        RuntimeException wrapper = new RuntimeException(inner);
        MustangException result = MustangException.propagate(wrapper);
        Assert.assertSame(inner, result);
    }

    @Test
    public void testPropagateWithRegularException() {
        RuntimeException cause = new RuntimeException("regular");
        MustangException result = MustangException.propagate(cause);
        Assert.assertEquals(ErrorCode.INTERNAL_ERROR, result.getErrorCode());
        Assert.assertEquals(cause, result.getCause());
    }

    @Test
    public void testPropagateWithExplicitErrorCode() {
        RuntimeException cause = new RuntimeException("error");
        MustangException result = MustangException.propagate(ErrorCode.CORRUPTED_JSON_ERROR, cause);
        Assert.assertEquals(ErrorCode.CORRUPTED_JSON_ERROR, result.getErrorCode());
    }

    @Test
    public void testErrorCodeValues() {
        Assert.assertEquals(7, ErrorCode.values().length);
        Assert.assertNotNull(ErrorCode.valueOf("INTERNAL_ERROR"));
        Assert.assertNotNull(ErrorCode.valueOf("INDEX_NOT_FOUND"));
        Assert.assertNotNull(ErrorCode.valueOf("INDEX_GENERATION_ERROR"));
        Assert.assertNotNull(ErrorCode.valueOf("CORRUPTED_JSON_ERROR"));
        Assert.assertNotNull(ErrorCode.valueOf("INDEX_EXPORT_ERROR"));
        Assert.assertNotNull(ErrorCode.valueOf("INDEX_IMPORT_ERROR"));
        Assert.assertNotNull(ErrorCode.valueOf("INDEX_GROUP_EXISTS"));
    }
}

