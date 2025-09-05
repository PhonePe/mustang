package com.phonepe.growth.mustang.criteria.tautology;

import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.criteria.impl.UNFCriteria;
import java.util.Collections;

public class UNFTautologicalCriteria extends UNFCriteria {

    public UNFTautologicalCriteria(final String id) {
        super(id, CompositionType.AND, Collections.emptyList(), Collections.emptyList());
    }

}
