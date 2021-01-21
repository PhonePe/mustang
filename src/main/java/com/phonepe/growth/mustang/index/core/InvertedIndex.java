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
