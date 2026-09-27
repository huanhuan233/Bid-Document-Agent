package com.zhibiao.platform.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 统一 JSON 响应：code、message、data、trace_id。
 * msg 为 message 的镜像字段，兼容 Soybean 请求封装读取 response.data.msg。
 * 业务主键在 JSON 中以字符串返回；时间为 UTC ISO 字符串。
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(
        String code,
        String message,
        String msg,
        T data,
        @JsonProperty("trace_id") String traceId) {

    public static final String SUCCESS = "0000";

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(SUCCESS, "ok", "ok", data, TraceIdFilter.currentTraceId());
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(code, message, message, null, TraceIdFilter.currentTraceId());
    }
}
