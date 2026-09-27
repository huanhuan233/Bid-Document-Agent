package com.zhibiao.platform.asset.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhibiao.platform.asset.domain.BizAsset;
import com.zhibiao.platform.asset.domain.BizAssetRelation;
import com.zhibiao.platform.asset.domain.BizAssetVersion;
import com.zhibiao.platform.asset.infrastructure.BizAssetMapper;
import com.zhibiao.platform.asset.infrastructure.BizAssetRelationMapper;
import com.zhibiao.platform.asset.infrastructure.BizAssetVersionMapper;
import com.zhibiao.platform.file.application.FileStorageService;
import com.zhibiao.platform.file.domain.FileObject;
import com.zhibiao.platform.project.application.ProjectAccessService;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 资产与版本（V2）：版本号原子分配 + 唯一键兜底；发布走乐观锁移动发布指针；
 * 版本一经确认不可修改内容，只能被新版本替代或归档。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetService {

    public static final List<String> CATEGORIES = List.of(
            "SOURCE_DOCUMENT", "MATERIAL", "TEMPLATE", "TENDER_REQUIREMENT",
            "BID_DOCUMENT", "REVIEW_REPORT", "OTHER");
    public static final List<String> RELATION_TYPES = List.of(
            "DERIVED_FROM", "USES_TEMPLATE", "REFERENCES_MATERIAL", "REVIEW_TARGET", "REVISION_BASE");

    private final BizAssetMapper assetMapper;
    private final BizAssetVersionMapper versionMapper;
    private final BizAssetRelationMapper relationMapper;
    private final FileStorageService fileStorageService;
    private final ProjectAccessService projectAccess;

    @Transactional
    public BizAsset createAsset(String category, String projectId, String name) {
        if (!CATEGORIES.contains(category)) {
            throw BizException.badRequest("非法资产类型");
        }
        if (!StringUtils.hasText(projectId)) {
            throw BizException.badRequest("资产必须归属项目");
        }
        if (!StringUtils.hasText(name)) {
            throw BizException.badRequest("资产名称必填");
        }
        String userId = SecurityUtils.currentUserId();
        projectAccess.assertRole(projectId, userId, ProjectAccessService.EDITOR);
        BizAsset asset = new BizAsset();
        asset.setProjectId(projectId);
        asset.setCategory(category);
        asset.setName(name.trim());
        asset.setOwnerUserId(userId);
        asset.setVersion(0);
        assetMapper.insert(asset);
        return asset;
    }

    @Transactional
    public BizAssetVersion createVersion(String assetId, String fileObjectId, String summary, String metaJson) {
        BizAsset asset = mustFind(assetId);
        String userId = SecurityUtils.currentUserId();
        projectAccess.assertRole(asset.getProjectId(), userId, ProjectAccessService.EDITOR);
        FileObject file = fileStorageService.requireReady(fileObjectId);
        // 文件项目归属必须与资产一致
        if (!asset.getProjectId().equals(file.getProjectId())) {
            throw BizException.badRequest("文件所属项目与资产不一致");
        }
        int versionNo = versionMapper.nextVersionNo(assetId);
        BizAssetVersion v = new BizAssetVersion();
        v.setAssetId(assetId);
        v.setVersionNo(versionNo);
        v.setFileObjectId(fileObjectId);
        v.setStatus("DRAFT");
        v.setSummary(summary);
        v.setMetaJson(metaJson);
        v.setCreatedBy(userId);
        try {
            versionMapper.insertIgnore(v);
        } catch (DuplicateKeyException e) {
            throw BizException.conflict("版本号冲突，请重试");
        }
        log.info("asset version created asset={} version={} file={}", assetId, versionNo, fileObjectId);
        return v;
    }

    /** 发布指定版本：乐观锁移动发布指针；发布后该版本状态置 CONFIRMED */
    @Transactional
    public BizAsset publishVersion(String assetId, String versionId) {
        BizAsset asset = mustFind(assetId);
        projectAccess.assertRole(asset.getProjectId(), SecurityUtils.currentUserId(), ProjectAccessService.EDITOR);
        BizAssetVersion v = requireVersion(versionId);
        if (!assetId.equals(v.getAssetId())) {
            throw BizException.badRequest("版本不属于该资产");
        }
        if ("ARCHIVED".equals(v.getStatus())) {
            throw BizException.badRequest("版本已归档，不能发布");
        }
        int rows = assetMapper.publishVersion(assetId, versionId, asset.getVersion());
        if (rows == 0) {
            throw BizException.conflict("发布冲突：资产正在被其他操作修改，请重试");
        }
        v.setStatus("CONFIRMED");
        versionMapper.updateById(v);
        BizAsset current = mustFind(assetId);
        log.info("asset published asset={} versionId={}", assetId, versionId);
        return current;
    }

    public BizAsset get(String assetId) {
        return mustFind(assetId);
    }

    public BizAssetVersion versionOf(String assetId, int versionNo) {
        BizAssetVersion v = versionMapper.selectOne(new LambdaQueryWrapper<BizAssetVersion>()
                .eq(BizAssetVersion::getAssetId, assetId)
                .eq(BizAssetVersion::getVersionNo, versionNo));
        if (v == null) {
            throw BizException.notFound("版本不存在");
        }
        return v;
    }

    public BizAssetVersion requireVersion(String versionId) {
        BizAssetVersion v = versionMapper.selectById(versionId);
        if (v == null) {
            throw BizException.notFound("版本不存在");
        }
        return v;
    }

    public List<BizAssetVersion> versions(String assetId) {
        mustFind(assetId);
        return versionMapper.selectList(new LambdaQueryWrapper<BizAssetVersion>()
                .eq(BizAssetVersion::getAssetId, assetId)
                .orderByDesc(BizAssetVersion::getVersionNo));
    }

    /** 资产读取访问：项目资产按成员校验 */
    public void assertReadable(BizAsset asset, String userId) {
        if (SecurityUtils.isSuper()) {
            return;
        }
        projectAccess.assertMember(asset.getProjectId(), userId);
    }

    @Transactional
    public BizAssetRelation addRelation(String fromVersionId, String toVersionId, String relationType) {
        if (!RELATION_TYPES.contains(relationType)) {
            throw BizException.badRequest("非法关系类型");
        }
        BizAssetVersion from = requireVersion(fromVersionId);
        BizAssetVersion to = requireVersion(toVersionId);
        String userId = SecurityUtils.currentUserId();
        assertReadable(mustFind(from.getAssetId()), userId);
        assertReadable(mustFind(to.getAssetId()), userId);
        BizAssetRelation r = new BizAssetRelation();
        r.setFromVersionId(fromVersionId);
        r.setToVersionId(toVersionId);
        r.setRelationType(relationType);
        r.setCreatedBy(userId);
        relationMapper.insertIgnore(r);
        return r;
    }

    public List<BizAssetRelation> relationsOf(String versionId) {
        return relationMapper.findByVersionId(versionId);
    }

    private BizAsset mustFind(String id) {
        BizAsset a = assetMapper.selectById(id);
        if (a == null) {
            throw BizException.notFound("资产不存在");
        }
        return a;
    }
}

