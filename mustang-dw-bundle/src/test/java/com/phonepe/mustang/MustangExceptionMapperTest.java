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

package com.phonepe.mustang;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.phonepe.mustang.response.MustangExceptionMapper;
import com.phonepe.mustang.exception.ErrorCode;
import com.phonepe.mustang.exception.MustangException;

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
