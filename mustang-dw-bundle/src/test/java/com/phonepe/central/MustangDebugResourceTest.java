package com.phonepe.central;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.central.mustang.request.DebugRequest;
import com.phonepe.central.mustang.request.IndexExportRequest;
import com.phonepe.central.mustang.request.IndexRatificationRequest;
import com.phonepe.central.mustang.request.IndexSnapshotRequest;
import com.phonepe.central.mustang.resources.MustangDebugResource;
import com.phonepe.central.mustang.service.impl.DebugServiceImpl;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

@RunWith(MockitoJUnitRunner.class)
public class MustangDebugResourceTest {

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
                            .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                            .build())
                    .predicate(IncludedPredicate.builder()
                            .lhs("$.p")
                            .values(Sets.newHashSet(true))
                            .build())
                    .build())
            .build();
    private MustangDebugResource resource;

    @Before
    public void before() {
        final MustangEngine engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        engine.add("test", c1);

        resource = new MustangDebugResource(DebugServiceImpl.builder()
                .engine(engine)
                .build());
    }

    @Test
    public void testDebug() {
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        Assert.assertTrue(resource.debug(DebugRequest.builder()
                .criteria(c1)
                .requestContext(RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .build())
                .getData()
                .isResult());
    }

    @Test
    public void testExportIndex() {
        Assert.assertNotNull(resource.exportIndex(IndexExportRequest.builder()
                .indexName("test")
                .build())
                .getData());
    }

    @Test
    public void testSnapshot() {
        Assert.assertNotNull(resource.snapshot(IndexSnapshotRequest.builder()
                .indexName("test")
                .build())
                .getData());
    }

    @Test
    public void testRatify() {
        Assert.assertTrue(resource.ratify(IndexRatificationRequest.builder()
                .indexName("test")
                .build())
                .getData()
                .getStatus());
    }

}
