package com.phonepe.central;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.phonepe.central.mustang.response.MustangExceptionMapper;
import com.phonepe.central.mustang.exception.ErrorCode;
import com.phonepe.central.mustang.exception.MustangException;

public class MustangExceptionMapperTest {
    private MustangExceptionMapper exceptionMapper = new MustangExceptionMapper();

    @Test
    public void priliminaryTest() {
        Response response = exceptionMapper.toResponse(new MustangException(ErrorCode.INTERNAL_ERROR, new Throwable()));
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                response.getStatusInfo()
                        .getStatusCode());

        response = exceptionMapper.toResponse(new MustangException(ErrorCode.INDEX_NOT_FOUND, new Throwable()));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatusInfo()
                        .getStatusCode());

        response = exceptionMapper.toResponse(new MustangException(ErrorCode.INDEX_GENERATION_ERROR, new Throwable()));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatusInfo()
                        .getStatusCode());

        response = exceptionMapper.toResponse(new MustangException(ErrorCode.CORRUPTED_JSON_ERROR, new Throwable()));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatusInfo()
                        .getStatusCode());

        response = exceptionMapper.toResponse(new MustangException(ErrorCode.INDEX_EXPORT_ERROR, new Throwable()));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatusInfo()
                        .getStatusCode());

        response = exceptionMapper.toResponse(new MustangException(ErrorCode.INDEX_IMPORT_ERROR, new Throwable()));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatusInfo()
                        .getStatusCode());

        response = exceptionMapper.toResponse(new MustangException(ErrorCode.INDEX_GROUP_EXISTS, new Throwable()));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatusInfo()
                        .getStatusCode());

    }
}
