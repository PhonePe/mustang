package com.phonepe.growth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonepe.growth.mustang.MustangEngine;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.index.IndexingFacade;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class IndexTest {


    private  MustangEngine engine;
    final ObjectMapper mapper = new ObjectMapper();
    @Before
    public void setUp() throws Exception {
        engine = MustangEngine.builder().mapper(mapper).build();
    }

    @Test
    public void testDnfIndexing() {
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet(0.1000000000001,0.20000000000002,0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.p").values(Sets.newHashSet(true)).build())
                .build()).build();
        engine.index("test", c1);
        IndexGroup index = engine.getIndexingFacde().get("test");
        Assert.assertEquals(1, index.getDnfInvertedIndex().getTable().size());
        Assert.assertEquals(8, index.getDnfInvertedIndex().getTable().get(3).size());

    }

    @Test
    public void testDnfIndexingWithOnlyExcludePredicate() {
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .build()).build();
        engine.index("test", c1);
        IndexGroup index = engine.getIndexingFacde().get("test");
        final Key key = Key.builder().name("ZZZ").value(0).upperBoundScore(0).build();
        Assert.assertEquals(1, index.getDnfInvertedIndex().getTable().size());
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(0).containsKey(key));
        Assert.assertEquals(3, index.getDnfInvertedIndex().getTable().get(0).size());
        Assert.assertTrue(true);
    }


    @Test
    public void testDnfIndexingWithMultiplePredicate() {
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .build()).build();
        Criteria c2 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.n").values(Sets.newHashSet(0.1000000000001,0.20000000000002,0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.p").values(Sets.newHashSet(true)).build())
                .build()).build();
        Criteria c3 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .build()).build();
        Criteria c4 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhsPath("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhsPath("$.d").values(Sets.newHashSet(true)).build())
                .predicate(ExcludedPredicate.builder().lhsPath("$.d").values(Sets.newHashSet(true)).build())
                .build()).build();
        engine.index("test", Arrays.asList(c1, c2, c3, c4));
        IndexGroup index = engine.getIndexingFacde().get("test");

        /* Asserions for index size */
        Assert.assertEquals(4, index.getDnfInvertedIndex().getTable().size());

        /* Asserions for keys in k = 0 */
        final Key zKey = Key.builder().name("ZZZ").value(0).upperBoundScore(0).build();
        Assert.assertEquals(3, index.getDnfInvertedIndex().getTable().get(0).size());
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(0).containsKey(zKey));
        final Key key = Key.builder().name("$.b").value("B1").upperBoundScore(0).build();
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(0).containsKey(key));
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(0).get(key).first().getEId().equals("C1"));

        /* Asserions for keys in k = 1 */
        Assert.assertEquals(4, index.getDnfInvertedIndex().getTable().get(1).size());
        final Key akeyC3 = Key.builder().name("$.a").value("A1").upperBoundScore(0).build();
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(1).containsKey(akeyC3));

        /* Asserions for keys in k = 2 */
        Assert.assertEquals(5, index.getDnfInvertedIndex().getTable().get(2).size());
        final Key keyK2 = Key.builder().name("$.a").value("A1").upperBoundScore(0).build();
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(2).containsKey(keyK2));

        /* Asserions for keys in k = 3 */
        Assert.assertEquals(8, index.getDnfInvertedIndex().getTable().get(3).size());
        Assert.assertFalse(index.getDnfInvertedIndex().getTable().get(3).containsKey(zKey));
        final Key aKey = Key.builder().name("$.a").value("A1").upperBoundScore(0).build();
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(3).containsKey(aKey));
        Assert.assertTrue(true);
    }

}
