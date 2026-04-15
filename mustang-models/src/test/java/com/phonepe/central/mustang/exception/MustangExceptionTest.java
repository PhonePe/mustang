package com.phonepe.central.mustang.exception;

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

