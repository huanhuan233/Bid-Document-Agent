package com.zhibiao.platform.library.infrastructure;

import com.zhibiao.platform.library.domain.LibraryAssetTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * library_asset_tag：复合主键表，全部走自定义 SQL。
 */
@Mapper
public interface LibraryAssetTagMapper {

    @Insert("INSERT IGNORE INTO library_asset_tag (asset_id, tag_id) VALUES (#{t.assetId}, #{t.tagId})")
    int insertIgnore(@Param("t") LibraryAssetTag relation);

    @Delete("DELETE FROM library_asset_tag WHERE asset_id = #{assetId} AND tag_id = #{tagId}")
    int deleteOne(@Param("assetId") String assetId, @Param("tagId") String tagId);

    @Select("SELECT tag_id AS tagId FROM library_asset_tag WHERE asset_id = #{assetId}")
    List<String> tagIdsOfAsset(@Param("assetId") String assetId);
}
