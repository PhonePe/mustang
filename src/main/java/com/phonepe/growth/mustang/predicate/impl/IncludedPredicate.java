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
package com.phonepe.growth.mustang.predicate.impl;

import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.detail.Detail;
import com.phonepe.growth.mustang.detail.impl.EqualityDetail;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.PredicateType;
import com.phonepe.growth.mustang.predicate.PredicateVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IncludedPredicate extends Predicate {
    @Valid
    @NotNull
    private Detail detail;

    @Builder
    @JsonCreator
    public IncludedPredicate(@JsonProperty("lhs") String lhs,
            @JsonProperty("lhsNotAPath") boolean lhsNotAPath,
            @JsonProperty("weight") Long weight,
            @JsonProperty("detail") Detail detail,
            @JsonProperty(access = Access.WRITE_ONLY, value = "values") Set<Object> values) {
        super(PredicateType.INCLUDED, lhs, lhsNotAPath, Objects.isNull(weight) ? 1 : weight, Boolean.FALSE);
        this.detail = Objects.nonNull(detail) ? detail
                : EqualityDetail.builder()
                        .values(values)
                        .build();
    }

    @Override
    public boolean evaluate(final RequestContext context, final Object lhsValue) {
        return detail.validate(context, lhsValue);
    }

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
