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
package com.phonepe.central.mustang.search.matcher;

import java.util.Objects;

import com.phonepe.central.mustang.common.Utils;
import org.apache.maven.artifact.versioning.ComparableVersion;

import com.phonepe.central.mustang.detail.Caveat;
import com.phonepe.central.mustang.detail.impl.ComparisionInference;
import com.phonepe.central.mustang.detail.impl.RangeDetail;
import com.phonepe.central.mustang.detail.impl.VersioningDetail;
import com.phonepe.central.mustang.index.core.Key;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CaveatEnforcer implements Caveat.Visitor<Boolean> {
    private final Key key;
    private final Object lhsValue;

    @Override
    public Boolean visitNone() {
        return true;
    }

    @Override
    public Boolean visitExistence() {
        return Utils.checkExistence(lhsValue);
    }

    @Override
    public Boolean visitNonExistence() {
        return !Utils.checkExistence(lhsValue);
    }

    @Override
    public Boolean visitEquality() {
        return Utils.compare(lhsValue, key.getValue());
    }

    @Override
    public Boolean visitSubSet() {
        return Utils.isSubSet(lhsValue, key.getValue());
    }

    @Override
    public Boolean visitEqualSet() {
        return Utils.areEqualSets(lhsValue, key.getValue());
    }

    @Override
    public Boolean visitSuperSet() {
        return Utils.isSuperSet(lhsValue, key.getValue());
    }

    @Override
    public Boolean visitRegexMatch() {
        if (Objects.nonNull(lhsValue) && String.class.isAssignableFrom(lhsValue.getClass())) {
            return lhsValue.toString()
                    .matches(String.valueOf(key.getValue()));
        }
        return false;
    }

    @Override
    public Boolean visitRange() {
        boolean result = false;
        if (Objects.nonNull(lhsValue) && Number.class.isAssignableFrom(lhsValue.getClass())) {
            final double numericalValue = ((Number) lhsValue).doubleValue();
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
        if (Objects.nonNull(lhsValue) && String.class.isAssignableFrom(lhsValue.getClass())) {
            final VersioningDetail detail = VersioningDetail.of(String.valueOf(key.getValue()));
            final int comparisionResult = new ComparableVersion(detail.getBaseVersion())
                    .compareTo(new ComparableVersion(lhsValue.toString()));
            return detail.getCheck()
                    .accept(new ComparisionInference(comparisionResult, detail.isExcludeBase()));
        }
        return false;
    }

}