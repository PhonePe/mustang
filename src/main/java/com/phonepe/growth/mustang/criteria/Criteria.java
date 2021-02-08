/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.criteria;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.phonepe.growth.mustang.debug.DebugResult;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.criteria.impl.CNFCriteria;
import com.phonepe.growth.mustang.criteria.impl.DNFCriteria;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "form")
@JsonSubTypes({ @JsonSubTypes.Type(name = CriteriaForm.DNF_TEXT, value = DNFCriteria.class),
        @JsonSubTypes.Type(name = CriteriaForm.CNF_TEXT, value = CNFCriteria.class) })
@JsonPropertyOrder({ "form", "id" })
public abstract class Criteria {
    @NotNull
    private CriteriaForm form;
    @NotBlank
    private String id;

    public abstract boolean evaluate(RequestContext context);

    public abstract DebugResult debug(RequestContext context);

    public abstract double getScore(RequestContext context);

    public abstract <T> T accept(CriteriaVisitor<T> visitor);

}
