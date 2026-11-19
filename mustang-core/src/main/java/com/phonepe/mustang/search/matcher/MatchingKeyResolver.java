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
package com.phonepe.mustang.search.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.phonepe.mustang.index.core.EqualityKeyLookup;
import com.phonepe.mustang.index.core.InvertedIndex;
import com.phonepe.mustang.index.core.Key;
import com.phonepe.mustang.preoperation.ChainOperator;

import lombok.experimental.UtilityClass;

/**
 * Finds the keys of a posting-list map that are satisfied by the current request.
 *
 * <p>
 * {@code EQUALITY} keys are resolved through a hash lookup per {@code (path, preOperations)} group; only the remaining
 * keys are evaluated one by one.
 * </p>
 */
@UtilityClass
class MatchingKeyResolver {

    /** Test only: evaluate every key one by one, to check the lookup returns the same keys as a full scan. */
    static volatile boolean scanAllKeys = false;

    static List<Key> resolve(final InvertedIndex<?> invertedIndex,
            final int size,
            final Map<Key, ?> postingLists,
            final Map<String, Object> pathValues) {
        if (scanAllKeys) {
            return scan(postingLists.keySet(), pathValues);
        }
        final EqualityKeyLookup lookup = invertedIndex.getEqualityKeyLookup(size, postingLists);
        final List<Key> matchingKeys = scan(lookup.getScanKeys(), pathValues);
        lookup.getEqualityGroups()
                .forEach((group, buckets) -> {
                    final Object normalised = EqualityKeyLookup.normaliseRequestValue(
                            ChainOperator.operate(group.getPreOps(), pathValues.get(group.getName())));
                    if (Objects.isNull(normalised)) {
                        return;
                    }
                    final List<Key> keys = buckets.get(normalised);
                    if (Objects.nonNull(keys)) {
                        matchingKeys.addAll(keys);
                    }
                });
        return matchingKeys;
    }

    private static List<Key> scan(final Iterable<Key> keys, final Map<String, Object> pathValues) {
        final List<Key> matchingKeys = new ArrayList<>();
        keys.forEach(key -> {
            if (satisfies(key, ChainOperator.operate(key.getPreOps(), pathValues.get(key.getName())))) {
                matchingKeys.add(key);
            }
        });
        return matchingKeys;
    }

    private static boolean satisfies(final Key key, final Object lhsValue) {
        return key.getCaveat()
                .visit(new CaveatEnforcer(key, lhsValue));
    }
}
