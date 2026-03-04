package org.example.springboot.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class StringToJsonArraySerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            gen.writeNull();
            return;
        }

        try {
            // 尝试将JSON字符串解析为数组并写入
            ObjectMapper mapper = new ObjectMapper();
            Object array = mapper.readValue(value, Object.class);
            gen.writeObject(array);
        } catch (Exception e) {
            // 如果解析失败，写入原始字符串
            gen.writeString(value);
        }
    }
} 