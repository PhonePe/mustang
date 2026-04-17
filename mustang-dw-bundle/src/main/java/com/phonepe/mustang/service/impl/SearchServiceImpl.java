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

import java.util.Set;

import com.phonepe.mustang.request.SearchRequest;
import com.phonepe.mustang.service.SearchService;
import com.phonepe.mustang.MustangEngine;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchServiceImpl implements SearchService {
    private final MustangEngine engine;

    public SearchServiceImpl(final MustangEngine enigne) {
        this.engine = enigne;
    }

    @Override
    public Set<String> search(final SearchRequest request) {
        if (request.isScore()) {
            return engine.search(request.getIndexName(), request.getRequestContext(), request.isScore());
        }
        return engine.search(request.getIndexName(), request.getRequestContext());
    }

}
