/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.index;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.phonepe.growth.mustang.exception.ErrorCode;
import com.phonepe.growth.mustang.exception.MustangException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.detail.Caveat;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class IndexTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = MustangEngine.builder()
                .mapper(mapper)
                .build();
    }

    @Test
    public void testDnfIndexing() {
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
        engine.add("test", c1);
        // Indexing Size assertion
        Assert.assertEquals(1,
                engine.getIndexingFacde()
                        .getIndexMap()
                        .size());
        // Fetch for a specific Index Group
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        Assert.assertEquals(1,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(8,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(3)
                        .size());

    }

    @Test
    public void testDnfIndexingWithOnlyExcludePredicate() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        // Indexing Size assertion
        Assert.assertEquals(1,
                engine.getIndexingFacde()
                        .getIndexMap()
                        .size());
        // Fetch for a specific Index Group
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        final Key key = Key.builder()
                .name("ZZZ")
                .value(0)
                .upperBoundScore(0)
                .build();
        Assert.assertEquals(1,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertTrue(index.getDnfInvertedIndex()
                .getTable()
                .get(0)
                .containsKey(key));
        Assert.assertEquals(3,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());
    }

    @Test
    public void testDnfIndexingWithMultiplePredicate() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
                .id("C2")
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
        Criteria c3 = DNFCriteria.builder()
                .id("C3")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c4 = DNFCriteria.builder()
                .id("C4")
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
                                .lhs("$.d")
                                .values(Sets.newHashSet(true))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.d")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        engine.add("test", Arrays.asList(c1, c2, c3, c4));
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");

        /* Asserions for index size */
        Assert.assertEquals(4,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());

        /* Asserions for keys in k = 0 */
        final Key zKey = Key.builder()
                .name("ZZZ")
                .value(0)
                .upperBoundScore(0)
                .build();
        Assert.assertEquals(3,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());
        Assert.assertTrue(index.getDnfInvertedIndex()
                .getTable()
                .get(0)
                .containsKey(zKey));
        final Key key = Key.builder()
                .name("$.b")
                .caveat(Caveat.EQUALITY)
                .value("B1")
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getDnfInvertedIndex()
                .getTable()
                .get(0)
                .containsKey(key));
        Assert.assertTrue(index.getDnfInvertedIndex()
                .getTable()
                .get(0)
                .get(key)
                .firstEntry()
                .getValue()
                .getEId()
                .equals("C1"));

        /* Asserions for keys in k = 1 */
        Assert.assertEquals(4,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(1)
                        .size());
        final Key akeyC3 = Key.builder()
                .name("$.a")
                .caveat(Caveat.EQUALITY)
                .value("A1")
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getDnfInvertedIndex()
                .getTable()
                .get(1)
                .containsKey(akeyC3));

        /* Asserions for keys in k = 2 */
        Assert.assertEquals(5,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(2)
                        .size());
        final Key keyK2 = Key.builder()
                .name("$.a")
                .caveat(Caveat.EQUALITY)
                .value("A1")
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getDnfInvertedIndex()
                .getTable()
                .get(2)
                .containsKey(keyK2));

        /* Asserions for keys in k = 3 */
        Assert.assertEquals(8,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(3)
                        .size());
        Assert.assertFalse(index.getDnfInvertedIndex()
                .getTable()
                .get(3)
                .containsKey(zKey));
        final Key aKey = Key.builder()
                .name("$.a")
                .caveat(Caveat.EQUALITY)
                .value("A1")
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getDnfInvertedIndex()
                .getTable()
                .get(3)
                .containsKey(aKey));
    }

    @Test(expected = MustangException.class)
    public void testDnfIndexingWithMultiplePredicateWithSameId() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder()
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
        engine.add("test", Arrays.asList(c1, c2));
        Assert.fail("MustangException should have been thrown");
    }

    @Test
    public void testCnfIndexing() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.user_id")
                                .values(Sets.newHashSet(1, 2))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.age")
                                .values(Sets.newHashSet(25, 30))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.premium")
                                .values(Sets.newHashSet(true))
                                .build())
                        .build())
                .build();
        Disjunction d1 = Disjunction.builder()
                .predicate(IncludedPredicate.builder()
                        .lhs("$.user_id")
                        .values(Sets.newHashSet(1, 2))
                        .build())
                .predicate(ExcludedPredicate.builder()
                        .lhs("$.age")
                        .values(Sets.newHashSet(25, 30))
                        .build())
                .predicate(IncludedPredicate.builder()
                        .lhs("$.premium")
                        .values(Sets.newHashSet(true))
                        .build())
                .build();
        Disjunction d2 = Disjunction.builder()
                .predicate(IncludedPredicate.builder()
                        .lhs("$.user_id")
                        .values(Sets.newHashSet(1, 2))
                        .build())
                .predicate(IncludedPredicate.builder()
                        .lhs("$.age")
                        .values(Sets.newHashSet(35, 40))
                        .build())
                .predicate(IncludedPredicate.builder()
                        .lhs("$.premium")
                        .values(Sets.newHashSet(true))
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
                .disjunctions(Arrays.asList(d1, d2))
                .build();
        engine.add("test", Arrays.asList(c1, c2));
        // Indexing Size assertion
        Assert.assertEquals(1,
                engine.getIndexingFacde()
                        .getIndexMap()
                        .size());
        // Fetch for a specific Index Group
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        final Key zKey = Key.builder()
                .name("ZZZ")
                .value(0)
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(0)
                .containsKey(zKey));
        Assert.assertEquals(2,
                index.getCnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(6,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());
        Assert.assertEquals(10,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(1)
                        .size());

    }

    @Test
    public void testCnfIndexingWithOnlyExcludePredicate() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.user_id")
                                .values(Sets.newHashSet(5, 6))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        final Key key = Key.builder()
                .name("ZZZ")
                .value(0)
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(0)
                .containsKey(key));
        Assert.assertEquals(1,
                index.getCnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(3,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());
    }

    @Test
    public void testCnfIndexingWithMultiplePredicate() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Criteria c2 = CNFCriteria.builder()
                .id("C2")
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
        Criteria c3 = CNFCriteria.builder()
                .id("C3")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A3", "A4"))
                                .build())
                        .build())
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.b")
                                .values(Sets.newHashSet("B1", "B2"))
                                .build())
                        .build())
                .build();
        Disjunction d1 = Disjunction.builder()
                .predicate(IncludedPredicate.builder()
                        .lhs("$.a")
                        .values(Sets.newHashSet("A1", "A2"))
                        .build())
                .predicate(IncludedPredicate.builder()
                        .lhs("$.d")
                        .values(Sets.newHashSet(true))
                        .build())
                .build();

        Disjunction d2 = Disjunction.builder()
                .predicate(IncludedPredicate.builder()
                        .lhs("$.user_id")
                        .values(Sets.newHashSet("20", "22"))
                        .build())
                .predicate(IncludedPredicate.builder()
                        .lhs("$.amount")
                        .values(Sets.newHashSet(100, 150, 200))
                        .build())
                .build();

        Disjunction d3 = Disjunction.builder()
                .predicate(IncludedPredicate.builder()
                        .lhs("$.user_id")
                        .values(Sets.newHashSet("20", "22"))
                        .build())
                .predicate(IncludedPredicate.builder()
                        .lhs("$.amount")
                        .values(Sets.newHashSet(100, 150, 200))
                        .build())
                .predicate(IncludedPredicate.builder()
                        .lhs("$.age")
                        .values(Sets.newHashSet(25, 28, 30))
                        .build())
                .build();

        Criteria c4 = CNFCriteria.builder()
                .id("C4")
                .disjunctions(Arrays.asList(d1, d2, d3))
                .build();
        engine.add("test", Arrays.asList(c1, c2, c3, c4));
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");

        /* Asserions for index size */
        Assert.assertEquals(4,
                index.getCnfInvertedIndex()
                        .getTable()
                        .size());

        /* Asserions for keys in k = 0 */
        final Key zKey = Key.builder()
                .name("ZZZ")
                .value(0)
                .upperBoundScore(0)
                .build();
        Assert.assertEquals(9,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(0)
                .containsKey(zKey));
        final Key key = Key.builder()
                .name("$.b")
                .caveat(Caveat.EQUALITY)
                .value("B2")
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(0)
                .containsKey(key));
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(0)
                .get(key)
                .firstEntry()
                .getValue()
                .getEId()
                .equals("C2"));

        /* Asserions for keys in k = 1 */
        Assert.assertEquals(6,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(2)
                        .size());
        final Key bkey = Key.builder()
                .name("$.b")
                .caveat(Caveat.EQUALITY)
                .value("B1")
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(1)
                .containsKey(bkey));

        /* Asserions for keys in k = 2 */
        Assert.assertEquals(6,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(2)
                        .size());
        final Key keyK2 = Key.builder()
                .name("$.a")
                .caveat(Caveat.EQUALITY)
                .value("A1")
                .order(0)
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(2)
                .containsKey(keyK2));

        /* Asserions for keys in k = 3 */
        Assert.assertEquals(16,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(3)
                        .size());
        Assert.assertFalse(index.getCnfInvertedIndex()
                .getTable()
                .get(3)
                .containsKey(zKey));
        final Key aKey = Key.builder()
                .name("$.user_id")
                .caveat(Caveat.EQUALITY)
                .value("22")
                .order(1)
                .upperBoundScore(0)
                .build();
        Assert.assertTrue(index.getCnfInvertedIndex()
                .getTable()
                .get(3)
                .containsKey(aKey));
    }

    @Test
    public void getInvalidIndexGroup() {
        IndexGroup index = null;
        try {
            index = engine.getIndexingFacde()
                    .getIndexGroup("test");
            ;
        } catch (MustangException e) {
            Assert.assertEquals("INDEX_NOT_FOUND",
                    e.getErrorCode()
                            .toString());
        }
        Assert.assertNull(index);
    }

    @Test
    public void testDnfIndexingSingleIncludePredicateAndSingleCriteria() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        Assert.assertEquals(1,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(2,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(1)
                        .size());
    }

    @Test
    public void testDnfIndexingSingleExcludePredicateAndSingleCriteria() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        Assert.assertEquals(1,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(3,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());

    }

    @Test
    public void testDnfIndexingSingleExcludeAndIncludePredicateAndSingleCriteria() {
        Criteria c1 = DNFCriteria.builder()
                .id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A20"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        Assert.assertEquals(1,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(4,
                index.getDnfInvertedIndex()
                        .getTable()
                        .get(1)
                        .size());
    }

    @Test
    public void testCnfIndexingSingleIncludePredicateAndSingleCriteria() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        // Check value in DNF index
        Assert.assertEquals(0,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        // Check value in CNF index
        Assert.assertEquals(1,
                index.getCnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(2,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(1)
                        .size());
    }

    @Test
    public void testCnfIndexingSingleExcludePredicateAndSingleCriteria() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        // Check value in DNF index
        Assert.assertEquals(0,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        // Check value in CNF index
        Assert.assertEquals(1,
                index.getCnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(3,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());

    }

    @Test
    public void testCnfIndexingSingleExcludeAndIncludePredicateAndSingleCriteria() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A20"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);

        IndexGroup index = engine.getIndexingFacde()
                .getIndexGroup("test");
        // Check value in DNF index
        Assert.assertEquals(0,
                index.getDnfInvertedIndex()
                        .getTable()
                        .size());
        // Check value in CNF index
        Assert.assertEquals(1,
                index.getCnfInvertedIndex()
                        .getTable()
                        .size());
        Assert.assertEquals(5,
                index.getCnfInvertedIndex()
                        .getTable()
                        .get(0)
                        .size());
    }

    @Test(expected = MustangException.class)
    public void testIndexReplacement1() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A20"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        engine.replaceIndex("test", "testNew");
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A1");
        engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.fail("Search on non-existant index should fail");

    }

    @Test
    public void testIndexReplacement2() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A20"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("testNew", c1);
        engine.replaceIndex("test", "testNew");
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.size() == 1);
        Assert.assertTrue(searchResults.contains("C1"));
    }

    @Test
    public void testIndexReplacement3() {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A20"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        engine.add("test", c1);
        c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A20"))
                                .build())
                        .build())
                .build();
        engine.add("testNew", c1);
        engine.replaceIndex("test", "testNew");
        Map<String, Object> testQuery = Maps.newHashMap();
        testQuery.put("a", "A10");
        final Set<String> searchResults = engine.search("test",
                RequestContext.builder()
                        .node(mapper.valueToTree(testQuery))
                        .build());
        Assert.assertTrue(searchResults.isEmpty());
    }

    @Test
    public void testDeleteOnNonIndexedCriteria() throws Exception {
        Criteria c1 = CNFCriteria.builder()
                .id("C1")
                .disjunction(Disjunction.builder()
                        .predicate(IncludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A10", "A20"))
                                .build())
                        .predicate(ExcludedPredicate.builder()
                                .lhs("$.a")
                                .values(Sets.newHashSet("A1", "A2"))
                                .build())
                        .build())
                .build();
        try {
            engine.delete("test", c1);
            Assert.fail("Mustang Exception should have been thrown");
        } catch (MustangException e) {
            Assert.assertTrue(ErrorCode.INDEX_NOT_FOUND.equals(e.getErrorCode()));
        }

    }

}
