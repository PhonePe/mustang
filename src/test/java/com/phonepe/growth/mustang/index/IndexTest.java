package com.phonepe.growth.mustang.index;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.composition.impl.Conjunction;
import com.phonepe.growth.mustang.composition.impl.Disjunction;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

public class IndexTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private MustangEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = MustangEngine.builder().mapper(mapper).build();
    }

    @Test
    public void testDnfIndexing() {
        Criteria c1 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n")
                        .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhs("$.p").values(Sets.newHashSet(true)).build()).build())
                .build();
        engine.index("test", c1);
        IndexGroup index = engine.getIndexingFacde().getIndexGroup("test");
        Assert.assertEquals(1, index.getDnfInvertedIndex().getTable().size());
        Assert.assertEquals(8, index.getDnfInvertedIndex().getTable().get(3).size());

    }

    @Test
    public void testDnfIndexingWithOnlyExcludePredicate() {
        Criteria c1 = DNFCriteria.builder().id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(
                                ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                        .build())
                .build();
        engine.index("test", c1);
        IndexGroup index = engine.getIndexingFacde().getIndexGroup("test");
        final Key key = Key.builder().name("ZZZ").value(0).upperBoundScore(0).build();
        Assert.assertEquals(1, index.getDnfInvertedIndex().getTable().size());
        Assert.assertTrue(index.getDnfInvertedIndex().getTable().get(0).containsKey(key));
        Assert.assertEquals(3, index.getDnfInvertedIndex().getTable().get(0).size());
    }

    @Test
    public void testDnfIndexingWithMultiplePredicate() {
        Criteria c1 = DNFCriteria.builder().id("C1")
                .conjunction(Conjunction.builder()
                        .predicate(
                                ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                        .build())
                .build();
        Criteria c2 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n")
                        .values(Sets.newHashSet(0.1000000000001, 0.20000000000002, 0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhs("$.p").values(Sets.newHashSet(true)).build()).build())
                .build();
        Criteria c3 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .build()).build();
        Criteria c4 = DNFCriteria.builder().id("C1").conjunction(Conjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.d").values(Sets.newHashSet(true)).build())
                .predicate(ExcludedPredicate.builder().lhs("$.d").values(Sets.newHashSet(true)).build()).build())
                .build();
        engine.index("test", Arrays.asList(c1, c2, c3, c4));
        IndexGroup index = engine.getIndexingFacde().getIndexGroup("test");

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
    }


    @Test
    public void testCnfIndexing() {
        Criteria c1 = CNFCriteria.builder().id("C1").disjunction(Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.user_id").values(Sets.newHashSet(1, 2)).build())
                .predicate(ExcludedPredicate.builder().lhs("$.age").values(Sets.newHashSet(25, 30)).build())
                .predicate(IncludedPredicate.builder().lhs("$.premium").values(Sets.newHashSet(true)).build())
                .build()).build();
        Disjunction d1 = Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.user_id").values(Sets.newHashSet(1, 2)).build())
                .predicate(ExcludedPredicate.builder().lhs("$.age").values(Sets.newHashSet(25, 30)).build())
                .predicate(IncludedPredicate.builder().lhs("$.premium").values(Sets.newHashSet(true)).build())
                .build();
        Disjunction d2 = Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.user_id").values(Sets.newHashSet(1, 2)).build())
                .predicate(IncludedPredicate.builder().lhs("$.age").values(Sets.newHashSet(35, 40)).build())
                .predicate(IncludedPredicate.builder().lhs("$.premium").values(Sets.newHashSet(true)).build())
                .build();
        Criteria c2 = CNFCriteria.builder().id("C2").disjunctions(Arrays.asList(d1, d2)).build();
        engine.index("test", Arrays.asList(c1, c2));
        IndexGroup index = engine.getIndexingFacde().getIndexGroup("test");
        final Key zKey = Key.builder().name("ZZZ").value(0).upperBoundScore(0).build();
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(0).containsKey(zKey));
        Assert.assertEquals(2, index.getCnfInvertedIndex().getTable().size());
        Assert.assertEquals(6, index.getCnfInvertedIndex().getTable().get(0).size());
        Assert.assertEquals(5, index.getCnfInvertedIndex().getTable().get(1).size());

    }



    @Test
    public void testCnfIndexingWithOnlyExcludePredicate() {
        Criteria c1 = CNFCriteria.builder().id("C1").disjunction(Disjunction.builder()
                .predicate(ExcludedPredicate.builder().lhs("$.user_id").values(Sets.newHashSet(5, 6)).build())
                .build()).build();
        engine.index("test", c1);
        IndexGroup index = engine.getIndexingFacde().getIndexGroup("test");
        final Key key = Key.builder().name("ZZZ").value(0).upperBoundScore(0).build();
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(0).containsKey(key));
        Assert.assertEquals(1, index.getCnfInvertedIndex().getTable().size());
        Assert.assertEquals(3, index.getCnfInvertedIndex().getTable().get(0).size());
    }



    @Test
    public void testCnfIndexingWithMultiplePredicate() {
        Criteria c1 = CNFCriteria.builder().id("C1").disjunction(Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .build()).build();
        Criteria c2 = CNFCriteria.builder().id("C2").disjunction(Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(ExcludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.n").values(Sets.newHashSet(0.1000000000001,0.20000000000002,0.300000000003)).build())
                .predicate(IncludedPredicate.builder().lhs("$.p").values(Sets.newHashSet(true)).build())
                .build()).build();
        Criteria c3 = CNFCriteria.builder().id("C3").disjunction(Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A3", "A4")).build())
                .build())
                .disjunction(Disjunction.builder()
                    .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                    .predicate(IncludedPredicate.builder().lhs("$.b").values(Sets.newHashSet("B1", "B2")).build())
                    .build()
                )
                .build();
        Disjunction d1 = Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.a").values(Sets.newHashSet("A1", "A2")).build())
                .predicate(IncludedPredicate.builder().lhs("$.d").values(Sets.newHashSet(true)).build())
                .build();

        Disjunction d2 = Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.user_id").values(Sets.newHashSet("20", "22")).build())
                .predicate(IncludedPredicate.builder().lhs("$.amount").values(Sets.newHashSet(100, 150,200)).build())
                .build();

        Disjunction d3 = Disjunction.builder()
                .predicate(IncludedPredicate.builder().lhs("$.user_id").values(Sets.newHashSet("20", "22")).build())
                .predicate(IncludedPredicate.builder().lhs("$.amount").values(Sets.newHashSet(100, 150,200)).build())
                .predicate(IncludedPredicate.builder().lhs("$.age").values(Sets.newHashSet(25, 28,30)).build())
                .build();

        Criteria c4 = CNFCriteria.builder().id("C4").disjunctions(Arrays.asList(d1, d2, d3)).build();
        engine.index("test", Arrays.asList(c1, c2, c3, c4));
        IndexGroup index = engine.getIndexingFacde().getIndexGroup("test");

        /* Asserions for index size */
        Assert.assertEquals(4, index.getCnfInvertedIndex().getTable().size());

        /* Asserions for keys in k = 0 */
        final Key zKey = Key.builder().name("ZZZ").value(0).upperBoundScore(0).build();
        Assert.assertEquals(9, index.getCnfInvertedIndex().getTable().get(0).size());
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(0).containsKey(zKey));
        final Key key = Key.builder().name("$.b").value("B2").upperBoundScore(0).build();
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(0).containsKey(key));
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(0).get(key).first().getEId().equals("C2"));

        /* Asserions for keys in k = 1 */
        Assert.assertEquals(4, index.getCnfInvertedIndex().getTable().get(2).size());
        final Key bkey = Key.builder().name("$.b").value("B1").upperBoundScore(0).build();
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(1).containsKey(bkey));

        /* Asserions for keys in k = 2 */
        Assert.assertEquals(4, index.getCnfInvertedIndex().getTable().get(2).size());
        final Key keyK2 = Key.builder().name("$.a").value("A1").upperBoundScore(0).build();
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(2).containsKey(keyK2));

        /* Asserions for keys in k = 3 */
        Assert.assertEquals(8, index.getCnfInvertedIndex().getTable().get(3).size());
        Assert.assertFalse(index.getCnfInvertedIndex().getTable().get(3).containsKey(zKey));
        final Key aKey = Key.builder().name("$.user_id").value("22").upperBoundScore(0).build();
        Assert.assertTrue(index.getCnfInvertedIndex().getTable().get(3).containsKey(aKey));
    }

}
