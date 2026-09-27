package com.zhibiao.platform.shared.exception;

/**
 * 业务错误码约定：
 * 0000 成功；8xxx 会话/登录相关（与前端 Soybean 退出码对齐）；B1xxx 业务校验；A0xxx 授权。
 */
public final class ErrorCodes {
    private ErrorCodes() {
    }

    /** 成功 */
    public static final String OK = "0000";
    /** 未登录/会话失效：前端收到后跳转登录页 */
    public static final String UNAUTHENTICATED = "8888";
    /** 已登录但权限不足 */
    public static final String FORBIDDEN = "A0403";
    /** 通用业务错误 */
    public static final String BAD_REQUEST = "B1000";
    /** 资源不存在 */
    public static final String NOT_FOUND = "B1404";
    /** 幂等冲突：相同幂等键不同参数 */
    public static final String IDEMPOTENCY_CONFLICT = "B1409";
    /** 状态冲突（乐观锁/条件更新失败） */
    public static final String STATE_CONFLICT = "B14091";
    /** 外部集成失败 */
    public static final String INTEGRATION_ERROR = "B1500";
    /** 系统内部错误 */
    public static final String INTERNAL = "B9999";
}
