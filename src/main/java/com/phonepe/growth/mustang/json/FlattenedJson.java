package com.phonepe.growth.mustang.json;

import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.phonepe.growth.mustang.exception.MustangException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlattenedJson {
    private static final String NORMALISED_KEY_FORMAT = "$.%s"; // Normalisation to conform to jwayjson path.
    @NotNull
    private ObjectMapper mapper;
    @NotNull
    private JsonNode node;

    public Map<String, Object> flatten() {
        try {
            return JsonFlattener.flattenAsMap(mapper.writeValueAsString(node)).entrySet().stream().collect(
                    Collectors.toMap(e -> String.format(NORMALISED_KEY_FORMAT, e.getKey()), Map.Entry::getValue));
        } catch (JsonProcessingException e) {
            throw MustangException.propagate(e);
        }
    }

}
