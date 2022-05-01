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
package com.phonepe.mustang.criteria;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.criteria.impl.CNFCriteria;
import com.phonepe.mustang.criteria.impl.DNFCriteria;
import com.phonepe.mustang.debug.DebugResult;

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
