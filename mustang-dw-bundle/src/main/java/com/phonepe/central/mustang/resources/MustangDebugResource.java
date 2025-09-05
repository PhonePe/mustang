package com.phonepe.central.mustang.resources;

import javax.inject.Singleton;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.central.mustang.response.MustangResponse;
import com.phonepe.growth.mustang.MustangEngine;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Mustang APIs", authorizations = { @Authorization("O-Bearer") })
@Path("/v1/mustang")
public class MustangDebugResource {
    private final MustangEngine mustangEngine;
    private final ObjectMapper mapper;

    public MustangDebugResource(final MustangEngine mustangEngine, final ObjectMapper mapper) {
        this.mustangEngine = mustangEngine;
        this.mapper = mapper;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Mustang Debug APIs", authorizations = { @Authorization("O-Bearer") })
    @ResponseMetered
    @Timed
//    @RolesAllowed(MUSTANG_PERMISSION)
    public MustangResponse<Boolean> callback(@Valid final JsonNode callback) {

        return MustangResponse.<Boolean>builder()
                .success(true)
                .data(true)
                .build();
    }
}
