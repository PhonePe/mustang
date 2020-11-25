package com.phonepe.growth.mustang.search.handler;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.exception.MustangException;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.search.Query;
import com.phonepe.growth.mustang.search.matcher.CNFMatcher;
import com.phonepe.growth.mustang.search.matcher.DNFMatcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriteriaSearchHandler implements CriteriaForm.Visitor<Future<Map<String, Double>>> {
    @NotNull
    private final IndexGroup indexGroup;
    @Valid
    @NotNull
    private final Query query;

    public Map<String, Double> handle() {
        return Stream.of(visitDNF(), visitCNF())
                .map(this::extract)
                .flatMap(map -> map.entrySet()
                        .stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1));
    }

    @Override
    public Future<Map<String, Double>> visitDNF() {
        return indexGroup.getService()
                .submit(() -> DNFMatcher.builder()
                        .invertedInex(indexGroup.getDnfInvertedIndex())
                        .query(query)
                        .allCriterias(indexGroup.getAllCriterias())
                        .build()
                        .getMatches());
    }

    @Override
    public Future<Map<String, Double>> visitCNF() {
        return indexGroup.getService()
                .submit(() -> CNFMatcher.builder()
                        .invertedIndex(indexGroup.getCnfInvertedIndex())
                        .query(query)
                        .allCriterias(indexGroup.getAllCriterias())
                        .build()
                        .getMatches());
    }

    private Map<String, Double> extract(Future<Map<String, Double>> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw MustangException.propagate(e);
        }
    }

}
