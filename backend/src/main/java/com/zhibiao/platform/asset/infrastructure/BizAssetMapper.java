package com.zhibiao.platform.asset.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhibiao.platform.asset.domain.BizAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BizAssetMapper extends BaseMapper<BizAsset> {

    /** 带乐观锁的发布：版本一致时才移动发布指针 */
    @Update("UPDATE biz_asset SET current_published_version_id = #{versionId}, " +
            "version = version + 1, updated_at = UTC_TIMESTAMP(6) " +
            "WHERE id = #{assetId} AND version = #{expectedVersion} AND deleted = 0")
    int publishVersion(@Param("assetId") String assetId,
                       @Param("versionId") String versionId,
                       @Param("expectedVersion") Integer expectedVersion);
}
