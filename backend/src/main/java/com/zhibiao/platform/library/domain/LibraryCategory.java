package com.zhibiao.platform.library.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** library_category：分类树（V3），kind 区分素材/模板两套分类 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("library_category")
public class LibraryCategory extends BaseEntity {
    private String name;
    /** MATERIAL/TEMPLATE */
    private String kind;
    private String parentId;
    private Integer sort;
}
