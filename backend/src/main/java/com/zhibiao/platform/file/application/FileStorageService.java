package com.zhibiao.platform.file.application;

import com.zhibiao.platform.file.domain.FileObject;
import com.zhibiao.platform.file.infrastructure.FileObjectMapper;
import com.zhibiao.platform.project.application.ProjectAccessService;
import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.port.ObjectStoragePort;
import com.zhibiao.platform.shared.security.SecurityUtils;
import com.zhibiao.platform.shared.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

/**
 * 文件上传/查询/下载：大小与扩展名白名单校验，对象键服务器生成（绝不用用户文件名做路径）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final long MAX_SIZE = 100L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of(
            "docx", "pdf", "xlsx", "pptx", "md", "txt", "png", "jpg", "jpeg", "zip");
    private static final Set<String> ALLOWED_CATEGORY = Set.of(
            "originals", "materials", "templates", "results", "reports", "temp");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneOffset.UTC);

    private final FileObjectMapper fileMapper;
    private final ObjectStoragePort storage;
    private final ProjectAccessService projectAccess;

    @Value("${zhibiao.storage.bucket}")
    private String bucket;

    @Value("${zhibiao.storage.presign-ttl-seconds:300}")
    private int presignTtlSeconds;

    public FileObject upload(MultipartFile file, String projectId, String category) {
        if (file == null || file.isEmpty()) {
            throw BizException.badRequest("上传文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw BizException.badRequest("文件超过 100MB 上限");
        }
        String ext = extOf(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw BizException.badRequest("不支持的文件类型: " + ext);
        }
        String finalCategory = StringUtils.hasText(category) ? category : "originals";
        if (!ALLOWED_CATEGORY.contains(finalCategory)) {
            throw BizException.badRequest("非法文件分类");
        }
        if (StringUtils.hasText(projectId)) {
            projectAccess.assertMember(projectId, SecurityUtils.currentUserId());
        }
        String key = buildKey(finalCategory, ext);
        String userId = SecurityUtils.currentUserId();
        String mediaType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

        // 先登记 UPLOADING 再写存储；写失败置 FAILED，残留对象由清理任务回收
        FileObject rec = new FileObject();
        rec.setProjectId(projectId);
        rec.setBucket(bucket);
        rec.setObjectKey(key);
        rec.setOriginalName(file.getOriginalFilename());
        rec.setSizeBytes(file.getSize());
        rec.setMediaType(mediaType);
        rec.setExt(ext);
        rec.setCategory(finalCategory);
        rec.setStatus("UPLOADING");
        rec.setUploaderId(userId);
        fileMapper.insert(rec);

        try (InputStream in = file.getInputStream()) {
            storage.put(bucket, key, in, file.getSize(), mediaType);
        } catch (IOException e) {
            rec.setStatus("FAILED");
            fileMapper.updateById(rec);
            throw BizException.internal("文件写入存储失败");
        }
        rec.setStatus("READY");
        fileMapper.updateById(rec);
        log.info("file uploaded id={} size={} ext={} category={} project={}",
                rec.getId(), file.getSize(), ext, finalCategory, projectId);
        return rec;
    }

    public FileObject get(String fileId) {
        FileObject f = fileMapper.selectById(fileId);
        if (f == null || "DELETED".equals(f.getStatus())) {
            throw BizException.notFound("文件不存在");
        }
        return f;
    }

    /** 访问控制：项目文件走成员校验，公共库文件在 library 模块校验可见性 */
    public void assertReadable(FileObject f, String userId) {
        if (SecurityUtils.isSuper()) {
            return;
        }
        if (StringUtils.hasText(f.getProjectId())) {
            projectAccess.assertMember(f.getProjectId(), userId);
        }
    }

    public String presignedDownloadUrl(FileObject f) {
        assertReadable(f, SecurityUtils.currentUserId());
        return storage.presignedGetUrl(f.getBucket(), f.getObjectKey(), presignTtlSeconds);
    }

    /** 下载（审计点）：受访问控制保护 */
    public InputStreamWithMeta openForDownload(FileObject f, String userId) {
        assertReadable(f, userId);
        InputStream in = storage.get(f.getBucket(), f.getObjectKey());
        return new InputStreamWithMeta(in, f.getOriginalName(), f.getMediaType(), f.getSizeBytes());
    }

    /** 存储侧完整性核对（大小/ETag 与元数据比对） */
    public void verifyStored(FileObject f) {
        ObjectStoragePort.StorageStat stat = storage.stat(f.getBucket(), f.getObjectKey());
        if (stat.size() != f.getSizeBytes()) {
            throw BizException.internal("文件大小与元数据不一致");
        }
    }

    public FileObject requireReady(String fileId) {
        FileObject f = get(fileId);
        if (!"READY".equals(f.getStatus())) {
            throw BizException.badRequest("文件尚未就绪");
        }
        return f;
    }

    private String buildKey(String category, String ext) {
        return "%s/%s/%s.%s".formatted(category, DATE_FMT.format(Instant.now()), Ids.uuid(), ext);
    }

    private static String extOf(String name) {
        if (!StringUtils.hasText(name) || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    public record InputStreamWithMeta(InputStream stream, String fileName, String contentType, Long size) {
    }
}
