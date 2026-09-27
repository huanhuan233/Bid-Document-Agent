package com.zhibiao.platform.project.controller;

import com.zhibiao.platform.project.application.ProjectService;
import com.zhibiao.platform.project.domain.BizProject;
import com.zhibiao.platform.shared.security.SecurityUtils;
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

import java.util.Map;

/**
 * /api/v1/projects：项目和成员。
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ApiResponse<BizProject> create(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(projectService.create(body.get("name"), body.get("code"), body.get("description")));
    }

    @GetMapping
    public ApiResponse<PageResult<BizProject>> list(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(name = "page_size", defaultValue = "20") long pageSize) {
        var p = projectService.page(page, pageSize, SecurityUtils.currentUserId());
        return ApiResponse.ok(PageResult.of(p.getRecords(), p.getTotal(), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<BizProject> get(@PathVariable String id) {
        return ApiResponse.ok(projectService.get(id));
    }

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable String id) {
        projectService.archive(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/members")
    public ApiResponse<Void> addMember(@PathVariable String id, @RequestBody Map<String, String> body) {
        projectService.addMember(id, body.get("userId"), body.getOrDefault("role", "VIEWER"));
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/members")
    public ApiResponse<Map<String, String>> members(@PathVariable String id) {
        return ApiResponse.ok(projectService.memberRoles(id));
    }

    @PostMapping("/{id}/members/{userId}/remove")
    public ApiResponse<Void> removeMember(@PathVariable String id, @PathVariable String userId) {
        projectService.removeMember(id, userId);
        return ApiResponse.ok();
    }
}
