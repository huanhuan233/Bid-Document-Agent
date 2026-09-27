package com.zhibiao.platform.asset.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.asset.domain.BizAssetVersion;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizAssetVersionMapper extends BaseMapper<BizAssetVersion> {

    @Select("SELECT COALESCE(MAX(version_no), 0) + 1 FROM biz_asset_version WHERE asset_id = #{assetId}")
    int nextVersionNo(@Param("assetId") String assetId);

    @Insert("INSERT IGNORE INTO biz_asset_version (id, asset_id, version_no, file_object_id, status, summary, " +
            "meta_json, created_by, created_at) VALUES (#{v.id}, #{v.assetId}, #{v.versionNo}, #{v.fileObjectId}, " +
            "#{v.status}, #{v.summary}, #{v.metaJson}, #{v.createdBy}, UTC_TIMESTAMP(6))")
    int insertIgnore(@Param("v") BizAssetVersion version);
}
