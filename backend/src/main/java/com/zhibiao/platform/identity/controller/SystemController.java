package com.zhibiao.platform.identity.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhibiao.platform.identity.application.UserAdminService;
import com.zhibiao.platform.identity.application.dto.AuthDtos.AssignRolesRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.CreateUserRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.ResetPasswordRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.RoleVO;
import com.zhibiao.platform.identity.application.dto.AuthDtos.UpdateUserRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.UserStatusRequest;
import com.zhibiao.platform.identity.application.dto.AuthDtos.UserVO;
import com.zhibiao.platform.identity.domain.SysPermission;
import com.zhibiao.platform.identity.domain.SysRole;
import com.zhibiao.platform.identity.infrastructure.SysPermissionMapper;
import com.zhibiao.platform.identity.infrastructure.SysRoleMapper;
import com.zhibiao.platform.shared.security.RequirePermission;
import com.zhibiao.platform.shared.web.ApiResponse;
import com.zhibiao.platform.shared.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * /api/v1/system：用户、角色、权限管理。
 */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final UserAdminService userAdminService;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @GetMapping("/users")
    @RequirePermission("api:system:manage")
    public ApiResponse<PageResult<UserVO>> users(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
                                                 @RequestParam(required = false) String keyword) {
        var p = userAdminService.page(page, pageSize, keyword);
        return ApiResponse.ok(PageResult.of(p.getRecords(), p.getTotal(), page, pageSize));
    }

    @PostMapping("/users")
    @RequirePermission("api:system:manage")
    public ApiResponse<String> create(@RequestBody CreateUserRequest req) {
        return ApiResponse.ok(userAdminService.create(req));
    }

    @PostMapping("/users/{id}/update")
    @RequirePermission("api:system:manage")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody UpdateUserRequest req) {
        userAdminService.update(id, req);
        return ApiResponse.ok();
    }

    @PostMapping("/users/{id}/reset-password")
    @RequirePermission("api:system:manage")
    public ApiResponse<Void> resetPassword(@PathVariable String id, @RequestBody ResetPasswordRequest req) {
        userAdminService.resetPassword(id, req.newPassword());
        return ApiResponse.ok();
    }

    @PostMapping("/users/{id}/status")
    @RequirePermission("api:system:manage")
    public ApiResponse<Void> status(@PathVariable String id, @RequestBody UserStatusRequest req) {
        userAdminService.changeStatus(id, req.status());
        return ApiResponse.ok();
    }

    @PostMapping("/users/{id}/roles")
    @RequirePermission("api:system:manage")
    public ApiResponse<Void> roles(@PathVariable String id, @RequestBody AssignRolesRequest req) {
        userAdminService.assignRoles(id, req.roleCodes());
        return ApiResponse.ok();
    }

    @GetMapping("/roles")
    @RequirePermission("api:system:manage")
    public ApiResponse<List<RoleVO>> roles() {
        List<RoleVO> list = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getCode))
                .stream().map(r -> new RoleVO(r.getId(), r.getCode(), r.getName(), r.getDescription())).toList();
        return ApiResponse.ok(list);
    }

    @GetMapping("/permissions")
    @RequirePermission("api:system:manage")
    public ApiResponse<List<SysPermission>> permissions() {
        return ApiResponse.ok(permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSort)));
    }
}
