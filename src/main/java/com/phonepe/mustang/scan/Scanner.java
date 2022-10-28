/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.scan;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.index.group.IndexGroup;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Scanner {
    private IndexGroup indexGroup;
    @Valid
    @NotNull
    private RequestContext context;

    public Set<String> scan() {
        final List<Criteria> scanResults = indexGroup.getAllCriterias()
                .values()
                .stream()
                .filter(criteria -> criteria.evaluate(context))
                .collect(Collectors.toList());
        return Stream.of(scanResults,
                indexGroup.getTautologicalCriterias()
                        .values())
                .flatMap(Collection::stream)
                .map(Criteria::getId)
                .collect(Collectors.toSet());
    }

}
