/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.index.entry.extractor;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import com.phonepe.mustang.detail.Detail;
import com.phonepe.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.mustang.index.core.Key;
import com.phonepe.mustang.predicate.PredicateType;
import com.phonepe.mustang.predicate.PredicateVisitor;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

@Data
@Builder
@AllArgsConstructor
public class CNFPostingListsExtractor implements PredicateVisitor<Map<Key, TreeMap<Integer, DisjunctionPostingEntry>>> {

    private static final Comparator<Key> KEY_ORDER_COMPARATOR = (k1, k2) -> Integer.compare(k1.getOrder(),
            k2.getOrder());
    private final Integer iId;
    private final String eId;
    private final int order;
    private final Map<Key, TreeMap<Integer, DisjunctionPostingEntry>> postingLists;
    private final Map<Key, AtomicInteger> cnfKeyFrequency;
    private final Map<String, JsonPath> allPaths;

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
                                    .build());
                })
                .map(key -> {
                    final Key baseKey = Key.builder()
                            .name(key.getName())
                            .caveat(detail.getCaveat())
                            .value(key.getValue())
                            .build();
                    cnfKeyFrequency.computeIfAbsent(baseKey, x -> new AtomicInteger(0))
                            .getAndIncrement();
                    allPaths.computeIfAbsent(lhs, x -> JsonPath.compile(lhs));
                    return key;
                })
                .map(key -> Pair.of(key, postingEntry))
                .collect(Collectors.groupingBy(Pair::getLeft,
                        LinkedHashMap::new,
                        Collectors.mapping(Pair::getRight,
                                Collectors.toMap(DisjunctionPostingEntry::getIId, x -> x, (o, n) -> n, TreeMap::new))));
    }
}
