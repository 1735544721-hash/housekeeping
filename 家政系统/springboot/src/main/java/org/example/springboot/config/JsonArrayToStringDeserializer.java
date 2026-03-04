package org.example.springboot.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonArrayToStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isArray()) {
            // 如果是数组，转换为JSON字符串
            return new ObjectMapper().writeValueAsString(node);
        } else if (node.isTextual()) {
            // 如果已经是字符串，直接返回
            return node.asText();
        }
        return null;
    }
} 