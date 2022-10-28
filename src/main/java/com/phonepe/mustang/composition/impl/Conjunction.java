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
 */
package com.phonepe.mustang.composition.impl;

import static com.phonepe.mustang.predicate.Predicate.NO_MATCH_SCORE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.composition.Composition;
import com.phonepe.mustang.composition.CompositionType;
import com.phonepe.mustang.debug.CompositionDebugResult;
import com.phonepe.mustang.predicate.Predicate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Conjunction extends Composition {

    @Builder
    @JsonCreator
    public Conjunction(@JsonProperty("predicates") @Singular List<Predicate> predicates) {
        super(CompositionType.AND, predicates);
    }

    @Override
    public boolean evaluate(final RequestContext context) {
        return getPredicates().stream()
                .allMatch(predicate -> predicate.evaluate(context));
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
        double sum = 0.0;
        for (Predicate predicate : getPredicates()) {
            final double score = predicate.getWeightFromContext(context);
            if (score == NO_MATCH_SCORE) {
                return NO_MATCH_SCORE;
            }
            sum += score;
        }
        return sum;
    }

}
