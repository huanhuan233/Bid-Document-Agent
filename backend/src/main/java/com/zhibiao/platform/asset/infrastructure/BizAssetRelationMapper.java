package com.zhibiao.platform.asset.infrastructure;

import com.zhibiao.platform.asset.domain.BizAssetRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 复合主键关系表：全部走自定义 SQL。
 */
@Mapper
public interface BizAssetRelationMapper {

    @Insert("INSERT IGNORE INTO biz_asset_relation (id, from_version_id, to_version_id, relation_type, created_by, created_at) " +
            "VALUES (#{r.id}, #{r.fromVersionId}, #{r.toVersionId}, #{r.relationType}, #{r.createdBy}, UTC_TIMESTAMP(6))")
    int insertIgnore(@Param("r") BizAssetRelation relation);

    @Delete("DELETE FROM biz_asset_relation WHERE from_version_id = #{from} AND to_version_id = #{to} " +
            "AND relation_type = #{type}")
    int deleteOne(@Param("from") String from, @Param("to") String to, @Param("type") String type);

    @Select("SELECT from_version_id AS fromVersionId, to_version_id AS toVersionId, relation_type AS relationType, " +
            "created_by AS createdBy, created_at AS createdAt FROM biz_asset_relation " +
            "WHERE from_version_id = #{versionId} OR to_version_id = #{versionId} " +
            "ORDER BY created_at DESC")
    List<BizAssetRelation> findByVersionId(@Param("versionId") String versionId);
}
