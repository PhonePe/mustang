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
import com.phonepe.mustang.request.SearchRequest;
import com.phonepe.mustang.response.MustangResponse;
import com.phonepe.mustang.service.SearchService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Mustang APIs", authorizations = { @Authorization("O-Bearer") })
@Path("/mustang/v1/search")
public class MustangSearchResource {

    private final SearchService service;

    public MustangSearchResource(final SearchService serviceImpl) {
        this.service = serviceImpl;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Search APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
    @RolesAllowed(MustangBundle.MUSTANG_PERMISSION)
    public MustangResponse<Set<String>> search(@Valid final SearchRequest request) {
        return MustangResponse.<Set<String>>builder()
                .success(true)
                .data(service.search(request))
                .build();
    }
}
