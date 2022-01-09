/**
 * Copyright (c) 2022 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.index.group;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.validator.constraints.NotBlank;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.core.impl.DNFInvertedIndex;
import com.phonepe.growth.mustang.ratify.RatificationResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(of = { "name" })
@NoArgsConstructor
@AllArgsConstructor
public class IndexGroup {
    @NotBlank
    private String name;
    private final DNFInvertedIndex<ConjunctionPostingEntry> dnfInvertedIndex = DNFInvertedIndex
            .<ConjunctionPostingEntry>builder()
            .build();
    private final CNFInvertedIndex<DisjunctionPostingEntry> cnfInvertedIndex = CNFInvertedIndex
            .<DisjunctionPostingEntry>builder()
            .build();
    private final Map<String, Criteria> allCriterias = Maps.newConcurrentMap();
    private final Map<String, Criteria> tautologicalCriterias = Maps.newConcurrentMap();
    private final Map<Key, AtomicInteger> dnfKeyFrequency = Maps.newConcurrentMap();
    private final Map<Key, AtomicInteger> cnfKeyFrequency = Maps.newConcurrentMap();
    private RatificationResult ratificationResult;
}
