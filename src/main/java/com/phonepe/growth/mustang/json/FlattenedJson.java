/**
 * Copyright (c) 2021 Mohammed Irfanulla S <mohammed.irfanulla.s1@gmail.com>
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

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.phonepe.growth.mustang.exception.MustangException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FlattenedJson {
    private static final String NORMALISED_KEY_FORMAT = "$.%s"; // Normalization.
    private static final TypeReference<Map<String, Object>> TYPE_REF = new TypeReference<Map<String, Object>>() {
    };
    @NotNull
    private final ObjectMapper mapper;
    @NotNull
    private final JsonNode node;

    public Map<String, Object> flatten() {
        try {
            final Map<String, Object> flattenedJson = JsonFlattener.flattenAsMap(mapper.writeValueAsString(node))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> String.format(NORMALISED_KEY_FORMAT, e.getKey()),
                            Map.Entry::getValue));
            return mapper.readValue(mapper.writeValueAsBytes(flattenedJson), TYPE_REF); // for type safety
        } catch (IOException e) {
            throw MustangException.propagate(e);
        }
    }

}
