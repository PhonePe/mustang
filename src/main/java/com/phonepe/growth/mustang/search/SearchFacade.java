package com.phonepe.growth.mustang.search;

import java.util.List;

import com.phonepe.growth.mustang.index.IndexingFacade;
import com.phonepe.growth.mustang.search.handler.CriteriaSearchHandler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchFacade {

    public static List<String> search(final String indexName, final Query query) {
        return CriteriaSearchHandler.builder().index(IndexingFacade.get(indexName)).query(query).build().handle();
    }

}
