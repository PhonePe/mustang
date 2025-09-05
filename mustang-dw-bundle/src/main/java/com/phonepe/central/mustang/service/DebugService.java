package com.phonepe.central.mustang.service;

import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.debug.DebugResult;
import com.phonepe.growth.mustang.ratify.RatificationResult;

public interface DebugService {

    DebugResult debug(Criteria criteria, RequestContext context);

    String exportIndex(String indexName);

    boolean importIndex(String indexName, String importedIndex);

    String snapshot(String indexName);

    boolean ratify(String indexName);

    RatificationResult getRatificationResult(String indexName);

}
