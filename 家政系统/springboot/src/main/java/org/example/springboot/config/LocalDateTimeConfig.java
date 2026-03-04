package org.example.springboot.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Configuration
public class LocalDateTimeConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDateTimeConfig.class);
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 创建自定义的JavaTimeModule来处理时间序列化和反序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 序列化：使用统一的标准格式
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        
        // 反序列化：支持多种格式
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(null) {
            @Override
            public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser p, 
                    com.fasterxml.jackson.databind.DeserializationContext ctxt) throws java.io.IOException {
                String dateString = p.getText().trim();
                LOGGER.debug("尝试解析日期时间字符串: {}", dateString);
                
                // 尝试多种可能的格式
                String[] patterns = {
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "yyyy/MM/dd HH:mm:ss",
                    "yyyy/MM/dd HH:mm",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                };
                
                // 逐个尝试不同的格式
                for (String pattern : patterns) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                        return LocalDateTime.parse(dateString, formatter);
                    } catch (DateTimeParseException e) {
                        // 继续尝试下一种格式
                        LOGGER.trace("使用格式 {} 解析失败", pattern);
                    }
                }
                
                // 如果以上都失败，尝试ISO标准格式
                try {
                    return LocalDateTime.parse(dateString);
                } catch (DateTimeParseException e) {
                    LOGGER.warn("无法解析日期时间字符串: {}", dateString);
                    throw new DateTimeParseException(
                            "无法解析日期时间，支持的格式有: yyyy-MM-dd HH:mm:ss, yyyy-MM-dd'T'HH:mm:ss等", 
                            dateString, 0, e);
                }
            }
        });
        
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }
} 