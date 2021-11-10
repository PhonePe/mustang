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
package com.phonepe.growth.mustang.index.entry.extractor;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import com.phonepe.growth.mustang.detail.Detail;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.predicate.PredicateVisitor;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CNFPostingListsExtractor implements PredicateVisitor<Map<Key, TreeMap<Integer, DisjunctionPostingEntry>>> {

    private static final Comparator<Key> KEY_ORDER_COMPARATOR = (k1, k2) -> Integer.valueOf(k1.getOrder())
            .compareTo(k2.getOrder());
    private final Integer iId;
    private final String eId;
    private final int order;
    private final Map<Key, TreeMap<Integer, DisjunctionPostingEntry>> postingLists;
    private final Map<Key, AtomicInteger> cnfKeyFrequency;

    @Override
    public Map<Key, TreeMap<Integer, DisjunctionPostingEntry>> visit(IncludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getDetail());
    }

    @Override
    public Map<Key, TreeMap<Integer, DisjunctionPostingEntry>> visit(ExcludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getDetail());
    }

    private Map<Key, TreeMap<Integer, DisjunctionPostingEntry>> extractPostingLists(final PredicateType pType,
            final String lhs,
            final Detail detail) {

        final DisjunctionPostingEntry postingEntry = DisjunctionPostingEntry.builder()
                .iId(iId)
                .eId(eId)
                .type(pType)
                .order(order)
                .score(0)
                .build();

        final Set<Object> values = detail.accept(new DetailValueExtractor());

        return values.stream()
                .map(value -> {
                    final Set<Key> keys = postingLists.keySet()
                            .stream()
                            .filter(key -> key.getName()
                                    .equals(lhs)
                                    && key.getCaveat()
                                            .equals(detail.getCaveat())
                                    && key.getValue()
                                            .equals(value))
                            .sorted(KEY_ORDER_COMPARATOR)
                            .collect(Collectors.toSet());
                    if (keys.isEmpty()) {
                        return Key.builder()
                                .name(lhs)
                                .caveat(detail.getCaveat())
                                .value(value)
                                .order(0)
                                .compiledPath(JsonPath.compile(lhs))
                                .build();
                    }
                    final AtomicInteger counter = new AtomicInteger(0);
                    return keys.stream()
                            .sequential()
                            .filter(key -> {
                                counter.incrementAndGet();
                                return !Sets.newTreeSet(postingLists.get(key)
                                        .values())
                                        .contains(postingEntry);
                            })
                            .findFirst()
                            .orElse(Key.builder()
                                    .name(lhs)
                                    .caveat(detail.getCaveat())
                                    .value(value)
                                    .order(counter.get())
                                    .compiledPath(JsonPath.compile(lhs))
                                    .build());
                })
                .map(key -> {
                    final Key baseKey = Key.builder()
                            .name(key.getName())
                            .caveat(detail.getCaveat())
                            .value(key.getValue())
                            .compiledPath(JsonPath.compile(key.getName()))
                            .build();
                    cnfKeyFrequency.computeIfAbsent(baseKey, x -> new AtomicInteger(0))
                            .getAndIncrement();
                    return key;
                })
                .map(key -> Pair.of(key, postingEntry))
                .collect(Collectors.groupingBy(Pair::getLeft,
                        LinkedHashMap::new,
                        Collectors.mapping(Pair::getRight,
                                Collectors.toMap(DisjunctionPostingEntry::getIId, x -> x, (o, n) -> n, TreeMap::new))));
    }
}
