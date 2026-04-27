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

package com.phonepe.mustang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.Test;

import com.phonepe.mustang.criteria.CriteriaForm;
import com.phonepe.mustang.detail.Caveat;
import com.phonepe.mustang.predicate.PredicateType;
import com.phonepe.mustang.preoperation.PreOperationType;
import com.phonepe.mustang.preoperation.impl.DateExtractionType;

public class EnumsTest {

    @Test
    public void testEnumCriteriaForm() {
        Arrays.asList(CriteriaForm.values())
                .stream()
                .forEach(x -> assertThat(CriteriaForm.valueOf(x.getValue()), is(x)));
    }

    @Test
    public void testEnumPredicateType() {
        Arrays.asList(PredicateType.values())
                .stream()
                .forEach(x -> assertThat(PredicateType.valueOf(x.getValue()), is(x)));
    }

    @Test
    public void testEnumCaveat() {
        Arrays.asList(Caveat.values())
                .stream()
                .forEach(x -> assertThat(Caveat.valueOf(x.getValue()), is(x)));
    }

    @Test
    public void testEnumPreOperationType() {
        Arrays.asList(PreOperationType.values())
                .stream()
                .forEach(x -> assertThat(PreOperationType.valueOf(x.getValue()), is(x)));
    }

    @Test
    public void testEnumDateExtracts() {
        Arrays.asList(DateExtractionType.values())
                .stream()
                .forEach(x -> assertThat(DateExtractionType.valueOf(x.getValue()), is(x)));
    }

}
