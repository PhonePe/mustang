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

import java.util.List;
import java.util.Set;

import com.phonepe.mustang.request.ScanCriteriaRequest;
import com.phonepe.mustang.request.ScanIndexRequest;
import com.phonepe.mustang.service.ScanService;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.criteria.Criteria;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanServiceImpl implements ScanService {

    private final MustangEngine engine;

    public ScanServiceImpl(final MustangEngine engine) {
        this.engine = engine;
    }

    @Override
    public Set<String> scan(final ScanIndexRequest request) {
        return engine.scan(request.getIndexName(), request.getRequestContext());
    }

    @Override
    public List<Criteria> scan(final ScanCriteriaRequest request) {
        return engine.scan(request.getCriterias(), request.getRequestContext());
    }

}
