package com.phonepe.growth.mustang.ratify;

import java.io.IOException;
import java.util.Collection;
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
import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.exception.MustangException;
import com.phonepe.growth.mustang.index.IndexingFacade;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.scan.Scanner;
import com.phonepe.growth.mustang.search.Query;
import com.phonepe.growth.mustang.search.handler.CriteriaSearchHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Ratifier {
    private ObjectMapper mapper;
    private IndexingFacade indexingFacade;
    private String indexName;

    public RatificationResult ratify() {
        final long startTime = System.currentTimeMillis();

        final IndexGroup index = indexingFacade.getIndexGroup(indexName);

        final Map<String, Criteria> allCriterias = index.getAllCriterias();

        final Set<Key> allKeys = getAllKeys(index);
        final Pair<Map<Integer, Key>, Map<Key, Integer>> keyIndex = buildIndex(allKeys);
        final Map<String, Set<Integer>> keyGroups = groupKeys(allKeys, keyIndex);

        final Set<List<Integer>> cartesianProductCombinations = Sets.cartesianProduct(keyGroups.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));

        final Set<Set<Integer>> subSetCombinations = IntStream.range(1,
                keyGroups.values()
                        .size())
                .boxed()
                .map(i -> Sets.combinations(keyIndex.getKey()
                        .keySet(), i))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        final Set<RatificationDetail> primaryDetails = cartesianProductCombinations.stream()
                .map(combination -> validate(allCriterias, allKeys, keyIndex, combination))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Set<RatificationDetail> secondaryDetails = subSetCombinations.stream()
                .map(combination -> validate(allCriterias, allKeys, keyIndex, combination))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return RatificationResult.builder()
                .ratified(secondaryDetails.isEmpty() && primaryDetails.isEmpty())
                .combinations(subSetCombinations.size() + cartesianProductCombinations.size())
                .anamolyDetails(Sets.union(secondaryDetails, primaryDetails))
                .timeTaken(System.currentTimeMillis() - startTime)
                .indexName(indexName)
                .build();
    }

    private RatificationDetail validate(final Map<String, Criteria> allCriterias,
            final Set<Key> allKeys,
            final Pair<Map<Integer, Key>, Map<Key, Integer>> keyIndex,
            final Collection<Integer> combination) {
        final Map<String, Object> assigment = combination.stream()
                .map(i -> keyIndex.getLeft()
                        .get(i))
                .collect(Collectors.toMap(Key::getName, Key::getValue, (x1, x2) -> x2));
        final RequestContext context = RequestContext.builder()
                .node(getJsonNodeFromAssignment(assigment))
                .build();
        final Query query = Query.builder()
                .assigment(assigment)
                .context(context)
                .build();
        final Set<String> searchResults = getSearchResults(query);
        final Set<String> scanResults = getScanResults(allCriterias, query);
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
                .map(x -> x.keySet())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        final Set<Key> cnfKeys = index.getCnfInvertedIndex()
                .getTable()
                .values()
                .stream()
                .map(x -> x.keySet())
                .flatMap(Set::stream)
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
        final String unflatten = JsonUnflattener.unflatten(deNormalisedAssignment);
        try {
            return mapper.readValue(unflatten, JsonNode.class);
        } catch (IOException e) {
            throw MustangException.propagate(e);
        }
    }

    private Set<String> getSearchResults(final Query query) {
        return CriteriaSearchHandler.builder()
                .indexGroup(indexingFacade.getIndexGroup(indexName))
                .query(query)
                .build()
                .handle()
                .keySet();
    }

    private Set<String> getScanResults(final Map<String, Criteria> allCriterias, final Query query) {
        return Scanner.builder()
                .criterias(allCriterias.entrySet()
                        .stream()
                        .map(x -> x.getValue())
                        .collect(Collectors.toList()))
                .context(query.getContext())
                .build()
                .scan()
                .stream()
                .map(Criteria::getId)
                .collect(Collectors.toSet());
    }

}
