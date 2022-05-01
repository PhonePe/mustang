/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.criteria.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.impl.Disjunction;
import com.phonepe.mustang.criteria.Criteria;
import com.phonepe.mustang.criteria.CriteriaForm;
import com.phonepe.mustang.criteria.CriteriaVisitor;
import com.phonepe.mustang.debug.DebugResult;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CNFCriteria extends Criteria {
    @Valid
    @NotEmpty
    private List<Disjunction> disjunctions;

    @Builder
    @JsonCreator
    public CNFCriteria(@JsonProperty("id") String id,
            @JsonProperty("disjunctions") @Singular List<Disjunction> disjunctions) {
        super(CriteriaForm.CNF, id);
        this.disjunctions = disjunctions;
    }

    @Override
    public boolean evaluate(RequestContext context) {
        return disjunctions.stream()
                .allMatch(disjunction -> disjunction.evaluate(context));
    }

    @Override
    public DebugResult debug(RequestContext context) {
        return DebugResult.builder()
                .result(evaluate(context))
                .id(this.getId())
                .form(this.getForm())
                .compositionDebugResults(disjunctions.stream()
                        .map(disjunction -> disjunction.debug(context))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public double getScore(RequestContext context) {
        // score of a CNF is the sum of score of all its constituent disjunctions.
        return disjunctions.stream()
                .mapToDouble(disjunction -> disjunction.getScore(context))
                .sum();
    }

    @Override
    public <T> T accept(CriteriaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
