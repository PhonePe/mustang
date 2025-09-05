package com.phonepe.central.mustang.service;

import java.util.Set;

import com.phonepe.growth.mustang.common.RequestContext;

public interface SearchService {

    Set<String> search(String indexname, RequestContext context, boolean score);

    Set<String> search(String indexName, RequestContext context, int topN);

}
