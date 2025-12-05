package com.phonepe.central.mustang.service;

import com.phonepe.central.mustang.request.DebugRequest;
import com.phonepe.central.mustang.request.IndexExportRequest;
import com.phonepe.central.mustang.request.IndexRatificationRequest;
import com.phonepe.central.mustang.request.IndexSnapshotRequest;
import com.phonepe.growth.mustang.debug.DebugResult;
import com.phonepe.growth.mustang.ratify.RatificationResult;

public interface DebugService {

    DebugResult debug(final DebugRequest request);

    String exportIndex(final IndexExportRequest request);

    String snapshot(final IndexSnapshotRequest request);

    RatificationResult ratify(final IndexRatificationRequest request);

}
