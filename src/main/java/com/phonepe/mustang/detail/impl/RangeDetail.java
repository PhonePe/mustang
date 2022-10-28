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
package com.phonepe.mustang.detail.impl;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.mustang.common.RequestContext;
import com.phonepe.mustang.detail.Caveat;
import com.phonepe.mustang.detail.Detail;
import com.phonepe.mustang.detail.DetailVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RangeDetail extends Detail {
    private static final String NORMALISED_FORMAT = "%s#%s#%s#%s";
    private static final String NORMALISED_FORMAT_SEPARATOR = "#";
    private Number lowerBound;
    private Number upperBound;
    private boolean includeLowerBound;
    private boolean includeUpperBound;

    @Builder
    @JsonCreator
    public RangeDetail(@JsonProperty("lowerBound") Number lowerBound,
            @JsonProperty("upperBound") Number upperBound,
            @JsonProperty("includeLowerBound") boolean includeLowerBound,
            @JsonProperty("includeUpperBound") boolean includeUpperBound) {
        super(Caveat.RANGE);
        this.lowerBound = Objects.nonNull(lowerBound) ? lowerBound : Double.MIN_VALUE;
        this.upperBound = Objects.nonNull(upperBound) ? upperBound : Double.MAX_VALUE;
        this.includeLowerBound = includeLowerBound;
        this.includeUpperBound = includeUpperBound;
    }

    @Override
    public boolean validate(RequestContext context, Object lhsValue) {
        boolean result = false;
        if (Number.class.isAssignableFrom(lhsValue.getClass())) {
            final double numericalValue = ((Number) lhsValue).doubleValue();
            result = includeLowerBound ? (lowerBound.doubleValue() <= numericalValue)
                    : (lowerBound.doubleValue() < numericalValue);
            result &= includeUpperBound ? (numericalValue <= upperBound.doubleValue())
                    : (numericalValue < upperBound.doubleValue());
        }
        return result;
    }

    @Override
    public <T> T accept(DetailVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getNormalisedView() {
        return String.format(NORMALISED_FORMAT, lowerBound, upperBound, includeLowerBound, includeUpperBound);
    }

    public static RangeDetail of(final String normalisedView) {
        final String[] parts = normalisedView.split(NORMALISED_FORMAT_SEPARATOR);
        return RangeDetail.builder()
                .lowerBound(Double.valueOf(parts[0]))
                .upperBound(Double.valueOf(parts[1]))
                .includeLowerBound(Boolean.valueOf(parts[2]))
                .includeUpperBound(Boolean.valueOf(parts[3]))
                .build();

    }

}
