package com.phonepe.growth.mustang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.phonepe.growth.mustang.json.JsonUtils;

public class Test2 {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String path = "$.paymentSources[?(@.type == 'CREDIT_CARD')].cardIssuer";
        JsonPath jsonPath = JsonPath.compile(path);
        String queryString = "{\"paymentSources\":[{\"type\":\"CREDIT_CARD\",\"cardIssuer\":\"AMEX\"}]}";
        JsonNode queryNode = mapper.valueToTree(queryString);
        // JsonNode queryNode = mapper.readValue(queryString, JsonNode.class);
        DocumentContext queryContext = JsonPath.parse(queryNode.toString());
        System.out.println(JsonUtils.getNodeValue(queryContext, jsonPath, null));

        Object res = JsonPath.read(queryNode.toString(), "$.paymentSources[?(@.type == 'CREDIT_CARD')].cardIssuer");
        System.out.println(res);

    }

}
