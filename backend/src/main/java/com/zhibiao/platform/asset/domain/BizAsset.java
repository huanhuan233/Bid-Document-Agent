package com.zhibiao.platform.asset.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * biz_asset：逻辑成果对象（V2 迁移）。current_published_version_id 为发布指针。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_asset")
public class BizAsset extends BaseEntity {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String projectId;
    /** SOURCE_DOCUMENT/MATERIAL/TEMPLATE/TENDER_REQUIREMENT/BID_DOCUMENT/REVIEW_REPORT/OTHER */
    private String category;
    private String name;
    /** 发布指针，可变，乐观锁保护 */
    private String currentPublishedVersionId;
    private String ownerUserId;
    /** 乐观锁计数 */
    private Integer version;
}
