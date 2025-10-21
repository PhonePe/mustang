package com.phonepe.central.mustang.resources;

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
import com.phonepe.central.mustang.request.SearchRequest;
import com.phonepe.central.mustang.response.MustangResponse;
import com.phonepe.central.mustang.service.SearchService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Mustang APIs", authorizations = { @Authorization("O-Bearer") })
@Path("/v1/mustang/search")
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
