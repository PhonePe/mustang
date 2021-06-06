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
package com.phonepe.growth.mustang.predicate;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.debug.PredicateDebugResult;
import com.phonepe.growth.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.growth.mustang.predicate.impl.IncludedPredicate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(name = PredicateType.INCLUDED_TEXT, value = IncludedPredicate.class),
        @JsonSubTypes.Type(name = PredicateType.EXCLUDED_TEXT, value = ExcludedPredicate.class), })
@JsonPropertyOrder({ "type", "lhs", "values", "lhsNotAPath", "weight", "defaultResult" })
public abstract class Predicate {
    @NotNull
    private PredicateType type;
    @NotBlank
    private String lhs;
    private boolean lhsNotAPath;
    private Long weight;
    private boolean defaultResult;

    public boolean evaluate(RequestContext context) {
        if (lhsNotAPath) {
            return evaluate(context, lhs);
        }
        try {
            return evaluate(context,
                    JsonPath.read(context.getNode()
                            .toString(), lhs));
        } catch (PathNotFoundException e) {
            return defaultResult;
        }
    }

    protected abstract boolean evaluate(RequestContext context, Object lhsValue);

    public abstract PredicateDebugResult debug(RequestContext context);

    public abstract <T> T accept(PredicateVisitor<T> visitor);

}
