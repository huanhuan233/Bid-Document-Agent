package com.zhibiao.platform.asset.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * biz_asset_version：不可变版本（V2 迁移）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_asset_version")
public class BizAssetVersion extends BaseEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String assetId;
    private Integer versionNo;
    private String fileObjectId;
    /** DRAFT/CONFIRMED/ARCHIVED */
    private String status;
    private String summary;
    private String metaJson;
    @com.baomidou.mybatisplus.annotation.TableField("created_by")
    private String createdBy;
}
