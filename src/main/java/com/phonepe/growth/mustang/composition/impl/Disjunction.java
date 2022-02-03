/**
 * Copyright (c) 2022 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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
package com.phonepe.growth.mustang.composition.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.composition.Composition;
import com.phonepe.growth.mustang.composition.CompositionType;
import com.phonepe.growth.mustang.debug.CompositionDebugResult;
import com.phonepe.growth.mustang.predicate.Predicate;
import com.phonepe.growth.mustang.predicate.PredicateType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Disjunction extends Composition {

    @Builder
    @JsonCreator
    public Disjunction(@JsonProperty("predicates") @Singular List<Predicate> predicates) {
        super(CompositionType.OR, predicates);
    }

    @Override
    public boolean evaluate(RequestContext context) {
        return getPredicates().stream()
                .anyMatch(predicate -> predicate.evaluate(context));
    }

    @Override
    public CompositionDebugResult debug(RequestContext context) {
        return CompositionDebugResult.builder()
                .result(evaluate(context))
                .type(this.getType())
                .predicateDebugResults(getPredicates().stream()
                        .map(predicate -> predicate.debug(context))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public double getScore(RequestContext context) {
        return getPredicates().stream()
                .filter(predicate -> PredicateType.INCLUDED.equals(predicate.getType()))
                .mapToDouble(predicate -> predicate.getWeight() * getWeigthFromContext(context, predicate))
                .max()
                .orElse(0);
    }

}
