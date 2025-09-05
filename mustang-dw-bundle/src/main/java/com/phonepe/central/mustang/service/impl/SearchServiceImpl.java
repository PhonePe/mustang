package com.phonepe.central.mustang.service.impl;

import java.util.Set;

import com.phonepe.central.mustang.service.SearchService;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;

public class SearchServiceImpl implements SearchService {
    private MustangEngine engine;

    public SearchServiceImpl(MustangEngine enigne) {
        this.engine = enigne;
    }

    @Override
    public Set<String> search(String indexname, RequestContext context, boolean score) {
        return engine.search(indexname, context, score);
    }

    @Override
    public Set<String> search(String indexName, RequestContext context, int topN) {
        return engine.search(indexName, context, topN);
    }

}
