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

import java.util.List;
import java.util.Set;

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
import com.phonepe.mustang.request.ScanCriteriaRequest;
import com.phonepe.mustang.request.ScanIndexRequest;
import com.phonepe.mustang.response.MustangResponse;
import com.phonepe.mustang.service.ScanService;
import com.phonepe.mustang.criteria.Criteria;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Mustang APIs", authorizations = { @Authorization("O-Bearer") })
@Path("/mustang/v1/scan")
public class MustangScanResource {

    private final ScanService service;

    public MustangScanResource(final ScanService service) {
        this.service = service;
    }

    @POST
    @Path("/index")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Scan APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
    @RolesAllowed(MustangBundle.MUSTANG_PERMISSION)
    public MustangResponse<Set<String>> scan(@Valid ScanIndexRequest request) {
        return MustangResponse.<Set<String>>builder()
                .success(true)
                .data(service.scan(request))
                .build();
    }

    @POST
    @Path("/criteria")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Scan APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
    @RolesAllowed(MustangBundle.MUSTANG_PERMISSION)
    public MustangResponse<List<Criteria>> scan(@Valid ScanCriteriaRequest request) {
        return MustangResponse.<List<Criteria>>builder()
                .success(true)
                .data(service.scan(request))
                .build();
    }
}
