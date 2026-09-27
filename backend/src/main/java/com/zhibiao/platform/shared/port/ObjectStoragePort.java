package com.zhibiao.platform.shared.port;

import java.io.InputStream;

/**
 * 对象存储端口：S3 兼容实现（MinIO）封装在其后，保留替换边界。
 * 不把 MinIO 管理权限交给浏览器；不设置匿名读写。
 */
public interface ObjectStoragePort {

    void put(String bucket, String key, InputStream in, long size, String contentType);

    StorageStat stat(String bucket, String key);

    InputStream get(String bucket, String key);

    void remove(String bucket, String key);

    /** 短时签名下载地址；签名地址不作为永久字段存储 */
    String presignedGetUrl(String bucket, String key, int ttlSeconds);

    void ensureBucket(String bucket);

    record StorageStat(long size, String contentType, String etag) {
    }
}
