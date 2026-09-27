package com.zhibiao.platform.project.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project_member")
public class BizProjectMember extends BaseEntity {
    private String projectId;
    private String userId;
    /** OWNER / EDITOR / VIEWER */
    private String role;
}
