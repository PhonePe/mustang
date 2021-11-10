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
package com.phonepe.growth.mustang.search.matcher;

import static com.phonepe.growth.mustang.json.JsonUtils.getNodeValue;

import java.util.Objects;

import com.phonepe.growth.mustang.detail.Caveat;
import com.phonepe.growth.mustang.detail.impl.RangeDetail;
import com.phonepe.growth.mustang.index.core.Key;
import com.phonepe.growth.mustang.search.Query;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CaveatEnforcer implements Caveat.Visitor<Boolean> {
    private final Key key;
    private final Query query;

    @Override
    public Boolean visitEquality() {
        return key.getValue()
                .equals(getNodeValue(query.getParsedContext(), key.getCompiledPath(), null));
    }

    @Override
    public Boolean visitRegexMatch() {
        final Object value = getNodeValue(query.getParsedContext(), key.getCompiledPath(), null);
        if (Objects.nonNull(value) && String.class.isAssignableFrom(value.getClass())) {
            return value.toString()
                    .matches(String.valueOf(key.getValue()));
        }
        return false;
    }

    @Override
    public Boolean visitRange() {
        final Object value = getNodeValue(query.getParsedContext(), key.getCompiledPath(), null);
        boolean result = false;
        if (Objects.nonNull(value) && Number.class.isAssignableFrom(value.getClass())) {
            final double numericalValue = ((Number) value).doubleValue();
            final RangeDetail detail = RangeDetail.of(String.valueOf(key.getValue()));
            result = detail.isIncludeLowerBound() ? (detail.getLowerBound()
                    .doubleValue() <= numericalValue)
                    : (detail.getLowerBound()
                            .doubleValue() < numericalValue);
            result &= detail.isIncludeUpperBound() ? (numericalValue <= detail.getUpperBound()
                    .doubleValue())
                    : (numericalValue < detail.getUpperBound()
                            .doubleValue());
        }
        return result;
    }

}