package com.zhibiao.platform.project.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project")
public class BizProject extends BaseEntity {
    private String name;
    private String code;
    private String description;
    /** ACTIVE / ARCHIVED */
    private String status;
    private String ownerUserId;
    /** 乐观锁 */
    private Integer version;
}
