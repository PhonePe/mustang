package com.phonepe.central;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.central.mustang.request.ScanCriteriaRequest;
import com.phonepe.central.mustang.request.ScanIndexRequest;
import com.phonepe.central.mustang.resources.MustangScanResource;
import com.phonepe.central.mustang.service.impl.ScanServiceImpl;
import com.phonepe.central.mustang.MustangEngine;
import com.phonepe.central.mustang.common.RequestContext;
import com.phonepe.central.mustang.composition.impl.Conjunction;
import com.phonepe.central.mustang.criteria.Criteria;
import com.phonepe.central.mustang.criteria.impl.DNFCriteria;
import com.phonepe.central.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.central.mustang.predicate.impl.IncludedPredicate;

@RunWith(MockitoJUnitRunner.class)
public class MustangScanResourceTest {

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
    private MustangScanResource resource;

    @Before
    public void before() {
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("test", c1);

        resource = new MustangScanResource(ScanServiceImpl.builder()
                .engine(engine)
                .build());
    }

    @Test
    public void testScanIndex() {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        final Set<String> searchResults = resource.scan(ScanIndexRequest.builder()
                .indexName("test")
                .requestContext(RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .build())
                .getData();
        assertThat(searchResults, contains("C1"));
    }

    @Test
    public void testScanCriteria() {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", true);

        final Set<String> searchResults = resource.scan(ScanCriteriaRequest.builder()
                .criterias(List.of(c1))
                .requestContext(RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .build())
                .getData()
                .stream()
                .map(Criteria::getId)
                .collect(Collectors.toSet());
        assertThat(searchResults, contains("C1"));
    }

}
