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
package com.phonepe.growth.mustang.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.criteria.tautology.TautologicalCriteria;
import com.phonepe.growth.mustang.detail.impl.EqualityDetail;
import com.phonepe.growth.mustang.exception.ErrorCode;
import com.phonepe.growth.mustang.exception.MustangException;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.growth.mustang.ratify.RatificationResult;

public class ExportImportTest {

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
    public void testExportImport() throws IOException {

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("fastag_campaigns_offers"))
                                        .build())
                                .build())
                        .build())
                .build();
        Criteria c11 = DNFCriteria.builder()
                .id("C11")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("fastag_campaigns_offers"))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("F"))
                                .build())
                        .build())
                .build();
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .build())
                .build();
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.g")
                                .values(Sets.newHashSet("M"))
                                .build())
                        .build())
                .build();
        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet(3, 4))
                                .build())
                        .build())
                .build();
        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.s")
                                .values(Sets.newHashSet("CA", "NY"))
                                .build())
                        .build())
                .build();

        Criteria c7 = TautologicalCriteria.generate(CriteriaForm.CNF, "C7");
        Criteria c8 = TautologicalCriteria.generate(CriteriaForm.DNF, "C8");
        Criteria c9 = TautologicalCriteria.generate(CriteriaForm.CNF, "C9");

        // Index ingestion
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        engine.add("testsearch", c1);
        engine.add("testsearch", c11);
        engine.add("testsearch", c2);
        engine.add("testsearch", c3);
        engine.add("testsearch", c4);
        engine.add("testsearch", c5);
        engine.add("testsearch", c6);
        engine.add("testsearch", c7);
        engine.add("testsearch", c8);
        engine.add("testsearch", c9);

        // Request Map
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("s", "CA");
        testQuery.put("g", "M");
        // Search query for same criteria
        final RequestContext request = RequestContext.builder()
                .node(mapper.valueToTree(testQuery))
                .build();
        final Set<String> searchResults = engine.search("testsearch", request);
        assertThat(searchResults, hasSize(6));
        assertThat(searchResults, containsInAnyOrder("C4", "C11", "C7", "C8", "C9", "C1"));
        String indexGroup = engine.exportIndexGroup("testsearch");
        assertNotNull(indexGroup);

        engine.ratify("testsearch");
        final RatificationResult ratificationResult = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult.getStatus(), is(true));
        assertThat(ratificationResult.getAnamolyDetails(), is(empty()));

        final MustangEngine engine1 = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine1.importIndexGroup("testsearch", indexGroup);

        final Set<String> searchResults1 = engine1.search("testsearch", request);
        assertThat(searchResults1, hasSize(6));
        assertThat(searchResults1, containsInAnyOrder("C4", "C11", "C7", "C8", "C9", "C1"));
        String indexGroup1 = engine1.exportIndexGroup("testsearch");
        assertNotNull(indexGroup1);

        engine1.ratify("testsearch");
        final RatificationResult ratificationResult1 = engine.getRatificationResult("testsearch");
        assertThat(ratificationResult1.getStatus(), is(true));
        assertThat(ratificationResult1.getAnamolyDetails(), is(empty()));

        assertNotNull(engine.snapshot("testsearch"));
        assertNotNull(engine1.snapshot("testsearch"));
    }

    @Test
    public void testExportNonExistantIndexGroup() throws IOException {
        try {
            engine.exportIndexGroup("nonExistent");
            Assert.fail("should have thrown exception");
        } catch (MustangException e) {
            assertTrue(ErrorCode.INDEX_NOT_FOUND.equals(e.getErrorCode()));
        }
    }

    @Test
    public void testImportFailureDueToPreExistence() throws IOException {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("fastag_campaigns_offers"))
                                        .build())
                                .build())
                        .build())
                .build();
        engine.add("testsearch", c1);
        String indexGroup = engine.exportIndexGroup("testsearch");
        final MustangEngine engine1 = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine1.add("testsearch", c1);

        try {
            engine1.importIndexGroup("testsearch", indexGroup);
            Assert.fail("should have thrown exception");
        } catch (MustangException e) {
            assertTrue(ErrorCode.INDEX_GROUP_EXISTS.equals(e.getErrorCode()));
        }
    }

    @Test
    public void testImportFailure() throws IOException {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("fastag_campaigns_offers"))
                                        .build())
                                .build())
                        .build())
                .build();
        engine.add("testsearch", c1);
        String indexGroup = engine.exportIndexGroup("testsearch");

        final ObjectMapper mapperMock = mock(ObjectMapper.class);

        final MustangEngine engine1 = MustangEngine.builder()
                .mapper(mapperMock)
                .build();

        doThrow(IOException.class).when(mapperMock)
                .readValue(Mockito.anyString(), Matchers.<TypeReference<List<Criteria>>>any());

        try {
            engine1.importIndexGroup("testsearch", indexGroup);
            Assert.fail("should have thrown exception");
        } catch (MustangException e) {
            assertTrue(ErrorCode.INDEX_IMPORT_ERROR.equals(e.getErrorCode()));
        }
    }

    @Test
    public void testSnapshotFailure() throws IOException {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .detail(EqualityDetail.builder()
                                        .values(Sets.newHashSet("fastag_campaigns_offers"))
                                        .build())
                                .build())
                        .build())
                .build();
        final ObjectMapper mapperMock = mock(ObjectMapper.class);
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapperMock)
                .build();
        engine.add("test", c1);

        doThrow(JsonProcessingException.class).when(mapperMock)
                .writeValueAsString(Mockito.anyObject());

        try {
            engine.snapshot("test");
            Assert.fail("should have thrown exception");
        } catch (MustangException e) {
            assertTrue(ErrorCode.INTERNAL_ERROR.equals(e.getErrorCode()));
        }
    }

}
