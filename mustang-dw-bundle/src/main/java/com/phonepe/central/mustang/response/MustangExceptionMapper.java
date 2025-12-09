package com.phonepe.central.mustang.response;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.phonepe.growth.mustang.exception.MustangException;

public class MustangExceptionMapper implements ExceptionMapper<MustangException> {
    @Override
    public Response toResponse(MustangException e) {
        return switch (e.getErrorCode()) {
        case INTERNAL_ERROR -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(getMustangException(e))
                .build();
        default -> Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(getMustangException(e))
                .build();
        };
    }

    private MustangResponse<String> getMustangException(final MustangException e) {
        return MustangResponse.<String>builder()
                .success(false)
                .errorCode(e.getErrorCode())
                .data(e.getMessage())
                .build();
    }
}