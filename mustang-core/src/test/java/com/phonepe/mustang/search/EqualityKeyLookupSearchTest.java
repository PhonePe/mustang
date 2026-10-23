/**
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
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
package com.phonepe.mustang.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.phonepe.mustang.MustangEngine;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.impl.Conjunction;
import com.phonepe.mustang.composition.impl.Disjunction;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.detail.impl.RangeDetail;
import com.phonepe.mustang.detail.impl.RegexDetail;
import com.phonepe.mustang.predicate.Predicate;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.mustang.preoperation.impl.AdditionPreOperation;
import com.phonepe.mustang.preoperation.impl.ModuloPreOperation;
import com.phonepe.mustang.preoperation.impl.SizePreOperation;
import com.phonepe.mustang.ratify.RatificationResult;

/**
 * Verifies that resolving EQUALITY keys through the hash lookup returns exactly the same search results as evaluating
 * every key (the original behaviour).
 */
public class EqualityKeyLookupSearchTest {

    private static final String INDEX = "test";
    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine lookupEngine;
    private MustangEngine scanEngine;

    @Before
    public void setUp() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        lookupEngine = MustangEngine.builder()
                .mapper(mapper)
                .build();
        scanEngine = MustangEngine.builder()
                .mapper(mapper)
                .equalityKeyLookupEnabled(false)
                .build();
    }

    @Test
    public void testNumericValuesCompareByDoubleValue() {
        add(dnf("INT", included("$.n", 5)));
        add(dnf("LONG", included("$.n", 7L)));
        add(dnf("DOUBLE", included("$.n", 2.5)));
        add(dnf("DECIMAL", included("$.n", new BigDecimal("9.0"))));
        add(dnf("NEG_ZERO", included("$.n", -0.0d)));
        add(dnf("STRING_FIVE", included("$.n", "5")));

        assertResults(request("n", 5), "INT");
        assertResults(request("n", 5.0), "INT");
        assertResults(request("n", 7), "LONG");
        assertResults(request("n", 2.5), "DOUBLE");
        assertResults(request("n", 9), "DECIMAL");
        assertResults(request("n", 0), "NEG_ZERO");
        assertResults(request("n", "5"), "STRING_FIVE");
        assertResults(request("n", 6));
    }

    @Test
    public void testBooleansAndStringsDoNotCrossMatch() {
        add(dnf("BOOL", included("$.b", true)));
        add(dnf("STRING_TRUE", included("$.b", "true")));

        assertResults(request("b", true), "BOOL");
        assertResults(request("b", "true"), "STRING_TRUE");
        assertResults(request("b", false));
    }

    @Test
    public void testListRequestValueUsesFirstElement() {
        add(dnf("X", included("$.ids", "X")));
        add(dnf("Y", included("$.ids", "Y")));

        assertResults(request("ids", Arrays.asList("X", "Y")), "X");
        assertResults(request("ids", Arrays.asList("Y", "X")), "Y");
    }

    @Test
    public void testMissingAndObjectValuesDoNotMatch() {
        add(dnf("A", included("$.a", "A1")));

        assertResults(request("other", "A1"));
        final Map<String, Object> nested = new HashMap<>();
        nested.put("x", "A1");
        assertResults(request("a", nested));
    }

    @Test
    public void testLargeValueListPredicate() {
        final Set<Object> plans = Sets.newHashSet();
        for (int i = 0; i < 200; i++) {
            plans.add("BPP" + i);
        }
        add(dnf("PLANS", IncludedPredicate.builder()
                .lhs("$.pricingPlanId")
                .values(plans)
                .build()));
        add(dnf("OTHER", included("$.pricingPlanId", "MPP1")));

        assertResults(request("pricingPlanId", "BPP137"), "PLANS");
        assertResults(request("pricingPlanId", "MPP1"), "OTHER");
        assertResults(request("pricingPlanId", "BPP999"));
    }

    @Test
    public void testPreOperationsAreAppliedPerGroup() {
        add(dnf("MOD", IncludedPredicate.builder()
                .lhs("$.id")
                .preOperation(ModuloPreOperation.builder()
                        .rhs(100)
                        .build())
                .values(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
                .build()));
        add(dnf("MOD_PLUS_ONE", IncludedPredicate.builder()
                .lhs("$.id")
                .preOperations(Arrays.asList(ModuloPreOperation.builder()
                        .rhs(100)
                        .build(),
                        AdditionPreOperation.builder()
                                .rhs(1)
                                .build()))
                .values(Sets.newHashSet(8))
                .build()));
        add(dnf("IDENTITY", included("$.id", 12307)));
        add(dnf("SIZE", IncludedPredicate.builder()
                .lhs("$.items")
                .preOperation(SizePreOperation.builder()
                        .build())
                .values(Sets.newHashSet(3))
                .build()));

        assertResults(request("id", 12307), "MOD", "MOD_PLUS_ONE", "IDENTITY");
        assertResults(request("id", 12350));
        assertResults(request("items", Arrays.asList("a", "b", "c")), "SIZE");
    }

    @Test
    public void testExcludedEqualityAndMixedCaveats() {
        add(dnf("MIXED", included("$.state", "MH"), ExcludedPredicate.builder()
                .lhs("$.city")
                .values(Sets.newHashSet("PUNE"))
                .build(),
                IncludedPredicate.builder()
                        .lhs("$.amount")
                        .detail(RangeDetail.builder()
                                .lowerBound(500)
                                .includeLowerBound(true)
                                .upperBound(2000)
                                .includeUpperBound(true)
                                .build())
                        .build(),
                IncludedPredicate.builder()
                        .lhs("$.user")
                        .detail(RegexDetail.builder()
                                .regex(".*0[0-9]$")
                                .build())
                        .build()));

        final Map<String, Object> request = new HashMap<>();
        request.put("state", "MH");
        request.put("city", "MUMBAI");
        request.put("amount", 1500);
        request.put("user", "U105");
        assertResults(request, "MIXED");

        request.put("city", "PUNE");
        assertResults(request);

        request.put("city", "MUMBAI");
        request.put("amount", 2500);
        assertResults(request);
    }

    @Test
    public void testCnfCriteria() {
        add(CNFCriteria.builder()
                .id("CNF")
                .disjunction(Disjunction.builder()
                        .predicate(included("$.pincode", "560034"))
                        .predicate(included("$.city", "BLR"))
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.state")
                                .values(Sets.newHashSet("TN"))
                                .build())
                        .build())
                .build());

        assertResults(requestOf("pincode", "560034", "state", "KA"), "CNF");
        assertResults(requestOf("city", "BLR", "state", "KA"), "CNF");
        assertResults(requestOf("city", "BLR", "state", "TN"));
        assertResults(requestOf("city", "MUM", "state", "KA"));
    }

    @Test
    public void testLookupIsRebuiltAfterIndexChanges() {
        final Criteria v1 = dnf("FENCE_OFFER", included("$.fenceId", "FE1"));
        add(v1);
        assertResults(request("fenceId", "FE1"), "FENCE_OFFER");

        final Criteria v2 = dnf("FENCE_OFFER", included("$.fenceId", "FE2"));
        lookupEngine.update(INDEX, v2);
        scanEngine.update(INDEX, v2);
        assertResults(request("fenceId", "FE1"));
        assertResults(request("fenceId", "FE2"), "FENCE_OFFER");

        add(dnf("NEW_OFFER", included("$.fenceId", "FE3")));
        assertResults(request("fenceId", "FE3"), "NEW_OFFER");

        lookupEngine.delete(INDEX, v2);
        scanEngine.delete(INDEX, v2);
        assertResults(request("fenceId", "FE2"));
    }

    @Test
    public void testManyFenceIdsOnOnePath() {
        final List<Criteria> criterias = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            criterias.add(dnf("OFFER_" + i, included("$.fenceId", "FE" + i)));
        }
        lookupEngine.add(INDEX, criterias);
        scanEngine.add(INDEX, criterias);
        assertResults(request("fenceId", "FE432"), "OFFER_432");
        assertResults(request("fenceId", "FE1000"));
    }

    @Test
    public void testRandomisedParityWithScan() {
        final Random random = new Random(42);
        final String[] paths = { "$.a", "$.b", "$.c", "$.d", "$.e" };
        for (int i = 0; i < 400; i++) {
            final int predicates = 1 + random.nextInt(3);
            final List<Predicate> list = new ArrayList<>();
            for (int p = 0; p < predicates; p++) {
                final String path = paths[random.nextInt(paths.length)];
                final Set<Object> values = Sets.newHashSet(randomValue(random), randomValue(random));
                list.add(random.nextInt(5) == 0 ? ExcludedPredicate.builder()
                        .lhs(path)
                        .values(values)
                        .build()
                        : IncludedPredicate.builder()
                                .lhs(path)
                                .values(values)
                                .build());
            }
            if (random.nextBoolean()) {
                add(dnf("D" + i, list.toArray(new Predicate[0])));
            } else {
                final CNFCriteria.CNFCriteriaBuilder builder = CNFCriteria.builder()
                        .id("C" + i);
                list.forEach(predicate -> builder.disjunction(Disjunction.builder()
                        .predicate(predicate)
                        .build()));
                add(builder.build());
            }
        }
        for (int r = 0; r < 1000; r++) {
            final Map<String, Object> request = new HashMap<>();
            for (final String path : paths) {
                if (random.nextInt(4) != 0) {
                    request.put(path.substring(2), randomValue(random));
                }
            }
            assertSameResults(request);
        }
    }

    private Object randomValue(final Random random) {
        final int v = random.nextInt(6);
        switch (random.nextInt(4)) {
        case 0:
            return v;
        case 1:
            return (double) v;
        case 2:
            return "V" + v;
        default:
            return v % 2 == 0;
        }
    }

    private void add(final Criteria criteria) {
        lookupEngine.add(INDEX, criteria);
        scanEngine.add(INDEX, criteria);
    }

    private static Criteria dnf(final String id, final Predicate... predicates) {
        final Conjunction.ConjunctionBuilder conjunction = Conjunction.builder();
        Arrays.stream(predicates)
                .forEach(conjunction::predicate);
        return DNFCriteria.builder()
                .id(id)
                .conjunction(conjunction.build())
                .build();
    }

    private static Predicate included(final String path, final Object value) {
        return IncludedPredicate.builder()
                .lhs(path)
                .values(Sets.newHashSet(value))
                .build();
    }

    private static Map<String, Object> request(final String key, final Object value) {
        final Map<String, Object> request = new HashMap<>();
        request.put(key, value);
        return request;
    }

    private static Map<String, Object> requestOf(final String k1, final Object v1, final String k2, final Object v2) {
        final Map<String, Object> request = request(k1, v1);
        request.put(k2, v2);
        return request;
    }

    private Set<String> assertSameResults(final Map<String, Object> request) {
        final RequestContext context = RequestContext.builder()
                .node(mapper.valueToTree(request))
                .build();
        final Set<String> lookupResults = lookupEngine.search(INDEX, context);
        final Set<String> scanResults = scanEngine.search(INDEX, context);
        assertThat("Mismatch for request " + request, lookupResults, is(scanResults));
        return lookupResults;
    }

    private void assertResults(final Map<String, Object> request, final String... expected) {
        final Set<String> results = assertSameResults(request);
        if (expected.length == 0) {
            assertThat(results, is(empty()));
        } else {
            assertThat(results, containsInAnyOrder(expected));
        }
    }

    private void assertRatified() {
        lookupEngine.ratify(INDEX);
        final RatificationResult result = lookupEngine.getRatificationResult(INDEX);
        assertThat(result.getStatus(), is(true));
        assertThat(result.getAnamolyDetails(), is(empty()));
    }
}
