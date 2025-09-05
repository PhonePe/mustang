package com.phonepe.central.mustang.service.impl;

import com.phonepe.central.mustang.service.DebugService;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.debug.DebugResult;
import com.phonepe.growth.mustang.ratify.RatificationResult;

public class DebugServiceImpl implements DebugService {
    private MustangEngine engine;

    public DebugServiceImpl(MustangEngine engine) {
        this.engine = engine;
    }

    @Override
    public DebugResult debug(Criteria criteria, RequestContext context) {
        return engine.debug(criteria, context);
    }

    @Override
    public String exportIndex(String indexName) {
        return engine.exportIndexGroup(indexName);
    }

    @Override
    public boolean importIndex(String indexName, String importedIndex) {
        engine.importIndexGroup(indexName, importedIndex);
        return true;
    }

    @Override
    public String snapshot(String indexName) {
        return engine.snapshot(indexName);
    }

    @Override
    public boolean ratify(String indexName) {
        engine.ratify(indexName);
        return true;
    }

    @Override
    public RatificationResult getRatificationResult(String indexName) {
        return engine.getRatificationResult(indexName);
    }

}
