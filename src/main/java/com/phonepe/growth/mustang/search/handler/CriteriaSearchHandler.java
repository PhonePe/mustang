package com.phonepe.growth.mustang.search.handler;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.search.Query;
import com.phonepe.growth.mustang.search.matcher.CNFMatcher;
import com.phonepe.growth.mustang.search.matcher.DNFMatcher;
import com.phonepe.growth.mustang.search.matcher.Matches;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriteriaSearchHandler implements CriteriaForm.Visitor<Matches> {
    @NotNull
    private final IndexGroup indexGroup;
    @Valid
    @NotNull
    private final Query query;

    public Map<String, Double> handle() {
        return Stream.of(CriteriaForm.values())
                .map(cForm -> cForm.accept(this))
                .map(matches -> SearchDataExtractor.extract(matches.getProbables()))
                .flatMap(map -> map.entrySet()
                        .stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
    }

    @Override
    public Matches visitDNF() {
        return Matches.builder()
                .probables(indexGroup.getProcessor()
                        .submit(() -> DNFMatcher.builder()
                                .invertedInex(indexGroup.getDnfInvertedIndex())
                                .query(query)
                                .allCriterias(indexGroup.getAllCriterias())
                                .build()
                                .getMatches()))
                .build();
    }

    @Override
    public Matches visitCNF() {
        return Matches.builder()
                .probables(indexGroup.getProcessor()
                        .submit(() -> CNFMatcher.builder()
                                .invertedIndex(indexGroup.getCnfInvertedIndex())
                                .query(query)
                                .allCriterias(indexGroup.getAllCriterias())
                                .build()
                                .getMatches()))
                .build();
    }

}
