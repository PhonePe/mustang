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

package com.phonepe.mustang.resources;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.phonepe.mustang.MustangBundle;
import com.phonepe.mustang.request.DebugRequest;
import com.phonepe.mustang.request.IndexExportRequest;
import com.phonepe.mustang.request.IndexRatificationRequest;
import com.phonepe.mustang.request.IndexSnapshotRequest;
import com.phonepe.mustang.response.MustangResponse;
import com.phonepe.mustang.service.DebugService;
import com.phonepe.mustang.debug.DebugResult;
import com.phonepe.mustang.ratify.RatificationResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Mustang APIs", authorizations = { @Authorization("O-Bearer") })
@Path("/mustang/v1/")
public class MustangDebugResource {
    private DebugService service;

    public MustangDebugResource(final DebugService service) {
        this.service = service;
    }

    @POST
    @Path("/debug")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Debug APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
    @RolesAllowed(MustangBundle.MUSTANG_PERMISSION)
    public MustangResponse<DebugResult> debug(@Valid final DebugRequest debugRequest) {
        return MustangResponse.<DebugResult>builder()
                .success(true)
                .data(service.debug(debugRequest))
                .build();
    }

    @POST
    @Path("/index/export")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Debug APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
    @RolesAllowed(MustangBundle.MUSTANG_PERMISSION)
    public MustangResponse<String> exportIndex(@Valid final IndexExportRequest request) {
        return MustangResponse.<String>builder()
                .success(true)
                .data(service.exportIndex(request))
                .build();
    }

    @POST
    @Path("/index/snapshot")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Debug APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
    @RolesAllowed(MustangBundle.MUSTANG_PERMISSION)
    public MustangResponse<String> snapshot(@Valid final IndexSnapshotRequest request) {
        return MustangResponse.<String>builder()
                .success(true)
                .data(service.snapshot(request))
                .build();
    }

    @POST
    @Path("/index/ratify")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Debug APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
    @RolesAllowed(MustangBundle.MUSTANG_PERMISSION)
    public MustangResponse<RatificationResult> ratify(@Valid final IndexRatificationRequest request) {
        return MustangResponse.<RatificationResult>builder()
                .success(true)
                .data(service.ratify(request))
                .build();
    }

}
