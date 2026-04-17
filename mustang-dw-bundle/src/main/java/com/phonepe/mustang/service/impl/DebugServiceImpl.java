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

package com.phonepe.mustang.service.impl;

import com.phonepe.mustang.request.DebugRequest;
import com.phonepe.mustang.request.IndexExportRequest;
import com.phonepe.mustang.request.IndexRatificationRequest;
import com.phonepe.mustang.request.IndexSnapshotRequest;
import com.phonepe.mustang.service.DebugService;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.debug.DebugResult;
import com.phonepe.mustang.ratify.RatificationResult;

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
