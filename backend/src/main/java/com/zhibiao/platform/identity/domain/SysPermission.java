package com.zhibiao.platform.identity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {
    private String code;
    private String name;
    /** MENU / API / DATA */
    private String type;
    private String parentId;
    private Integer sort;
}
