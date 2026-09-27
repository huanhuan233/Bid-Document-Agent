package com.zhibiao.platform.asset.controller;

import com.zhibiao.platform.asset.application.AssetService;
import com.zhibiao.platform.asset.domain.BizAsset;
import com.zhibiao.platform.asset.domain.BizAssetRelation;
import com.zhibiao.platform.asset.domain.BizAssetVersion;
import com.zhibiao.platform.shared.security.SecurityUtils;
import com.zhibiao.platform.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * /api/v1/assets：资产、版本、血缘关系。
 */
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ApiResponse<BizAsset> create(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(assetService.createAsset(
                body.getOrDefault("category", "OTHER"), body.get("projectId"), body.get("name")));
    }

    @GetMapping("/{id}")
    public ApiResponse<BizAsset> get(@PathVariable String id) {
        BizAsset asset = assetService.get(id);
        assetService.assertReadable(asset, SecurityUtils.currentUserId());
        return ApiResponse.ok(asset);
    }

    @PostMapping("/{id}/versions")
    public ApiResponse<BizAssetVersion> createVersion(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(assetService.createVersion(id, body.get("fileObjectId"),
                body.get("summary"), body.get("metaJson")));
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<List<BizAssetVersion>> versions(@PathVariable String id) {
        BizAsset asset = assetService.get(id);
        assetService.assertReadable(asset, SecurityUtils.currentUserId());
        return ApiResponse.ok(assetService.versions(id));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<BizAsset> publish(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(assetService.publishVersion(id, body.get("versionId")));
    }

    @PostMapping("/relations")
    public ApiResponse<BizAssetRelation> addRelation(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(assetService.addRelation(
                body.get("fromVersionId"), body.get("toVersionId"), body.getOrDefault("relationType", "DERIVED_FROM")));
    }

    @GetMapping("/versions/{versionId}/relations")
    public ApiResponse<List<BizAssetRelation>> relations(@PathVariable String versionId) {
        return ApiResponse.ok(assetService.relationsOf(versionId));
    }
}
