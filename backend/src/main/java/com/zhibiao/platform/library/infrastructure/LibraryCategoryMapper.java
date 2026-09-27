package com.zhibiao.platform.library.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.library.domain.LibraryCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LibraryCategoryMapper extends BaseMapper<LibraryCategory> {
}
