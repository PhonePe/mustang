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

package com.phonepe.mustang.response;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.phonepe.mustang.exception.MustangException;

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