package com.zhibiao.platform.file.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * file_object：不可变物理文件元数据（V2 迁移）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_object")
public class FileObject extends BaseEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String projectId;
    private String bucket;
    private String objectKey;
    private String originalName;
    private Long sizeBytes;
    private String mediaType;
    private String ext;
    private String sha256;
    /** originals/materials/templates/results/reports/temp */
    private String category;
    /** UPLOADING/READY/DELETED/FAILED */
    private String status;
    private String uploaderId;
}
