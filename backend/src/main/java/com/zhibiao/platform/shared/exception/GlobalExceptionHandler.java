package com.zhibiao.platform.shared.exception;

import com.zhibiao.platform.shared.web.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：文件、SSE 与真实 HTTP 错误不强套 JSON 成功返回。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> biz(BizException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> auth(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorCodes.UNAUTHENTICATED, "未登录或会话已失效"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> denied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorCodes.FORBIDDEN, "无权访问该资源"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(FieldError::getDefaultMessage).orElse("参数校验失败");
        return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCodes.BAD_REQUEST, msg));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> tooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(413).body(ApiResponse.fail(ErrorCodes.BAD_REQUEST, "文件大小超出限制"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> noResource(NoResourceFoundException e) {
        return ResponseEntity.status(404).body(ApiResponse.fail(ErrorCodes.NOT_FOUND, "资源不存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unknown(Exception e) {
        log.error("unhandled error", e);
        return ResponseEntity.status(500).body(ApiResponse.fail(ErrorCodes.INTERNAL, "系统内部错误"));
    }
}
