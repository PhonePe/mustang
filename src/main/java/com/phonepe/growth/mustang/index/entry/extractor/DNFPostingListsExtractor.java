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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
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
public class DNFPostingListsExtractor implements PredicateVisitor<Map<Key, TreeSet<ConjunctionPostingEntry>>> {
    private final Integer iId;
    private final String eId;
    private final Map<Key, AtomicInteger> dnfKeyFrequency;

    @Override
    public Map<Key, TreeSet<ConjunctionPostingEntry>> visit(IncludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    @Override
    public Map<Key, TreeSet<ConjunctionPostingEntry>> visit(ExcludedPredicate predicate) {
        return extractPostingLists(predicate.getType(), predicate.getLhs(), predicate.getValues());
    }

    private Map<Key, TreeSet<ConjunctionPostingEntry>> extractPostingLists(PredicateType pType,
            String lhs,
            Set<?> values) {
        return values.stream()
                .map(value -> {
                    final Key key = Key.builder()
                            .name(lhs)
                            .value(value)
                            .build();
                    dnfKeyFrequency.computeIfAbsent(key, x -> new AtomicInteger(0))
                            .getAndIncrement();
                    return key;
                })
                .map(key -> Pair.of(key,
                        ConjunctionPostingEntry.builder()
                                .iId(iId)
                                .eId(eId)
                                .type(pType)
                                .score(0)
                                .build()))
                .collect(Collectors.groupingBy(Pair::getKey,
                        LinkedHashMap::new,
                        Collectors.mapping(Pair::getValue, Collectors.toCollection(TreeSet::new))));
    }
}
