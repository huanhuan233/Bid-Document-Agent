package com.zhibiao.platform.library.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhibiao.platform.shared.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** library_tag：标签（V3），kind 区分素材/模板 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("library_tag")
public class LibraryTag extends BaseEntity {
    private String name;
    /** MATERIAL/TEMPLATE */
    private String kind;
}
