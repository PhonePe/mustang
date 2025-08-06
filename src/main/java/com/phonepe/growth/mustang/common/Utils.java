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
package com.phonepe.growth.mustang.common;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.phonepe.growth.mustang.preoperation.PreOperation;
import com.phonepe.growth.mustang.preoperation.impl.IdentityOperation;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static final PreOperation DEFAULT_PREOPERATION = IdentityOperation.builder()
            .build();

    public static final Configuration JSONPATH_CONFIGURATION = Configuration.defaultConfiguration()
            .addOptions(Option.SUPPRESS_EXCEPTIONS);

    public Long getRationalWeight(final Long weight) {
        if (Objects.isNull(weight) || weight == 0) {
            return 1L;
        }
        return weight;
    }

    public static boolean compare(final Object lhsValue, final Object rhsValue) {
        if (Objects.nonNull(lhsValue)) {
            if (List.class.isAssignableFrom(lhsValue.getClass())) {
                return compareNatively(((List<?>) lhsValue).get(0), rhsValue);
            }
            return compareNatively(lhsValue, rhsValue);
        }
        return rhsValue.equals(lhsValue);
    }

    private static boolean compareNatively(final Object lhsValue, final Object rhsValue) {
        if (Number.class.isAssignableFrom(lhsValue.getClass()) && Number.class.isAssignableFrom(rhsValue.getClass())) {
            return ((Number) lhsValue).doubleValue() == ((Number) rhsValue).doubleValue();
        } else if (Boolean.class.isAssignableFrom(lhsValue.getClass())
                && Boolean.class.isAssignableFrom(rhsValue.getClass())) {
            return ((Boolean) lhsValue).booleanValue() == ((Boolean) rhsValue).booleanValue();
        }
        return rhsValue.equals(lhsValue);

    }

    public static boolean isSubSet(Object lhsValue, Object rhsValue) {
        if (Objects.nonNull(lhsValue) && List.class.isAssignableFrom(lhsValue.getClass())) {
            return ((Set<?>) rhsValue).containsAll((List<?>) lhsValue);
        }
        return false;
    }

    public static boolean areEqualSets(Object lhsValue, Object rhsValue) {
        if (Objects.nonNull(lhsValue) && List.class.isAssignableFrom(lhsValue.getClass())) {
            return ((Set<?>) rhsValue).containsAll((List<?>) lhsValue)
                    && ((List<?>) lhsValue).containsAll((Set<?>) rhsValue);
        }
        return false;
    }

    public static boolean isSuperSet(Object lhsValue, Object rhsValue) {
        if (Objects.nonNull(lhsValue) && List.class.isAssignableFrom(lhsValue.getClass())) {
            return ((List<?>) lhsValue).containsAll((Set<?>) rhsValue);
        }
        return false;
    }

}
