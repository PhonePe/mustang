package com.phonepe.central.mustang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.Test;

import com.phonepe.central.mustang.criteria.CriteriaForm;
import com.phonepe.central.mustang.detail.Caveat;
import com.phonepe.central.mustang.predicate.PredicateType;
import com.phonepe.central.mustang.preoperation.PreOperationType;
import com.phonepe.central.mustang.preoperation.impl.DateExtractionType;

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
