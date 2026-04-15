package com.phonepe.central.mustang.criteria.tautology;

import com.phonepe.central.mustang.composition.CompositionType;
import com.phonepe.central.mustang.criteria.impl.UNFCriteria;
import java.util.Collections;

public class UNFTautologicalCriteria extends UNFCriteria {

    public UNFTautologicalCriteria(final String id) {
        super(id, CompositionType.AND, Collections.emptyList(), Collections.emptyList());
    }

}
