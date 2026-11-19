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
 */
package com.phonepe.mustang.index.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.phonepe.mustang.detail.Caveat;
import com.phonepe.mustang.preoperation.PreOperation;

import lombok.Getter;
import lombok.Value;

/**
 * Read-only lookup over the keys of one posting-list map (one entry of {@code InvertedIndex#getTable()}).
 *
 * <p>
 * {@link Caveat#EQUALITY} keys whose value has well defined equality semantics (strings, booleans and standard
 * numbers) are bucketed by {@code (path, preOperations)} and then by a normalised value, so that the keys
 * matching a request can be found with a hash lookup instead of evaluating every key. All other keys (other caveats,
 * or equality keys with any other value type) are kept in {@code getScanKeys()} and must be evaluated one by one,
 * exactly as before.
 * </p>
 *
 * <p>
 * Normalisation mirrors {@code Utils.compare}: numbers are compared by {@code doubleValue()}, booleans only match
 * booleans, strings only match strings, and a list request value is compared using its first element only. A request
 * value that cannot match any hashed key (missing, empty list, non-list collection, other types) matches nothing.
 * </p>
 *
 * <p>
 * The lookup is a pure function of the source map. The source map is never mutated by the indexers (they always
 * install a new map instance), so a lookup stays valid as long as {@code getSource()} is the current map.
 * </p>
 */
@Getter
public final class EqualityKeyLookup {

    private final Map<Key, ?> source;
    private final Map<KeyGroup, Map<Object, List<Key>>> equalityGroups;
    private final List<Key> scanKeys;

    private EqualityKeyLookup(final Map<Key, ?> source,
            final Map<KeyGroup, Map<Object, List<Key>>> equalityGroups,
            final List<Key> scanKeys) {
        this.source = source;
        this.equalityGroups = equalityGroups;
        this.scanKeys = scanKeys;
    }

    public static EqualityKeyLookup of(final Map<Key, ?> source) {
        final Map<KeyGroup, Map<Object, List<Key>>> groups = new HashMap<>();
        final List<Key> scan = new ArrayList<>();
        source.keySet()
                .forEach(key -> {
                    final Object normalisedValue = Caveat.EQUALITY.equals(key.getCaveat())
                            ? normaliseKeyValue(key.getValue())
                            : null;
                    if (Objects.isNull(normalisedValue)) {
                        scan.add(key);
                        return;
                    }
                    groups.computeIfAbsent(new KeyGroup(key.getName(), key.getPreOps()), x -> new HashMap<>())
                            .computeIfAbsent(normalisedValue, x -> new ArrayList<>(1))
                            .add(key);
                });
        return new EqualityKeyLookup(source, Collections.unmodifiableMap(groups), Collections.unmodifiableList(scan));
    }

    /**
     * @return the normalised hash value of an equality key, or {@code null} if the key must be scanned.
     */
    public static Object normaliseKeyValue(final Object value) {
        if (value instanceof String || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Number number && isStandardNumber(number)) {
            return normaliseNumber(number);
        }
        return null;
    }

    /**
     * Normalises a (pre-operated) request value the same way {@code Utils.compare} would compare it.
     *
     * @return the hash value to look up, or {@code null} if no hashed key can match.
     */
    public static Object normaliseRequestValue(final Object lhsValue) {
        Object value = lhsValue;
        if (value instanceof List<?> list) {
            value = list.isEmpty() ? null : list.get(0);
        }
        if (value instanceof String || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Number number) {
            return normaliseNumber(number);
        }
        // Hashed keys are strings, booleans or numbers; none of them equal any other type.
        return null;
    }

    private static Object normaliseNumber(final Number number) {
        final double value = number.doubleValue();
        if (Double.isNaN(value)) {
            return null; // NaN never equals anything under doubleValue() comparison
        }
        return value == 0.0d ? 0.0d : value; // fold -0.0 into 0.0, as == does
    }

    private static boolean isStandardNumber(final Number value) {
        return value instanceof Integer || value instanceof Long || value instanceof Double
                || value instanceof Float || value instanceof Short || value instanceof Byte
                || value instanceof BigDecimal || value instanceof BigInteger;
    }

    @Value
    public static class KeyGroup {
        String name;
        List<PreOperation> preOps;
    }
}
