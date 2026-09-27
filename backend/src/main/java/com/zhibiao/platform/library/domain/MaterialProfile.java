package com.zhibiao.platform.library.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/** material_profile：素材档案（V3），内容文件引用资产体系 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("material_profile")
public class MaterialProfile extends BaseEntity {
    /** 关联 biz_asset（category=MATERIAL） */
    private String assetId;
    /** 为空表示组织级 */
    private String projectId;
    /** PRIVATE/PROJECT/ORG */
    private String visibility;
    private String ownerUserId;
    /** COMPANY_PROFILE/CASE/CERTIFICATE/COPY/OTHER */
    private String materialType;
    private String companyName;
    private Instant validFrom;
    private Instant validUntil;
    private String remark;
    /** 乐观锁 */
    private Integer version;
}
