package com.phonepe.growth.mustang.json;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
    private static final String NORMALISED_KEY_FORMAT = "$.%s"; // Normalization.
    private static final TypeReference<Map<String, Object>> TYPE_REF = new TypeReference<Map<String, Object>>() {
    };
    @NotNull
    private ObjectMapper mapper;
    @NotNull
    private JsonNode node;

    public Map<String, Object> flatten() {
        try {
            final Map<String, Object> collect = JsonFlattener.flattenAsMap(mapper.writeValueAsString(node)).entrySet()
                    .stream().collect(Collectors.toMap(e -> String.format(NORMALISED_KEY_FORMAT, e.getKey()),
                            Map.Entry::getValue));
            return mapper.readValue(mapper.writeValueAsBytes(collect), TYPE_REF); // for type safety
        } catch (JsonProcessingException e) {
            throw MustangException.propagate(e);
        } catch (IOException e) {
            throw MustangException.propagate(e);
        }
    }

}
