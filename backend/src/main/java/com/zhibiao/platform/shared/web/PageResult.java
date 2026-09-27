package com.zhibiao.platform.shared.web;

import java.util.List;

/**
 * 统一分页响应：items、total、page、page_size。
 */
public record PageResult<T>(List<T> items, long total, long page, long pageSize) {

    public static <T> PageResult<T> of(List<T> items, long total, long page, long pageSize) {
        return new PageResult<>(items, total, page, pageSize);
    }

    public static <T> PageResult<T> empty(long page, long pageSize) {
        return new PageResult<>(List.of(), 0, page, pageSize);
    }
}
