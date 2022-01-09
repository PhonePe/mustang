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

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    public static Object getNodeValue(final JsonNode node, final String path) {
        return getNodeValue(JsonPath.parse(node.toString()), JsonPath.compile(path), null);
    }

    public static Object getNodeValue(final DocumentContext documentContext,
            final JsonPath jsonPath,
            final Object defaultValue) {
        Object returnValue = defaultValue;
        try {
            final Object nodeValue = documentContext.read(jsonPath);
            if (Objects.nonNull(nodeValue)) {
                if (List.class.isAssignableFrom(nodeValue.getClass())) {
                    final List<?> nodeListValue = (List<?>) nodeValue;
                    if (!nodeListValue.isEmpty()) {
                        returnValue = nodeListValue.get(0);
                    }
                } else {
                    returnValue = nodeValue;
                }
            }
        } catch (PathNotFoundException e) {
            // consume silently
        }
        return returnValue;
    }

}
