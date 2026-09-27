package com.zhibiao.platform.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhibiao.platform.asset.application.AssetService;
import com.zhibiao.platform.asset.domain.BizAsset;
import com.zhibiao.platform.asset.domain.BizAssetVersion;
import com.zhibiao.platform.library.application.LibraryService;
import com.zhibiao.platform.library.domain.LibraryCategory;
import com.zhibiao.platform.library.domain.LibraryTag;
import com.zhibiao.platform.library.domain.MaterialProfile;
import com.zhibiao.platform.library.domain.TemplateProfile;
import com.zhibiao.platform.library.infrastructure.LibraryCategoryMapper;
import com.zhibiao.platform.library.infrastructure.LibraryTagMapper;
import com.zhibiao.platform.library.infrastructure.MaterialProfileMapper;
import com.zhibiao.platform.library.infrastructure.TemplateProfileMapper;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.security.SecurityUtils;
import com.zhibiao.platform.shared.web.ApiResponse;
import com.zhibiao.platform.shared.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * /api/v1/library：素材库、模板库、分类与标签（V3）。
 */
@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

    private final AssetService assetService;
    private final LibraryService libraryService;
    private final MaterialProfileMapper materialMapper;
    private final TemplateProfileMapper templateMapper;
    private final LibraryCategoryMapper categoryMapper;
    private final LibraryTagMapper tagMapper;

    // ---------- 素材 ----------

    @PostMapping("/materials")
    @Transactional
    public ApiResponse<Map<String, Object>> createMaterial(@RequestBody Map<String, String> body) {
        String fileId = body.get("fileObjectId");
        libraryService.validateProfileFile(fileId);
        BizAsset asset = assetService.createAsset("MATERIAL", body.get("projectId"), body.get("name"));
        String visibility = body.getOrDefault("visibility", "PRIVATE");
        libraryService.attachProfile(asset, visibility, body.get("projectId"));
        MaterialProfile profile = new MaterialProfile();
        profile.setAssetId(asset.getId());
        profile.setVisibility(visibility);
        profile.setProjectId(body.get("projectId"));
        profile.setOwnerUserId(SecurityUtils.currentUserId());
        profile.setMaterialType(body.getOrDefault("materialType", "OTHER"));
        profile.setCompanyName(body.get("companyName"));
        profile.setRemark(body.get("remark"));
        profile.setVersion(0);
        materialMapper.insert(profile);
        BizAssetVersion v = assetService.createVersion(asset.getId(), fileId, body.get("summary"), null);
        return ApiResponse.ok(Map.of("asset", asset, "profile", profile, "version", v));
    }

    @GetMapping("/materials")
    public ApiResponse<PageResult<Map<String, Object>>> materials(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "material_type") String materialType,
            @RequestParam(required = false, name = "valid_only") boolean validOnly) {
        LambdaQueryWrapper<MaterialProfile> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(materialType), MaterialProfile::getMaterialType, materialType)
                .orderByDesc(MaterialProfile::getCreatedAt);
        Page<MaterialProfile> p = materialMapper.selectPage(new Page<>(page, pageSize), qw);
        List<Map<String, Object>> rows = p.getRecords().stream()
                .filter(mp -> !validOnly || mp.getValidUntil() == null
                        || mp.getValidUntil().isAfter(Instant.now()))
                .map(mp -> visibleMaterialRow(mp, keyword))
                .filter(r -> r != null)
                .toList();
        return ApiResponse.ok(PageResult.of(rows, rows.size(), page, pageSize));
    }

    private Map<String, Object> visibleMaterialRow(MaterialProfile mp, String keyword) {
        try {
            libraryService.assertVisible(mp.getVisibility(), mp.getProjectId(), mp.getOwnerUserId());
            BizAsset asset = assetService.get(mp.getAssetId());
            if (StringUtils.hasText(keyword) && !asset.getName().contains(keyword)) {
                return null;
            }
            return Map.of("profile", mp, "asset", asset);
        } catch (BizException e) {
            return null;
        }
    }

    // ---------- 模板 ----------

    @PostMapping("/templates")
    @Transactional
    public ApiResponse<Map<String, Object>> createTemplate(@RequestBody Map<String, String> body) {
        String fileId = body.get("fileObjectId");
        libraryService.validateProfileFile(fileId);
        BizAsset asset = assetService.createAsset("TEMPLATE", body.get("projectId"), body.get("name"));
        String visibility = body.getOrDefault("visibility", "PRIVATE");
        libraryService.attachProfile(asset, visibility, body.get("projectId"));
        TemplateProfile profile = new TemplateProfile();
        profile.setAssetId(asset.getId());
        profile.setVisibility(visibility);
        profile.setProjectId(body.get("projectId"));
        profile.setOwnerUserId(SecurityUtils.currentUserId());
        profile.setTemplateType(body.getOrDefault("templateType", "OTHER"));
        profile.setStatus("DRAFT");
        profile.setVersion(0);
        templateMapper.insert(profile);
        BizAssetVersion v = assetService.createVersion(asset.getId(), fileId, body.get("summary"), null);
        return ApiResponse.ok(Map.of("asset", asset, "profile", profile, "version", v));
    }

    /** 模板发布：移动资产发布指针 + 更新模板状态（乐观锁在 asset 侧保护） */
    @PostMapping("/templates/{id}/publish")
    @Transactional
    public ApiResponse<TemplateProfile> publishTemplate(@PathVariable String id, @RequestBody Map<String, String> body) {
        TemplateProfile profile = templateMapper.selectById(id);
        if (profile == null) {
            throw BizException.notFound("模板不存在");
        }
        String versionId = body.get("versionId");
        if (!StringUtils.hasText(versionId)) {
            throw BizException.badRequest("versionId 必填");
        }
        assetService.publishVersion(profile.getAssetId(), versionId);
        profile.setStatus("PUBLISHED");
        profile.setPublishedVersionId(versionId);
        templateMapper.updateById(profile);
        return ApiResponse.ok(profile);
    }

    @GetMapping("/templates")
    public ApiResponse<PageResult<Map<String, Object>>> templates(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(name = "page_size", defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "template_type") String templateType,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<TemplateProfile> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(templateType), TemplateProfile::getTemplateType, templateType)
                .eq(StringUtils.hasText(status), TemplateProfile::getStatus, status)
                .orderByDesc(TemplateProfile::getCreatedAt);
        Page<TemplateProfile> p = templateMapper.selectPage(new Page<>(page, pageSize), qw);
        List<Map<String, Object>> rows = p.getRecords().stream()
                .map(tp -> visibleTemplateRow(tp, keyword))
                .filter(r -> r != null)
                .toList();
        return ApiResponse.ok(PageResult.of(rows, rows.size(), page, pageSize));
    }

    private Map<String, Object> visibleTemplateRow(TemplateProfile tp, String keyword) {
        try {
            libraryService.assertVisible(tp.getVisibility(), tp.getProjectId(), tp.getOwnerUserId());
            BizAsset asset = assetService.get(tp.getAssetId());
            if (StringUtils.hasText(keyword) && !asset.getName().contains(keyword)) {
                return null;
            }
            return Map.of("profile", tp, "asset", asset);
        } catch (BizException e) {
            return null;
        }
    }

    // ---------- 分类 / 标签 ----------

    @PostMapping("/categories")
    public ApiResponse<LibraryCategory> createCategory(@RequestBody Map<String, String> body) {
        String kind = body.getOrDefault("kind", "MATERIAL");
        if (!List.of("MATERIAL", "TEMPLATE").contains(kind)) {
            throw BizException.badRequest("非法分类类别");
        }
        if (!StringUtils.hasText(body.get("name"))) {
            throw BizException.badRequest("分类名必填");
        }
        LibraryCategory c = new LibraryCategory();
        c.setName(body.get("name").trim());
        c.setKind(kind);
        c.setParentId(body.get("parentId"));
        c.setSort(Integer.valueOf(body.getOrDefault("sort", "0")));
        categoryMapper.insert(c);
        return ApiResponse.ok(c);
    }

    @GetMapping("/categories")
    public ApiResponse<List<LibraryCategory>> categories(@RequestParam(required = false) String kind) {
        LambdaQueryWrapper<LibraryCategory> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(kind), LibraryCategory::getKind, kind)
                .orderByAsc(LibraryCategory::getSort);
        return ApiResponse.ok(categoryMapper.selectList(qw));
    }

    @PostMapping("/tags")
    public ApiResponse<LibraryTag> createTag(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(libraryService.createTag(
                body.get("name"), body.getOrDefault("kind", "MATERIAL")));
    }

    @GetMapping("/tags")
    public ApiResponse<List<LibraryTag>> tags(@RequestParam(required = false) String kind) {
        LambdaQueryWrapper<LibraryTag> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(kind), LibraryTag::getKind, kind)
                .orderByAsc(LibraryTag::getName);
        return ApiResponse.ok(tagMapper.selectList(qw));
    }

    @PostMapping("/assets/{assetId}/tags")
    public ApiResponse<Void> tagAsset(@PathVariable String assetId, @RequestBody Map<String, String> body) {
        assetService.assertReadable(assetService.get(assetId), SecurityUtils.currentUserId());
        libraryService.tagAsset(assetId, body.get("name"), body.getOrDefault("kind", "MATERIAL"));
        return ApiResponse.ok();
    }
}
