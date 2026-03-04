package org.example.springboot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象转换为JSON字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error("转换JSON失败", e);
            throw new RuntimeException("转换JSON失败", e);
        }
    }

    /**
     * 将JSON字符串转换为对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error("解析JSON失败", e);
            throw new RuntimeException("解析JSON失败", e);
        }
    }

    /**
     * 将JSON字符串转换为对象列表
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            LOGGER.error("解析JSON数组失败", e);
            throw new RuntimeException("解析JSON数组失败", e);
        }
    }

    /**
     * 将JSON字符串转换为复杂类型对象
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            LOGGER.error("解析复杂JSON失败", e);
            throw new RuntimeException("解析复杂JSON失败", e);
        }
    }
} 