package com.zhibiao.platform.shared.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    public BizException(String code, String message) {
        this(code, message, 400);
    }

    public BizException(String code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public static BizException badRequest(String message) {
        return new BizException(ErrorCodes.BAD_REQUEST, message);
    }

    public static BizException notFound(String message) {
        return new BizException(ErrorCodes.NOT_FOUND, message, 404);
    }

    public static BizException forbidden(String message) {
        return new BizException(ErrorCodes.FORBIDDEN, message, 403);
    }

    public static BizException conflict(String message) {
        return new BizException(ErrorCodes.STATE_CONFLICT, message, 409);
    }

    public static BizException integration(String message) {
        return new BizException(ErrorCodes.INTEGRATION_ERROR, message, 502);
    }

    public static BizException internal(String message) {
        return new BizException(ErrorCodes.INTERNAL, message, 500);
    }
}
