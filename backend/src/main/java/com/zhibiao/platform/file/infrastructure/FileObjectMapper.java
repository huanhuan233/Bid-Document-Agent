package com.zhibiao.platform.file.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.file.domain.FileObject;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileObjectMapper extends BaseMapper<FileObject> {
}
