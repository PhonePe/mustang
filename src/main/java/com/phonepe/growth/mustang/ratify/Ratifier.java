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
package com.phonepe.growth.mustang.ratify;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.index.builder.CNFIndexer;
import com.phonepe.growth.mustang.index.builder.DNFIndexer;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.scan.Scanner;
import com.phonepe.growth.mustang.search.Query;
import com.phonepe.growth.mustang.search.QueryBuilder;
import com.phonepe.growth.mustang.search.handler.CriteriaSearchHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Ratifier {
    private final ObjectMapper mapper;
    private final IndexGroup indexGroup;
    private final boolean fullFledged;
    private final long requestedAt;

    public RatificationResult ratify() {
        final long startTime = System.currentTimeMillis();

        final Set<Key> allKeys = getAllKeys(indexGroup);
        final Pair<Map<Integer, Key>, Map<Key, Integer>> keyIndex = buildIndex(allKeys);
        final Map<String, Set<Integer>> keyGroups = groupKeys(allKeys, keyIndex);

        final Set<List<Integer>> cartesianProductCombinations = Sets.cartesianProduct(keyGroups.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));

        final Set<Set<Integer>> subSetCombinations = fullFledged ? IntStream.range(1,
                keyGroups.values()
                        .size())
                .boxed()
                .map(i -> Sets.combinations(keyIndex.getKey()
                        .keySet(), i))
                .flatMap(Set::stream)
                .collect(Collectors.toSet()) : Collections.emptySet();

        final Set<RatificationDetail> primaryDetails = cartesianProductCombinations.stream()
                .map(combination -> validate(keyIndex, combination))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Set<RatificationDetail> secondaryDetails = subSetCombinations.stream()
                .map(combination -> validate(keyIndex, combination))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return RatificationResult.builder()
                .status(secondaryDetails.isEmpty() && primaryDetails.isEmpty())
                .combinations(RatificationResult.Combinations.builder()
                        .totalCount(cartesianProductCombinations.size() + subSetCombinations.size())
                        .cpCount(cartesianProductCombinations.size())
                        .ssCount(subSetCombinations.size())
                        .build())
                .anamolyDetails(Sets.union(primaryDetails, secondaryDetails))
                .timeTakenMs(System.currentTimeMillis() - startTime)
                .requestedAt(requestedAt)
                .fullFledgedRun(fullFledged)
                .ratifiedAt(System.currentTimeMillis())
                .build();
    }

    private RatificationDetail validate(final Pair<Map<Integer, Key>, Map<Key, Integer>> keyIndex,
            final Collection<Integer> combination) {
        final Map<String, Object> assigment = combination.stream()
                .map(i -> keyIndex.getLeft()
                        .get(i))
                .collect(Collectors.toMap(Key::getName, Key::getValue, (o, n) -> n));
        final RequestContext context = RequestContext.builder()
                .node(getJsonNodeFromAssignment(assigment))
                .build();
        final Query query = QueryBuilder.buildQuery(context);
        final Set<String> searchResults = getSearchResults(query);
        final Set<String> scanResults = getScanResults(query);
        final boolean result = Sets.symmetricDifference(searchResults, scanResults)
                .isEmpty();
        if (!result) {
            return RatificationDetail.builder()
                    .expected(scanResults)
                    .actual(searchResults)
                    .context(context)
                    .build();
        }
        return null;
    }

    private Set<Key> getAllKeys(final IndexGroup index) {
        final Set<Key> dnfKeys = index.getDnfInvertedIndex()
                .getTable()
                .values()
                .stream()
                .map(Map::keySet)
                .flatMap(Set::stream)
                .filter(key -> !key.getName()
                        .equals(DNFIndexer.ZERO_SIZE_CONJUNCTION_ENTRY_KEYNAME))
                .collect(Collectors.toSet());
        final Set<Key> cnfKeys = index.getCnfInvertedIndex()
                .getTable()
                .values()
                .stream()
                .map(Map::keySet)
                .flatMap(Set::stream)
                .filter(key -> !key.getName()
                        .equals(CNFIndexer.ZERO_SIZE_DISJUNCTION_ENTRY_KEYNAME))
                .collect(Collectors.toSet());
        return Stream.concat(dnfKeys.stream(), cnfKeys.stream())
                .distinct()
                .collect(Collectors.toSet());

    }

    private Pair<Map<Integer, Key>, Map<Key, Integer>> buildIndex(final Set<Key> allKeys) {
        final AtomicInteger counter = new AtomicInteger();
        final Map<Integer, Key> forwardIndex = allKeys.stream()
                .map(key -> Pair.of(counter.incrementAndGet(), key))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        final Map<Key, Integer> reverseIndex = forwardIndex.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        return Pair.of(forwardIndex, reverseIndex);

    }

    private Map<String, Set<Integer>> groupKeys(final Set<Key> allKeys,
            final Pair<Map<Integer, Key>, Map<Key, Integer>> keyIndex) {
        return allKeys.stream()
                .collect(Collectors.groupingBy(Key::getName,
                        Collectors.mapping(x -> keyIndex.getValue()
                                .get(x), Collectors.toSet())));
    }

    private JsonNode getJsonNodeFromAssignment(final Map<String, Object> assignment) {
        final Map<String, Object> deNormalisedAssignment = assignment.entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey()
                        .substring(2), entry.getValue()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        return mapper.valueToTree(deNormalisedAssignment);
    }

    private Set<String> getSearchResults(final Query query) {
        return CriteriaSearchHandler.builder()
                .indexGroup(indexGroup)
                .query(query)
                .build()
                .handle()
                .keySet();
    }

    private Set<String> getScanResults(final Query query) {
        return Scanner.builder()
                .indexGroup(indexGroup)
                .context(query.getRequestContext())
                .build()
                .scan();
    }

}
