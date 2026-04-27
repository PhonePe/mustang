/**
 * Copyright (c) 2022 PhonePe India Pvt. Ltd.
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
package com.phonepe.mustang.predicate;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jayway.jsonpath.JsonPath;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.common.Utils;
import com.phonepe.mustang.debug.PredicateDebugResult;
import com.phonepe.mustang.detail.Detail;
import com.phonepe.mustang.predicate.impl.ExcludedPredicate;
import com.phonepe.mustang.predicate.impl.IncludedPredicate;
import com.phonepe.mustang.preoperation.PreOperation;
import com.phonepe.mustang.preoperation.impl.IdentityOperation;

import io.dropwizard.validation.ValidationMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(name = PredicateType.INCLUDED_TEXT, value = IncludedPredicate.class),
        @JsonSubTypes.Type(name = PredicateType.EXCLUDED_TEXT, value = ExcludedPredicate.class), })
@JsonPropertyOrder({ "type", "lhs", "preOperation", "detail", "weight" })
public abstract class Predicate {

    public static final double NO_MATCH_SCORE = -1.0D;
    @NotNull
    private PredicateType type;
    @NotBlank
    private String lhs;
    private Long weight;

    public boolean evaluate(final RequestContext context) {
        return evaluate(Utils.getNodeValue(context.getNode(), lhs));
    }

    public PredicateDebugResult debug(final RequestContext context) {
        return PredicateDebugResult.builder()
                .result(evaluate(context))
                .type(type)
                .lhs(lhs)
                .lhsValue(Utils.getNodeValue(context.getNode(), lhs))
                .preOperations(getPreOperations())
                .detail(getDetail())
                .build();
    }

    @ValidationMethod(message = "lhs path is an invalid json path")
    @JsonIgnore
    public boolean isValidPredicate() {
        try {
            JsonPath.compile(lhs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected @Valid List<PreOperation> extractPreOperations(PreOperation preOperation,
            List<PreOperation> preOperations) {
        final List<PreOperation> fallback = List.of(Objects.nonNull(preOperation) ? preOperation
                : IdentityOperation.builder()
                        .build());
        return Objects.nonNull(preOperations) ? preOperations : fallback;
    }

    public abstract boolean evaluate(Object lhsValue);

    public abstract Detail getDetail();

    public abstract List<PreOperation> getPreOperations();

    public abstract <T> T accept(PredicateVisitor<T> visitor);

    public abstract long getScore(RequestContext context);

}
