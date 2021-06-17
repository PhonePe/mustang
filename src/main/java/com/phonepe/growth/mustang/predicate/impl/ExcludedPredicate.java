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

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
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
public class ExcludedPredicate extends Predicate {
    @NotEmpty
    private Set<Object> values;

    @Builder
    @JsonCreator
    public ExcludedPredicate(@JsonProperty("lhs") String lhs,
            @JsonProperty("lhsNotAPath") boolean lhsNotAPath,
            @JsonProperty("weight") Long weight,
            @JsonProperty("values") Set<Object> values) {
        super(PredicateType.EXCLUDED, lhs, lhsNotAPath, Objects.isNull(weight) ? 1 : weight, Boolean.TRUE);
        this.values = values;
    }

    @Override
    public boolean evaluate(final RequestContext context, final Object lhsValue) {
        return !values.contains(lhsValue);
    }

    @Override
    public <T> T accept(PredicateVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
