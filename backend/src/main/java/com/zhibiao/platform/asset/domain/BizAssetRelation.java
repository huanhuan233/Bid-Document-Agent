package com.zhibiao.platform.asset.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * biz_asset_relation：版本血缘（V2）。id 为主键，(from,to,type) 唯一键防重。
 */
@Data
@TableName("biz_asset_relation")
public class BizAssetRelation {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String fromVersionId;
    private String toVersionId;
    /** DERIVED_FROM/USES_TEMPLATE/REFERENCES_MATERIAL/REVIEW_TARGET/REVISION_BASE */
    private String relationType;
    private String createdBy;
    private Instant createdAt;
}
