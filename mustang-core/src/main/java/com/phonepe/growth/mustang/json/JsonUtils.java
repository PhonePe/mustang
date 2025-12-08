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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.phonepe.growth.mustang.common.Utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    public static Object getNodeValue(final JsonNode node, final String path) {
        return getNodeValue(JsonPath.parse(node.toString(), Utils.JSONPATH_CONFIGURATION),
                JsonPath.compile(path),
                null);
    }

    public static JsonNode getJsonNodeFromAssignment(final ObjectMapper mapper, final Map<String, Object> assignment) {
        final JsonNode node = mapper.createObjectNode();
        assignment.entrySet()
                .stream()
                .forEach(
                        entry -> JsonUtils.merge(node, JsonUtils.createNode(mapper, entry.getKey(), entry.getValue())));
        return node;
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

    public static ObjectNode createNode(final ObjectMapper mapper, final String path, final Object value) {
        final ObjectNode child = mapper.createObjectNode();
        final String[] paths = path.substring(1)
                .split("\\.");
        if (paths.length == 0) {
            return child;
        }

        if (value instanceof JsonNode) {
            child.set(paths[paths.length - 1], (JsonNode) value);
        } else if (value instanceof Number) {
            if (Double.class.isAssignableFrom(value.getClass())) {
                child.put(paths[paths.length - 1], ((Number) value).doubleValue());
            } else {
                child.put(paths[paths.length - 1], ((Number) value).longValue());
            }
        } else if (value instanceof Boolean) {
            child.put(paths[paths.length - 1], Boolean.valueOf(value.toString()));
        } else {
            child.put(paths[paths.length - 1], value.toString());
        }

        return createNodeUtil(mapper, child, paths);
    }

    public static void merge(JsonNode primary, JsonNode backup) {
        Iterator<String> fieldNames = backup.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode primaryValue = primary.get(fieldName);
            if (primaryValue == null || !primaryValue.isObject()) {
                JsonNode backupValue = backup.get(fieldName)
                        .deepCopy();
                if (primary instanceof ArrayNode) {
                    ((ArrayNode) primary).set(0, backupValue);
                } else if (primary instanceof ObjectNode) {
                    ((ObjectNode) primary).set(fieldName, backupValue);
                }
            } else if (primaryValue.isObject()) {
                JsonNode backupValue = backup.get(fieldName);
                if (backupValue.isObject()) {
                    merge(primaryValue, backupValue.deepCopy());
                }
            }
        }
    }

    private static ObjectNode createNodeUtil(ObjectMapper mapper, ObjectNode child, String[] paths) {
        ObjectNode parent = null;
        for (int i = paths.length - 2; i > 0; i--) {
            final String currentPath = paths[i];
            if (currentPath.length() == 0) {
                continue;
            }
            parent = mapper.createObjectNode();
            parent.set(currentPath, child);
            child = parent;
        }
        if (parent == null) {
            parent = child;
        }
        return parent;
    }

}
