package com.phonepe.central.mustang.service.impl;

import com.phonepe.central.mustang.request.DebugRequest;
import com.phonepe.central.mustang.request.IndexExportRequest;
import com.phonepe.central.mustang.request.IndexRatificationRequest;
import com.phonepe.central.mustang.request.IndexSnapshotRequest;
import com.phonepe.central.mustang.service.DebugService;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.debug.DebugResult;
import com.phonepe.growth.mustang.ratify.RatificationResult;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DebugServiceImpl implements DebugService {

    private final MustangEngine engine;

    public DebugServiceImpl(final MustangEngine engine) {
        this.engine = engine;
    }

    @Override
    public DebugResult debug(final DebugRequest debugRequest) {
        return engine.debug(debugRequest.getCriteria(), debugRequest.getRequestContext());
    }

    @Override
    public String exportIndex(final IndexExportRequest request) {
        return engine.exportIndexGroup(request.getIndexName());
    }

    @Override
    public String snapshot(final IndexSnapshotRequest request) {
        return engine.snapshot(request.getIndexName());
    }

    @Override
    public RatificationResult ratify(final IndexRatificationRequest request) {
        engine.ratify(request.getIndexName(), request.isFullFledged());
        return engine.getRatificationResult(request.getIndexName());
    }

}
