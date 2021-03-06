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
package com.phonepe.mustang.index.core.impl;

import java.util.Map;

import com.google.common.collect.Maps;
import com.phonepe.mustang.criteria.CriteriaForm;
import com.phonepe.mustang.index.core.InvertedIndex;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CNFInvertedIndex<T> extends InvertedIndex<T> {
    private final Map<Integer, Integer[]> disjunctionCounters = Maps.newConcurrentMap();

    @Builder
    public CNFInvertedIndex() {
        super(CriteriaForm.CNF);
    }

}
