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
package com.phonepe.mustang.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.phonepe.mustang.exception.MustangException;
import com.phonepe.mustang.preoperation.PreOperation;
import com.phonepe.mustang.preoperation.impl.IdentityOperation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static final Set<Object> MARKER = Collections.singleton("-");

    public static final List<PreOperation> DEFAULT_PREOPERATION = List.of(IdentityOperation.builder()
            .build());

    public static final Configuration JSONPATH_CONFIGURATION = Configuration.defaultConfiguration()
            .addOptions(Option.SUPPRESS_EXCEPTIONS);

    public static boolean checkExistence(Object value) {
        return Objects.nonNull(value);
    }

    public Long getRationalWeight(final Long weight) {
        if (Objects.isNull(weight) || weight == 0) {
            return 1L;
        }
        return weight;
    }

    public static <T> T executeSecurely(final ReentrantReadWriteLock.WriteLock lock, SecureExecution<T> block) {
        try {
            lock.lock();
            return block.execute();
        } catch (Exception exception) {
            throw MustangException.propagate(exception);
        } finally {
            lock.unlock();
        }
    }

    public static boolean compare(final Object lhsValue, final Object rhsValue) {
        if (Objects.nonNull(lhsValue)) {
            if (isCollection(lhsValue)) {
                return compareNatively(((List<?>) lhsValue).get(0), rhsValue);
            }
            return compareNatively(lhsValue, rhsValue);
        }
        return rhsValue.equals(lhsValue);
    }

    private static boolean compareNatively(final Object lhsValue, final Object rhsValue) {
        if (isNumber(lhsValue) && isNumber(rhsValue)) {
            return ((Number) lhsValue).doubleValue() == ((Number) rhsValue).doubleValue();
        } else if (isBoolean(lhsValue) && isBoolean(rhsValue)) {
            return ((Boolean) lhsValue).booleanValue() == ((Boolean) rhsValue).booleanValue();
        }
        return rhsValue.equals(lhsValue);

    }

    public static boolean isSubSet(Object lhsValue, Object rhsValue) {
        if (isCollection(lhsValue)) {
            return ((Set<?>) rhsValue).containsAll((Collection<?>) lhsValue);
        }
        return false;
    }

    public static boolean areEqualSets(Object lhsValue, Object rhsValue) {
        if (isCollection(lhsValue)) {
            return ((Set<?>) rhsValue).containsAll((Collection<?>) lhsValue)
                    && ((Collection<?>) lhsValue).containsAll((Set<?>) rhsValue);
        }
        return false;
    }

    public static boolean isSuperSet(Object lhsValue, Object rhsValue) {
        if (isCollection(lhsValue)) {
            return ((Collection<?>) lhsValue).containsAll((Set<?>) rhsValue);
        }
        return false;
    }

    public static Object getNodeValue(final JsonNode node, final String path) {
        return getNodeValue(JsonPath.parse(node.toString(), Utils.JSONPATH_CONFIGURATION), JsonPath.compile(path), null);
    }

    public static Object getNodeValue(final DocumentContext documentContext,
                                      final JsonPath jsonPath,
                                      final Object defaultValue) {
        Object returnValue = defaultValue;
        final Object nodeValue = documentContext.read(jsonPath);
        if (Objects.nonNull(nodeValue)) {
            if (List.class.isAssignableFrom(nodeValue.getClass())) {
                final List<?> nodeListValue = (List<?>) nodeValue;
                if (!nodeListValue.isEmpty()) {
                    returnValue = nodeListValue;
                }
            } else {
                returnValue = nodeValue;
            }
        }
        return returnValue;
    }

    private static boolean isCollection(Object value) {
        return Objects.nonNull(value) && Collection.class.isAssignableFrom(value.getClass());
    }

    private static boolean isBoolean(Object value) {
        return Boolean.class.isAssignableFrom(value.getClass());
    }

    private static boolean isNumber(Object value) {
        return Number.class.isAssignableFrom(value.getClass());
    }

    @UtilityClass
    public static class Constants {
        public static final String ERA_AD = "AD";
        public static final String ERA_BC = "BC";

        public static final String AM = "AM";
        public static final String PM = "PM";
    }

}
