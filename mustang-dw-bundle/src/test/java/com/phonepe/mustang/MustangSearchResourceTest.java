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

package com.phonepe.mustang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.mustang.request.SearchRequest;
import com.phonepe.mustang.resources.MustangSearchResource;
import com.phonepe.mustang.service.impl.SearchServiceImpl;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;

@RunWith(MockitoJUnitRunner.class)
public class MustangSearchResourceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Criteria c1 = DNFCriteria.builder()
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
                            .values(Sets.newHashSet(0.000000000000001, 0.000000000000002, 0.000000000000003))
                            .build())
                    .predicate(IncludedPredicate.builder()
                            .lhs("$.p")
                            .values(Sets.newHashSet(true))
                            .build())
                    .build())
            .build();
    private MustangSearchResource resource;

    @Before
    public void before() {
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("test", c1);

        resource = new MustangSearchResource(SearchServiceImpl.builder()
                .engine(engine)
                .build());
    }

    @Test
    public void testSearch() {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        final Set<String> searchResults = resource.search(SearchRequest.builder()
                .indexName("test")
                .requestContext(RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .build())
                .getData();
        assertThat(searchResults, contains("C1"));

        final Set<String> searchResults1 = resource.search(SearchRequest.builder()
                .indexName("test")
                .requestContext(RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .score(true)
                .build())
                .getData();
        assertThat(searchResults1, contains("C1"));
    }

}
