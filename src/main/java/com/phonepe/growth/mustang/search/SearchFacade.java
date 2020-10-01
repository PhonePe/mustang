package com.phonepe.growth.mustang.search;

import java.util.Set;

import com.phonepe.growth.mustang.index.IndexingFacade;
import com.phonepe.growth.mustang.search.handler.CriteriaSearchHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SearchFacade {
    private final IndexingFacade indexingFacade;

    public Set<String> search(final String indexName, final Query query) {
        return CriteriaSearchHandler.builder().index(indexingFacade.getIndexGroup(indexName)).query(query).build().handle();
    }

}
