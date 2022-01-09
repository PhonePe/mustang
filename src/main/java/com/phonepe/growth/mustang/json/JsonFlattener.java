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
package com.phonepe.growth.mustang.json;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Builder;
import lombok.Value;

@Value
public class JsonFlattener {

    private static final String ROOT_PREFIX = "$";
    private static final String OBJECT_DOT_NOTATION_PREFIX_FORMAT = "%s.%s";
    private static final String OBJECT_BRACKET_NOTATION_PREFIX_FORMAT = "%s['%s']";
    private static final String ARRAY_PREFIX_FORMAT = "%s[%d]";
    private final MultiKeyMap<String, Object> map = MultiKeyMap.multiKeyMap(new LinkedMap<>());
    private final JsonNode node;

    @Builder
    public JsonFlattener(JsonNode node) {
        this.node = node;
    }

    public MultiKeyMap<String, Object> flatten() {
        process(node, ROOT_PREFIX, ROOT_PREFIX);
        return map;
    }

    private void process(final JsonNode node, final String dotNotation, final String bracketNotation) {
        if (node.isObject()) {
            final ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields()
                    .forEachRemaining(field -> {
                        process(field.getValue(),
                                String.format(OBJECT_DOT_NOTATION_PREFIX_FORMAT, dotNotation, field.getKey()),
                                String.format(OBJECT_BRACKET_NOTATION_PREFIX_FORMAT, bracketNotation, field.getKey()));
                    });
        } else if (node.isArray()) {
            final ArrayNode arrayNode = (ArrayNode) node;
            final AtomicInteger counter = new AtomicInteger();
            arrayNode.elements()
                    .forEachRemaining(element -> {
                        final int index = counter.getAndIncrement();
                        process(element,
                                String.format(ARRAY_PREFIX_FORMAT, dotNotation, index),
                                String.format(ARRAY_PREFIX_FORMAT, bracketNotation, index));
                    });
        } else {
            map.put(dotNotation, bracketNotation, node);
        }
    }

}
