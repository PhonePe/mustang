package com.phonepe.central;

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
import com.phonepe.central.mustang.request.SearchRequest;
import com.phonepe.central.mustang.resources.MustangSearchResource;
import com.phonepe.central.mustang.service.impl.SearchServiceImpl;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

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
    }

}
