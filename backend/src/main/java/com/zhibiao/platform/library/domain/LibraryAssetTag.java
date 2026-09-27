package com.zhibiao.platform.library.domain;

import lombok.Data;

/**
 * library_asset_tag：复合主键 (asset_id, tag_id)，无 id 列；仅经自定义 SQL 读写。
 */
@Data
public class LibraryAssetTag {
    private String assetId;
    private String tagId;
}
