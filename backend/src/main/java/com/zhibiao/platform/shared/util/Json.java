package com.zhibiao.platform.shared.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 共享 Jackson 配置：UTC、ISO-8601、时间戳禁用。
 */
public final class Json {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private Json() {
    }

    public static String write(Object o) {
        try {
            return MAPPER.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 序列化失败", e);
        }
    }

    public static <T> T read(String s, Class<T> type) {
        try {
            return MAPPER.readValue(s, type);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 反序列化失败", e);
        }
    }
}
