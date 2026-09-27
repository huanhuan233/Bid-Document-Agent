package com.zhibiao.platform.identity.application.dto;

import java.util.List;

/**
 * 认证相关传输对象。
 */
public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(String userName, String password) {
    }

    /** token 为服务端会话标识；同时以 HttpOnly Cookie 下发 */
    public record LoginResponse(String token, String refreshToken, String userId,
                                String userName, String displayName,
                                List<String> roles, List<String> permissions) {
    }

    public record UserInfoVO(String userId, String userName, String displayName,
                             List<String> roles, List<String> buttons) {
    }

    public record ChangePasswordRequest(String oldPassword, String newPassword) {
    }

    public record ResetPasswordRequest(String userId, String newPassword) {
    }

    public record CreateUserRequest(String username, String password, String displayName,
                                    String email, String phone, List<String> roleCodes) {
    }

    public record UpdateUserRequest(String displayName, String email, String phone) {
    }

    public record UserStatusRequest(String status) {
    }

    public record AssignRolesRequest(List<String> roleCodes) {
    }

    public record UserVO(String id, String username, String displayName, String email, String phone,
                         String status, boolean superUser, List<String> roles, String createdAt) {
    }

    public record RoleVO(String id, String code, String name, String description) {
    }
}
