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

    private void process(final JsonNode node, final String prefix1, final String prefix2) {
        if (node.isObject()) {
            final ObjectNode object = (ObjectNode) node;
            object.fields()
                    .forEachRemaining(field -> {
                        process(field.getValue(),
                                String.format(OBJECT_DOT_NOTATION_PREFIX_FORMAT, prefix1, field.getKey()),
                                String.format(OBJECT_BRACKET_NOTATION_PREFIX_FORMAT, prefix2, field.getKey()));
                    });
        } else if (node.isArray()) {
            final ArrayNode array = (ArrayNode) node;
            final AtomicInteger counter = new AtomicInteger();
            array.elements()
                    .forEachRemaining(element -> {
                        final int index = counter.getAndIncrement();
                        process(element,
                                String.format(ARRAY_PREFIX_FORMAT, prefix1, index),
                                String.format(ARRAY_PREFIX_FORMAT, prefix2, index));
                    });
        } else {
            map.put(prefix1, prefix2, node);
        }
    }

}
