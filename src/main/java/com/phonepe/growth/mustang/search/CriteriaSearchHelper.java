package com.phonepe.growth.mustang.search;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.phonepe.growth.mustang.criteria.CriteriaForm;
import com.phonepe.growth.mustang.index.core.ConjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.index.core.IndexGroup;
import com.phonepe.growth.mustang.index.core.Key;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriteriaSearchHelper implements CriteriaForm.Visitor<List<String>> {
    @Valid
    @NotNull
    private final IndexGroup index;
    @NotEmpty
    private final Map<String, Object> assignment;

    @Override
    public List<String> visitDNF() {
        // TODO implement
        final Map<Integer, Map<Key, Set<ConjunctionPostingEntry>>> table = index.getDnfInvertedIndex().getTable();
        return Collections.emptyList();
    }

    @Override
    public List<String> visitCNF() {
        // TODO implement
        Map<Integer, Map<Key, Set<DisjunctionPostingEntry>>> table = index.getCnfInvertedIndex().getTable();
        return Collections.emptyList();
    }
}