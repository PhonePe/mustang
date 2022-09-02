/**
 * Copyright (c) 2022 Original Author(s), PhonePe India Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonepe.mustang.search.matcher;

import java.util.Objects;

import org.apache.maven.artifact.versioning.ComparableVersion;

import com.phonepe.mustang.detail.Caveat;
import com.phonepe.mustang.detail.impl.ComparisionInference;
import com.phonepe.mustang.detail.impl.RangeDetail;
import com.phonepe.mustang.detail.impl.VersioningDetail;
import com.phonepe.mustang.index.core.Key;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CaveatEnforcer implements Caveat.Visitor<Boolean> {
    private final Key key;
    private final Object value;

    @Override
    public Boolean visitNone() {
        return true;
    }

    @Override
    public Boolean visitEquality() {
        return key.getValue()
                .equals(value);
    }

    @Override
    public Boolean visitRegexMatch() {
        if (Objects.nonNull(value) && String.class.isAssignableFrom(value.getClass())) {
            return value.toString()
                    .matches(String.valueOf(key.getValue()));
        }
        return false;
    }

    @Override
    public Boolean visitRange() {
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

    @Override
    public Boolean visitVersioning() {
        if (Objects.nonNull(value) && String.class.isAssignableFrom(value.getClass())) {
            final VersioningDetail detail = VersioningDetail.of(String.valueOf(key.getValue()));
            final int comparisionResult = new ComparableVersion(detail.getBaseVersion())
                    .compareTo(new ComparableVersion(value.toString()));
            return detail.getCheck()
                    .accept(new ComparisionInference(comparisionResult, detail.isExcludeBase()));
        }
        return false;
    }

}