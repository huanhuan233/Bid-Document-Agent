package com.zhibiao.platform.identity.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String passwordHash;
    private String displayName;
    private String email;
    private String phone;
    /** ENABLED / DISABLED */
    private String status;
    private Boolean isSuper;
    /** 后端生成的稳定 Dify 系统用户标识 */
    private String difyUserId;
    /** 乐观锁 */
    private Integer version;
}
