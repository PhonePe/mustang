package com.phonepe.central.mustang.resources;

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
import com.phonepe.central.mustang.MustangBundle;
import com.phonepe.central.mustang.request.ScanCriteriaRequest;
import com.phonepe.central.mustang.request.ScanIndexRequest;
import com.phonepe.central.mustang.response.MustangResponse;
import com.phonepe.central.mustang.service.ScanService;
import com.phonepe.growth.mustang.criteria.Criteria;

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
