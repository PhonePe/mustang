package com.phonepe.central.mustang.similarity;

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
import com.phonepe.central.mustang.common.RequestContext;
import com.phonepe.central.mustang.criteria.Criteria;
import com.phonepe.central.mustang.criteria.CriteriaVisitor;
import com.phonepe.central.mustang.criteria.impl.CNFCriteria;
import com.phonepe.central.mustang.criteria.impl.DNFCriteria;
import com.phonepe.central.mustang.criteria.impl.UNFCriteria;
import com.phonepe.central.mustang.detail.DetailVisitor;
import com.phonepe.central.mustang.detail.impl.EqualSetDetail;
import com.phonepe.central.mustang.detail.impl.EqualityDetail;
import com.phonepe.central.mustang.detail.impl.ExistenceDetail;
import com.phonepe.central.mustang.detail.impl.NonExistenceDetail;
import com.phonepe.central.mustang.detail.impl.RangeDetail;
import com.phonepe.central.mustang.detail.impl.RegexDetail;
import com.phonepe.central.mustang.detail.impl.SubSetDetail;
import com.phonepe.central.mustang.detail.impl.SuperSetDetail;
import com.phonepe.central.mustang.detail.impl.VersioningDetail;
import com.phonepe.central.mustang.index.IndexGroup;
import com.phonepe.central.mustang.json.JsonUtils;
import com.phonepe.central.mustang.predicate.PredicateVisitor;
import com.phonepe.central.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.central.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.central.mustang.search.Query;
import com.phonepe.central.mustang.search.QueryBuilder;
import com.phonepe.central.mustang.search.handler.CriteriaSearchHandler;

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
