package com.zhibiao.platform.shared.port;

import com.zhibiao.platform.shared.exception.BizException;
import com.zhibiao.platform.shared.util.Ids;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO（S3 兼容）对象存储实现。
 * 截至 2026-09-27，MinIO 社区仓库已归档且声明不再维护；保留既有选型不代表免除安全更新评估，
 * 本端口保留替换边界（可换其他 S3 兼容实现）。
 */
@Slf4j
@Component
@Profile("!it-mock-storage")
public class MinioObjectStorage implements ObjectStoragePort {

    private final MinioClient client;

    public MinioObjectStorage(@Value("${zhibiao.storage.endpoint}") String endpoint,
                              @Value("${zhibiao.storage.access-key}") String accessKey,
                              @Value("${zhibiao.storage.secret-key}") String secretKey) {
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            log.warn("对象存储凭证未配置（ZB_MINIO_ACCESS_KEY/ZB_MINIO_SECRET_KEY），上传下载将在调用时失败");
        }
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public void ensureBucket(String bucket) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("created bucket {}", bucket);
            }
        } catch (Exception e) {
            throw BizException.integration("对象存储初始化失败: " + rootMessage(e));
        }
    }

    @Override
    public void put(String bucket, String key, InputStream in, long size, String contentType) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, size, -1)
                    .contentType(contentType == null ? "application/octet-stream" : contentType)
                    .build());
        } catch (Exception e) {
            throw BizException.integration("对象存储写入失败: " + rootMessage(e));
        }
    }

    @Override
    public StorageStat stat(String bucket, String key) {
        try {
            StatObjectResponse r = client.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
            return new StorageStat(r.size(), r.contentType(), r.etag());
        } catch (Exception e) {
            throw BizException.integration("对象存储读取元数据失败: " + rootMessage(e));
        }
    }

    @Override
    public InputStream get(String bucket, String key) {
        try {
            return client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw BizException.integration("对象存储读取失败: " + rootMessage(e));
        }
    }

    @Override
    public void remove(String bucket, String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw BizException.integration("对象存储删除失败: " + rootMessage(e));
        }
    }

    @Override
    public String presignedGetUrl(String bucket, String key, int ttlSeconds) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(key)
                    .expiry(ttlSeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw BizException.integration("生成签名地址失败: " + rootMessage(e));
        }
    }

    private static String rootMessage(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }

    /** 生成含随机标识的服务端 object_key，不用用户文件名作为路径 */
    public static String buildKey(String prefix, String ext) {
        return prefix + "/" + Ids.uuid() + (ext.isBlank() ? "" : "." + ext);
    }
}
