package com.zhibiao.platform.file.controller;

import com.zhibiao.platform.audit.application.AuditService;
import com.zhibiao.platform.file.application.FileStorageService;
import com.zhibiao.platform.file.domain.FileObject;
import com.zhibiao.platform.shared.security.SecurityUtils;
import com.zhibiao.platform.shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * /api/v1/files：上传、元数据、下载。
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    @PostMapping("/upload")
    public ApiResponse<FileObject> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "project_id", required = false) String projectId,
                                          @RequestParam(value = "category", required = false) String category) {
        return ApiResponse.ok(fileStorageService.upload(file, projectId, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<FileObject> get(@PathVariable String id) {
        FileObject f = fileStorageService.get(id);
        fileStorageService.assertReadable(f, SecurityUtils.currentUserId());
        return ApiResponse.ok(f);
    }

    @GetMapping("/{id}/download-url")
    public ApiResponse<Map<String, String>> downloadUrl(@PathVariable String id) {
        FileObject f = fileStorageService.get(id);
        String url = fileStorageService.presignedDownloadUrl(f);
        auditService.record("FILE_DOWNLOAD_URL", "file", id, f.getProjectId(), Map.of(), "SUCCESS");
        return ApiResponse.ok(Map.of("url", url, "expires_in", "1800"));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String id) {
        FileObject f = fileStorageService.get(id);
        var content = fileStorageService.openForDownload(f, SecurityUtils.currentUserId());
        auditService.record("FILE_DOWNLOAD", "file", id, f.getProjectId(), Map.of(), "SUCCESS");
        String encoded = URLEncoder.encode(content.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(
                        content.contentType() == null ? "application/octet-stream" : content.contentType()))
                .body(new InputStreamResource(content.stream()));
    }
}
