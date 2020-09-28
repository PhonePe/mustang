package com.phonepe.growth.mustang.index.core;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Maps;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.core.impl.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.core.impl.DNFInvertedIndex;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "form")
@JsonSubTypes({ @JsonSubTypes.Type(name = CriteriaForm.DNF_TEXT, value = DNFInvertedIndex.class),
        @JsonSubTypes.Type(name = CriteriaForm.CNF_TEXT, value = CNFInvertedIndex.class) })
public abstract class InvertedIndex<T> {
    @NotNull
    private CriteriaForm form;
    private final Map<Integer, Map<Key, TreeSet<T>>> table = Maps.newConcurrentMap();
    private final AtomicInteger idCounter = new AtomicInteger(0);
    private final Map<String, Integer> idCache = Maps.newConcurrentMap();

    public Integer getInternalIdFromCache(String externalId) {
        return idCache.computeIfAbsent(externalId, x -> idCounter.incrementAndGet());
    }

    public abstract <U> U accept(InvertedIndexVisitor<T, U> visitor);

}
