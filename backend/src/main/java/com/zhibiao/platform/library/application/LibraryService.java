package com.zhibiao.platform.library.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhibiao.platform.asset.application.AssetService;
import com.zhibiao.platform.asset.domain.BizAsset;
import com.zhibiao.platform.file.application.FileStorageService;
import com.zhibiao.platform.file.domain.FileObject;
import com.zhibiao.platform.library.domain.LibraryAssetTag;
import com.zhibiao.platform.library.domain.LibraryTag;
import com.zhibiao.platform.library.infrastructure.LibraryAssetTagMapper;
import com.zhibiao.platform.library.infrastructure.LibraryTagMapper;
import com.zhibiao.platform.project.application.ProjectAccessService;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 素材/模板入库与可见性判定：PRIVATE（仅本人）/ PROJECT（项目成员）/ ORG（全体登录用户）。
 */
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final AssetService assetService;
    private final FileStorageService fileStorageService;
    private final ProjectAccessService projectAccess;
    private final LibraryTagMapper tagMapper;
    private final LibraryAssetTagMapper assetTagMapper;

    /** 入库校验：可见性合法性 + 项目权限；profile 记录由调用方写入 */
    public void attachProfile(BizAsset asset, String visibility, String projectId) {
        String userId = SecurityUtils.currentUserId();
        if (!List.of("PRIVATE", "PROJECT", "ORG").contains(visibility)) {
            throw BizException.badRequest("非法可见性");
        }
        if ("PROJECT".equals(visibility)) {
            if (!StringUtils.hasText(projectId)) {
                throw BizException.badRequest("项目可见性必须指定项目");
            }
            projectAccess.assertRole(projectId, userId, ProjectAccessService.EDITOR);
        }
        assetService.assertReadable(asset, userId);
    }

    /** 可见性判定：不通过则与不存在同样返回 404，避免探测 */
    public void assertVisible(String visibility, String projectId, String ownerUserId) {
        String userId = SecurityUtils.currentUserId();
        if (SecurityUtils.isSuper() || userId.equals(ownerUserId)) {
            return;
        }
        switch (visibility) {
            case "ORG" -> {
            }
            case "PROJECT" -> projectAccess.assertMember(projectId, userId);
            default -> throw BizException.notFound("资源不存在");
        }
    }

    public void validateProfileFile(String fileId) {
        FileObject f = fileStorageService.requireReady(fileId);
        fileStorageService.assertReadable(f, SecurityUtils.currentUserId());
    }

    public LibraryTag createTag(String name, String kind) {
        if (!StringUtils.hasText(name)) {
            throw BizException.badRequest("标签名必填");
        }
        if (!List.of("MATERIAL", "TEMPLATE").contains(kind)) {
            throw BizException.badRequest("非法标签类别");
        }
        LibraryTag exists = tagMapper.selectOne(new LambdaQueryWrapper<LibraryTag>()
                .eq(LibraryTag::getKind, kind)
                .eq(LibraryTag::getName, name));
        if (exists != null) {
            return exists;
        }
        LibraryTag tag = new LibraryTag();
        tag.setName(name.trim());
        tag.setKind(kind);
        tagMapper.insert(tag);
        return tag;
    }

    public void tagAsset(String assetId, String tagName, String kind) {
        LibraryTag tag = createTag(tagName, kind);
        LibraryAssetTag at = new LibraryAssetTag();
        at.setAssetId(assetId);
        at.setTagId(tag.getId());
        assetTagMapper.insertIgnore(at);
    }

    public List<LibraryTag> tagsOf(String assetId) {
        List<String> tagIds = assetTagMapper.tagIdsOfAsset(assetId);
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectList(new LambdaQueryWrapper<LibraryTag>().in(LibraryTag::getId, tagIds));
    }
}
