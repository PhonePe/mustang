/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.index.core;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.CriteriaForm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class InvertedIndex<T> {
    @NotNull
    private final CriteriaForm form;
    private final Map<Integer, Map<Key, TreeSet<T>>> table = Maps.newConcurrentMap();
    private final AtomicInteger idCounter = new AtomicInteger(0);
    private final Map<String, Integer> idCache = Maps.newConcurrentMap();

    public Integer getInternalIdFromCache(final String externalId) {
        return idCache.computeIfAbsent(externalId, x -> idCounter.incrementAndGet());
    }

}
