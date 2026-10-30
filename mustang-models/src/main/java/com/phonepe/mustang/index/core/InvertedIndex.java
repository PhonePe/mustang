/**
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
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
package com.phonepe.mustang.index.core;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.phonepe.mustang.criteria.CriteriaForm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@AllArgsConstructor
public abstract class InvertedIndex<T> {
    @NotNull
    private final CriteriaForm form;
    private final AtomicInteger idCounter = new AtomicInteger(0);
    private final Map<Integer, Map<Key, TreeMap<Integer, T>>> table = Maps.newConcurrentMap();
    private final Map<String, PriorityQueue<Integer>> idCache = Maps.newConcurrentMap();
    private final Map<Integer, TreeSet<Integer>> linkages = Maps.newConcurrentMap();
    /**
     * Derived, per conjunction/disjunction size lookups over {@code table}. Never serialised; rebuilt lazily
     * whenever the posting-list map for a size is replaced by the indexers.
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final transient Map<Integer, EqualityKeyLookup> equalityKeyLookups = Maps.newConcurrentMap();

    /**
     * Returns the equality key lookup for the given size, rebuilding it if the posting-list map has been replaced
     * since it was last built.
     *
     * @param size         conjunction (DNF) or disjunction (CNF) size, i.e. the key of {@code table}
     * @param postingLists the current posting-list map for that size
     */
    public EqualityKeyLookup getEqualityKeyLookup(final int size, final Map<Key, ?> postingLists) {
        final EqualityKeyLookup existing = equalityKeyLookups.get(size);
        if (Objects.nonNull(existing) && existing.getSource() == postingLists) {
            return existing;
        }
        final EqualityKeyLookup rebuilt = EqualityKeyLookup.of(postingLists);
        equalityKeyLookups.put(size, rebuilt);
        return rebuilt;
    }

    public Integer getInternalIdFromCache(final String externalId) {
        return idCache.computeIfAbsent(externalId, x -> {
            final PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(Comparator.reverseOrder());
            priorityQueue.add(idCounter.incrementAndGet());
            return priorityQueue;
        })
                .peek();
    }

    public Integer getNextInternalIdFromCache(final String externalId) {
        idCache.computeIfAbsent(externalId, x -> new PriorityQueue<>(Comparator.reverseOrder()))
                .add(idCounter.incrementAndGet());
        return idCache.get(externalId)
                .peek();
    }

}
