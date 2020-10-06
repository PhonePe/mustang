package com.phonepe.growth.mustang.search.handler;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.group.IndexGroup;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriteriaSearchHandler implements CriteriaForm.Visitor<Set<String>> {
    @NotNull
    private final IndexGroup index;
    @Valid
    @NotNull
    private final Query query;

    public Set<String> handle() {
        return Stream.of(visitDNF(), visitCNF()).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<String> visitDNF() {
        return DNFMatcher.builder().invertedInex(index.getDnfInvertedIndex()).query(query).build().getMatches();
    }

    @Override
    public Set<String> visitCNF() {
        return CNFMatcher.builder().invertedIndex(index.getCnfInvertedIndex()).query(query).build().getMatches();
    }

}
