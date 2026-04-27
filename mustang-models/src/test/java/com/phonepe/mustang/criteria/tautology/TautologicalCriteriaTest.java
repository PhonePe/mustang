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

package com.phonepe.mustang.criteria.tautology;

import com.phonepe.mustang.criteria.CriteriaForm;
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

