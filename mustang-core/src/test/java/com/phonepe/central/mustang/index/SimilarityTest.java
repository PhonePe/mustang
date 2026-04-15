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
 */
package com.phonepe.central.mustang.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.central.mustang.MustangEngine;
import com.phonepe.central.mustang.composition.impl.Conjunction;
import com.phonepe.central.mustang.composition.impl.Disjunction;
import com.phonepe.central.mustang.criteria.Criteria;
import com.phonepe.central.mustang.criteria.impl.CNFCriteria;
import com.phonepe.central.mustang.criteria.impl.DNFCriteria;
import com.phonepe.central.mustang.detail.impl.CheckType;
import com.phonepe.central.mustang.detail.impl.EqualityDetail;
import com.phonepe.central.mustang.detail.impl.ExistenceDetail;
import com.phonepe.central.mustang.detail.impl.NonExistenceDetail;
import com.phonepe.central.mustang.detail.impl.RangeDetail;
import com.phonepe.central.mustang.detail.impl.RegexDetail;
import com.phonepe.central.mustang.detail.impl.SubSetDetail;
import com.phonepe.central.mustang.detail.impl.SuperSetDetail;
import com.phonepe.central.mustang.detail.impl.VersioningDetail;
import com.phonepe.central.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.central.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.central.mustang.preoperation.impl.BinaryConversionPreOperation;
import com.phonepe.central.mustang.preoperation.impl.SubtractionPreOperation;
import com.phonepe.central.mustang.similarity.SimilarityStats;

public class SimilarityTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;

    @Before
    public void setUp() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
    }

    @Test
    public void testDNFPositiveMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .preOperation(SubtractionPreOperation.builder()
                                        .rhs(0.0000001)
                                        .build())
                                .values(Sets.newHashSet(0.0000001, 0.0000002, 0.0000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x")
                                .preOperation(BinaryConversionPreOperation.builder()
                                        .build())
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.0000004);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);

        SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), contains("C1"));

        similarityStats = engine.checkSimilarity("test", c2);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), contains("C2"));

    }

    @Test
    public void testCNFPositiveMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", false);

        engine.add("test", c1);
        engine.add("test", c2);

        SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C1"));

        similarityStats = engine.checkSimilarity("test", c2);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C2"));
    }

    @Test
    public void testDNFPositiveRangeCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.*")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("B1", "B2"))
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);

        SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C1"));

        similarityStats = engine.checkSimilarity("test", c2);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C2"));

    }

    @Test
    public void testDNFPositiveVersioningCheck() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.*")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("B1", "B2"))
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.ABOVE)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("5.7.40")
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.x")
                                .detail(VersioningDetail.builder()
                                        .check(CheckType.BELOW)
                                        .baseVersion("1.2")
                                        .build())
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", "5.7.40");
        testQuery.put("x", 1.2); // Not a number and hence shall fail
        testQuery.put("p", true);

        engine.add("test", c1);
        engine.add("test", c2);
        engine.add("test", c3);

        SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C1"));

        similarityStats = engine.checkSimilarity("test", c2);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C2"));

        similarityStats = engine.checkSimilarity("test", c3);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C3"));
    }

    // @Test -- REVISIT
    public void testDNFMultiValueSingleEclipsing() throws JsonMappingException, JsonProcessingException {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicates(List.of(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Set.of("A1"))
                                        .build())
                                .build(),
                                IncludedPredicate.builder()
                                        .lhs("$.c.modes[*].type")
                                        .detail(SuperSetDetail.builder()
                                                .values(Set.of("C1", "C2", "C3"))
                                                .build())
                                        .build()))
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicates(List.of(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Set.of("A2"))
                                        .build())
                                .build(),
                                IncludedPredicate.builder()
                                        .lhs("$.c.modes[*].type")
                                        .detail(SubSetDetail.builder()
                                                .values(Set.of("C1", "C2", "C3"))
                                                .build())
                                        .build()))
                        .build())
                .build();

        engine.add("test", c1);
        engine.add("test", c2);

        SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C1"));

        similarityStats = engine.checkSimilarity("test", c2);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C2"));
    }

    @Test
    public void testDNFPositiveRegexMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("ABC.*")
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();

        engine.add("test", c1);

        final SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C1"));
    }

    @Test
    public void testCNFPositiveRangeMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A4", "A2", "A3"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet("4", "5", "6"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
                                .build())
                        .build())
                .build();

        engine.add("test", c1);
        engine.add("test", c2);

        SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C1"));

        similarityStats = engine.checkSimilarity("test", c2);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C2"));

    }

    @Test
    public void testDNFPositiveExistenceMatch() throws Exception {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(ExistenceDetail.builder()
                                        .build())
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .detail(ExistenceDetail.builder()
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .detail(NonExistenceDetail.builder()
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .detail(ExistenceDetail.builder()
                                        .build())
                                .build())
                        .build())
                .build();

        engine.add("test", c1);
        engine.add("test", c2);

        SimilarityStats similarityStats = engine.checkSimilarity("test", c1);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C1"));

        similarityStats = engine.checkSimilarity("test", c2);
        assertThat(similarityStats.getSimilarities()
                .stream()
                .map(similarity -> similarity.getSimilarCriterias())
                .flatMap(Set::stream)
                .collect(Collectors.toSet()), hasItem("C2"));

    }

}
