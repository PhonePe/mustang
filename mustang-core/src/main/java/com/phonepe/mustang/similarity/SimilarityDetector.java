/*
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phonepe.mustang.similarity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.curiousoddman.rgxgen.RgxGen;
import com.google.common.collect.Sets;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.CriteriaVisitor;
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.criteria.impl.UNFCriteria;
import com.phonepe.mustang.detail.DetailVisitor;
import com.phonepe.mustang.detail.impl.EqualSetDetail;
import com.phonepe.mustang.detail.impl.EqualityDetail;
import com.phonepe.mustang.detail.impl.ExistenceDetail;
import com.phonepe.mustang.detail.impl.NonExistenceDetail;
import com.phonepe.mustang.detail.impl.RangeDetail;
import com.phonepe.mustang.detail.impl.RegexDetail;
import com.phonepe.mustang.detail.impl.SubSetDetail;
import com.phonepe.mustang.detail.impl.SuperSetDetail;
import com.phonepe.mustang.detail.impl.VersioningDetail;
import com.phonepe.mustang.index.IndexGroup;
import com.phonepe.mustang.json.JsonUtils;
import com.phonepe.mustang.predicate.PredicateVisitor;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.mustang.search.Query;
import com.phonepe.mustang.search.QueryBuilder;
import com.phonepe.mustang.search.handler.CriteriaSearchHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityDetector {

    private IndexGroup indexGroup;
    private Criteria criteria;
    private ObjectMapper mapper;

    public SimilarityStats checkSimilarity() {
        return criteria.accept(new CriteriaVisitorImpl());
    }

    private final class CriteriaVisitorImpl implements CriteriaVisitor<SimilarityStats> {

        @Override
        public SimilarityStats visit(DNFCriteria dnf) {

            final List<JsonNode> allContexts = dnf.getConjunctions()
                    .stream()
                    .map(cnj -> {
                        final Map<String, Set<Pair<String, Object>>> buckets = cnj.getPredicates()
                                .stream()
                                .map(predicate -> predicate.accept(new PredicateVisitorImpl()))
                                .flatMap(Collection::stream)
                                .collect(Collectors.groupingBy(Pair::getKey,
                                        Collectors.mapping(x -> x, Collectors.toSet())));

                        final Set<List<Pair<String, Object>>> cartesianProduct = Sets.cartesianProduct(buckets.values()
                                .stream()
                                .collect(Collectors.toList()));

                        return cartesianProduct.stream()
                                .map(list -> {
                                    final Map<String, Object> assignment = list.stream()
                                            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
                                    return JsonUtils.getJsonNodeFromAssignment(mapper, assignment);
                                })
                                .collect(Collectors.toList());
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            return extractSimilarities(allContexts);
        }

        @Override
        public SimilarityStats visit(CNFCriteria cnf) {
            final List<JsonNode> allContexts = cnf.getDisjunctions()
                    .stream()
                    .map(dnj -> {
                        final String uuid = UUID.randomUUID()
                                .toString();
                        final Set<Pair<String, Object>> sets = dnj.getPredicates()
                                .stream()
                                .map(predicate -> predicate.accept(new PredicateVisitorImpl()))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toSet());

                        final Map<String, Set<Pair<String, Object>>> buckets = Map.of(uuid, sets);

                        final Set<List<Pair<String, Object>>> cartesianProduct = Sets.cartesianProduct(buckets.values()
                                .stream()
                                .collect(Collectors.toList()));

                        return cartesianProduct.stream()
                                .map(list -> {
                                    final Map<String, Object> assignment = list.stream()
                                            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
                                    return JsonUtils.getJsonNodeFromAssignment(mapper, assignment);
                                })
                                .collect(Collectors.toList());
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            return extractSimilarities(allContexts);
        }

        @Override
        public SimilarityStats visit(UNFCriteria unf) {
            throw new UnsupportedOperationException("Similarity detection is not supported");
        }

        private Set<String> getSearchResults(final Query query) {
            return CriteriaSearchHandler.builder()
                    .indexGroup(indexGroup)
                    .query(query)
                    .build()
                    .handle()
                    .keySet();
        }

        private SimilarityStats extractSimilarities(final List<JsonNode> allContexts) {
            final List<Similarity> similarities = allContexts.stream()
                    .map(jsonNode -> {
                        final Query query = QueryBuilder.buildQuery(RequestContext.builder()
                                .node(jsonNode)
                                .build());
                        final Set<String> searchResults = getSearchResults(query);

                        if (!searchResults.isEmpty()) {
                            return Optional.of(Similarity.builder()
                                    .context(jsonNode)
                                    .similarCriterias(searchResults)
                                    .build());
                        }
                        return Optional.empty();
                    })
                    .filter(Optional::isPresent)
                    .map(map -> (Similarity) map.get())
                    .collect(Collectors.toList());

            return SimilarityStats.builder()
                    .overallScenarios(allContexts.size())
                    .overlappingScenarios(similarities.size())
                    .similarityScore((similarities.size() / allContexts.size()) * 100)
                    .similarities(similarities)
                    .build();
        }
    }

    private final class PredicateVisitorImpl implements PredicateVisitor<List<Pair<String, Object>>> {

        @Override
        public List<Pair<String, Object>> visit(IncludedPredicate predicate) {
            final String lhs = predicate.getLhs();
            final Set<Object> values = predicate.getDetail()
                    .accept(new DetailVisitorImpl());

            return values.stream()
                    .map(value -> Pair.of(lhs, value))
                    .collect(Collectors.toList());
        }

        @Override
        public List<Pair<String, Object>> visit(ExcludedPredicate predicate) {
            return Collections.emptyList();
        }

    }

    private final class DetailVisitorImpl implements DetailVisitor<Set<Object>> {

        @Override
        public Set<Object> visit(ExistenceDetail detail) {
            return Collections.singleton(new Object());
        }

        @Override
        public Set<Object> visit(NonExistenceDetail detail) {
            return Collections.emptySet();
        }

        @Override
        public Set<Object> visit(EqualityDetail detail) {
            return detail.getValues();
        }

        @Override
        public Set<Object> visit(SubSetDetail detail) {
            return detail.getValues();
        }

        @Override
        public Set<Object> visit(EqualSetDetail detail) {
            return detail.getValues();
        }

        @Override
        public Set<Object> visit(SuperSetDetail detail) {
            return detail.getValues();
        }

        @Override
        public Set<Object> visit(RegexDetail detail) {
            final RgxGen rgxGen = RgxGen.parse(detail.getRegex());
            return IntStream.rangeClosed(0, 5)
                    .boxed()
                    .map(x -> rgxGen.generate())
                    .collect(Collectors.toSet());
        }

        @Override
        public Set<Object> visit(RangeDetail detail) {

            final Object lowerValue = detail.isIncludeLowerBound() ? detail.getLowerBound()
                    : Math.nextUp(detail.getLowerBound()
                            .floatValue());
            final Object upperValue = detail.isIncludeUpperBound() ? detail.getUpperBound()
                    : Math.nextDown(detail.getUpperBound()
                            .floatValue());
            final float avgValue = (detail.getLowerBound()
                    .floatValue()
                    + detail.getUpperBound()
                            .floatValue())
                    / 2;
            return Set.of(lowerValue, upperValue, avgValue);
        }

        @Override
        public Set<Object> visit(VersioningDetail detail) {
            return detail.isExcludeBase() ? Collections.emptySet() : Set.of(detail.getBaseVersion());
        }
    }

}
