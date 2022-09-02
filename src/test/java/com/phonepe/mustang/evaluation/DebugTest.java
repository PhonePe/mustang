/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phonepe.mustang.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
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
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DebugTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;

    @Before
    public void setUp() throws Exception {
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
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testDNFPositiveRegexMatch() throws Exception {
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
                                .values(Sets.newHashSet("B1"))
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
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testDNFPositiveRangeMatch() throws Exception {
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
                                .values(Sets.newHashSet("B1"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.300000000003)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", true);

        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testDNFNegativeMatch() throws Exception {

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(7)
                                        .build())
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A");
        testQuery.put("n", 7);

        Assert.assertFalse(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());

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
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A4");
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", false);

        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testCNFRegexMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.?")
                                        .build())
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A4");
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", false);

        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testCNFPositiveRangeMatch() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.1000000000001)
                                        .build())
                                .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.p")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A4");
        testQuery.put("b", "B1");
        testQuery.put("n", 0.300000000003);
        testQuery.put("p", false);

        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testCNFNegativeMatch() throws Exception {

        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "C");
        testQuery.put("n", "7");

        Assert.assertFalse(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());

    }

    @Test
    public void testCNFNegativeRegexMatch() throws Exception {

        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .detail(RegexDetail.builder()
                                        .regex("A.*")
                                        .build())
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(6)
                                        .build())
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "C");
        testQuery.put("n", 6);

        Assert.assertFalse(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());

    }

    @Test
    public void testDNFPositiveMultipleRangeCheck() throws Exception {

        final Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", false);

        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c2,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000004)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c3,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000002)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c4,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c5 = DNFCriteria.builder()
                .id("C5")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c5,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c6 = DNFCriteria.builder()
                .id("C6")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c6,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c7 = DNFCriteria.builder()
                .id("C7")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c7,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c8 = DNFCriteria.builder()
                .id("C8")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000004)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c8,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testCNFPositiveMultipleRangeCheck() throws Exception {
        final Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        testQuery.put("b", "B3");
        testQuery.put("n", 0.000000000000003);
        testQuery.put("p", false);

        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c2,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000004)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c3,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .upperBound(0.000000000000002)
                                        .includeUpperBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c4,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c5 = CNFCriteria.builder()
                .id("C5")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c5,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c6 = CNFCriteria.builder()
                .id("C6")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000003)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c6,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c7 = CNFCriteria.builder()
                .id("C7")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000002)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertTrue(engine.debug(c7,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
        Criteria c8 = CNFCriteria.builder()
                .id("C8")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .detail(RangeDetail.builder()
                                        .lowerBound(0.000000000000004)
                                        .includeLowerBound(true)
                                        .build())
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c8,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());
    }

    @Test
    public void testPredicateWithAbsentPath() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", "7"); // path $.a is not being given explicitly

        Assert.assertFalse(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());

    }

    @Test
    public void testCriteriaHavingPredicateWithAbsentPath() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("n", "7"); // path $.a is not being given explicitly

        Assert.assertTrue(engine.evaluate(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build()));

    }

    @Test
    public void testPredicateWithANonJsonPath() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("A") // Not a valid path
                                .values(Sets.newHashSet("A", "B"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.n")
                                .values(Sets.newHashSet(1, 2, 3))
                                .build())
                        .build())
                .build();
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "B"); // Doesn't matter as the predicate setup above doesn't point to valid path.
        testQuery.put("n", "7");

        Assert.assertFalse(engine.debug(c1,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());

        Criteria c2 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("1")
                                .values(Sets.newHashSet("6", "7")) // Never satisfied
                                .build())
                        .build())
                .build();
        Assert.assertFalse(engine.debug(c2,
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build())
                .isResult());

    }
}
