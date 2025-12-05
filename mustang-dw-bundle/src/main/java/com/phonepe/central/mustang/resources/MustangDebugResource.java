package com.phonepe.central.mustang.resources;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.phonepe.central.mustang.MustangBundle;
import com.phonepe.central.mustang.request.DebugRequest;
import com.phonepe.central.mustang.request.IndexExportRequest;
import com.phonepe.central.mustang.request.IndexRatificationRequest;
import com.phonepe.central.mustang.request.IndexSnapshotRequest;
import com.phonepe.central.mustang.response.MustangResponse;
import com.phonepe.central.mustang.service.DebugService;
import com.phonepe.growth.mustang.debug.DebugResult;
import com.phonepe.growth.mustang.ratify.RatificationResult;

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
