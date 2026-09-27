package com.zhibiao.platform.library.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** template_profile：文档排版模板档案（V3） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("template_profile")
public class TemplateProfile extends BaseEntity {
    /** 关联 biz_asset（category=TEMPLATE） */
    private String assetId;
    private String projectId;
    /** PRIVATE/PROJECT/ORG */
    private String visibility;
    private String ownerUserId;
    /** COMMERCIAL/TECHNICAL/OFFICIAL/OTHER */
    private String templateType;
    /** DRAFT/PUBLISHED/ARCHIVED */
    private String status;
    /** 已发布版本指针，可变，乐观锁保护 */
    private String publishedVersionId;
    /** 乐观锁 */
    private Integer version;
}
