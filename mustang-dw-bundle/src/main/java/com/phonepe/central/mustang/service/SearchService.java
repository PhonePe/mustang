package com.phonepe.central.mustang.service;

import java.util.Set;

import com.phonepe.central.mustang.request.SearchRequest;

public interface SearchService {

    Set<String> search(final SearchRequest request);

}
