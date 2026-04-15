package com.phonepe.central.mustang.criteria.tautology;

import com.phonepe.central.mustang.criteria.CriteriaForm;
import org.junit.Assert;
import org.junit.Test;

public class TautologicalCriteriaTest {

    @Test
    public void testDNFTautologicalCriteria() {
        DNFTautologicalCriteria criteria = new DNFTautologicalCriteria("T1");
        Assert.assertEquals("T1", criteria.getId());
        Assert.assertEquals(CriteriaForm.DNF, criteria.getForm());
        Assert.assertNotNull(criteria.getConjunctions());
        Assert.assertTrue(criteria.getConjunctions().isEmpty());
    }

    @Test
    public void testCNFTautologicalCriteria() {
        CNFTautologicalCriteria criteria = new CNFTautologicalCriteria("T2");
        Assert.assertEquals("T2", criteria.getId());
        Assert.assertEquals(CriteriaForm.CNF, criteria.getForm());
        Assert.assertNotNull(criteria.getDisjunctions());
        Assert.assertTrue(criteria.getDisjunctions().isEmpty());
    }

    @Test
    public void testUNFTautologicalCriteria() {
        UNFTautologicalCriteria criteria = new UNFTautologicalCriteria("T3");
        Assert.assertEquals("T3", criteria.getId());
        Assert.assertEquals(CriteriaForm.UNF, criteria.getForm());
    }

    @Test
    public void testTautologicalCriteriaEquality() {
        DNFTautologicalCriteria c1 = new DNFTautologicalCriteria("T1");
        DNFTautologicalCriteria c2 = new DNFTautologicalCriteria("T1");
        Assert.assertEquals(c1, c2);
    }

    @Test
    public void testTautologicalCriteriaInequality() {
        DNFTautologicalCriteria c1 = new DNFTautologicalCriteria("T1");
        DNFTautologicalCriteria c2 = new DNFTautologicalCriteria("T2");
        Assert.assertNotEquals(c1, c2);
    }
}

