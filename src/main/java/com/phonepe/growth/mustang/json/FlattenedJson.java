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
