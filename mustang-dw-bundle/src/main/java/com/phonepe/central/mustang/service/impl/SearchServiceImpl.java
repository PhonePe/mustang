package com.phonepe.central.mustang.service.impl;

import java.util.Set;

import com.phonepe.central.mustang.request.SearchRequest;
import com.phonepe.central.mustang.service.SearchService;
import com.phonepe.growth.mustang.MustangEngine;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchServiceImpl implements SearchService {
    private final MustangEngine engine;

    public SearchServiceImpl(final MustangEngine enigne) {
        this.engine = enigne;
    }

    @Override
    public Set<String> search(final SearchRequest request) {
        if (request.isScore()) {
            return engine.search(request.getIndexName(), request.getRequestContext(), request.isScore());
        }
        return engine.search(request.getIndexName(), request.getRequestContext());
    }

}
