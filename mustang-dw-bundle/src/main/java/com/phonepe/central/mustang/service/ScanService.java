package com.phonepe.central.mustang.service;

import java.util.List;
import java.util.Set;

import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;

public interface ScanService {

    Set<String> scan(String indexName, RequestContext context);

    List<Criteria> scan(List<Criteria> criteria, RequestContext context);

}
