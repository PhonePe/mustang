package com.phonepe.central.mustang.service.impl;

import java.util.List;
import java.util.Set;

import com.phonepe.central.mustang.request.ScanCriteriaRequest;
import com.phonepe.central.mustang.request.ScanIndexRequest;
import com.phonepe.central.mustang.service.ScanService;
import com.phonepe.growth.mustang.MustangEngine;
import com.phonepe.growth.mustang.criteria.Criteria;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanServiceImpl implements ScanService {

    private final MustangEngine engine;

    public ScanServiceImpl(final MustangEngine engine) {
        this.engine = engine;
    }

    @Override
    public Set<String> scan(final ScanIndexRequest request) {
        return engine.scan(request.getIndexName(), request.getRequestContext());
    }

    @Override
    public List<Criteria> scan(final ScanCriteriaRequest request) {
        return engine.scan(request.getCriterias(), request.getRequestContext());
    }

}
