package com.phonepe.central.mustang.service.impl;

import java.util.List;
import java.util.Set;

import com.phonepe.central.mustang.service.ScanService;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;

public class ScanServiceImpl implements ScanService {

    private MustangEngine engine;

    public ScanServiceImpl(MustangEngine engine) {
        this.engine = engine;
    }

    @Override
    public Set<String> scan(String indexName, RequestContext context) {
        return engine.scan(indexName, context);
    }

    @Override
    public List<Criteria> scan(List<Criteria> criteria, RequestContext context) {
        return engine.scan(criteria, context);
    }

}
