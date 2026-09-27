package com.zhibiao.platform.audit.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.audit.domain.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}
