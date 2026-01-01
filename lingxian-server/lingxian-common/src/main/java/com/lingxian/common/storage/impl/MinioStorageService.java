package com.lingxian.common.storage.impl;

import com.lingxian.common.storage.StorageProperties;
import com.lingxian.common.storage.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO存储服务实现
 */
@Slf4j
public class MinioStorageService implements StorageService {

    private final StorageProperties properties;
    private MinioClient minioClient;

    public MinioStorageService(StorageProperties properties) {
        this.properties = properties;
        initClient();
    }

    private void initClient() {
        StorageProperties.MinioConfig config = properties.getMinio();
        if (config.getEndpoint() != null && config.getAccessKey() != null) {
            this.minioClient = MinioClient.builder()
                    .endpoint(config.getEndpoint())
                    .credentials(config.getAccessKey(), config.getSecretKey())
                    .build();

            // 确保bucket存在
            try {
                String bucketName = config.getBucketName();
                boolean bucketExists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(bucketName).build()
                );
                if (!bucketExists) {
                    minioClient.makeBucket(
                            MakeBucketArgs.builder().bucket(bucketName).build()
                    );
                    log.info("创建MinIO Bucket: {}", bucketName);
                }
            } catch (Exception e) {
                log.warn("检查/创建MinIO Bucket失败: {}", e.getMessage());
            }
        }
    }

    private MinioClient getClient() {
        if (minioClient == null) {
            initClient();
        }
        return minioClient;
    }

    @Override
    public String upload(MultipartFile file, String path) {
        try {
            StorageProperties.MinioConfig config = properties.getMinio();
            String objectKey = config.getPrefix() + path;

            InputStream inputStream = file.getInputStream();
            getClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回相对路径，不返回带签名的URL
            return objectKey;
        } catch (Exception e) {
            log.error("MinIO文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String path = datePath + "/" + fileName;

        return upload(file, path);
    }

    @Override
    public String upload(byte[] bytes, String path, String contentType) {
        try {
            StorageProperties.MinioConfig config = properties.getMinio();
            String objectKey = config.getPrefix() + path;

            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            getClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(objectKey)
                            .stream(inputStream, bytes.length, -1)
                            .contentType(contentType)
                            .build()
            );

            // 返回相对路径，不返回带签名的URL
            return objectKey;
        } catch (Exception e) {
            log.error("MinIO文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            StorageProperties.MinioConfig config = properties.getMinio();
            String objectKey = config.getPrefix() + path;
            getClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO文件删除失败", e);
        }
    }

    @Override
    public boolean testConnection() {
        try {
            StorageProperties.MinioConfig config = properties.getMinio();
            return getClient().bucketExists(
                    BucketExistsArgs.builder().bucket(config.getBucketName()).build()
            );
        } catch (Exception e) {
            log.error("MinIO连接测试失败", e);
            return false;
        }
    }

    @Override
    public String getType() {
        return "minio";
    }

    @Override
    public String generateUrl(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        // 检测无效的 blob URL（历史错误数据）
        // 例如: http://localhost:9000/lingxian/blob%3Ahttp%3A/localhost...
        if (path.contains("blob%3A") || path.contains("blob:")) {
            log.warn("检测到无效的blob URL，无法生成有效访问链接: {}", path);
            return null;
        }

        // 如果已经是完整URL（http开头），判断是否需要重新生成签名
        if (path.startsWith("http://") || path.startsWith("https://")) {
            // 如果是外部URL（如picsum.photos、placeholder.com），直接返回
            StorageProperties.MinioConfig config = properties.getMinio();
            String endpointHost = config.getEndpoint().replace("http://", "").replace("https://", "");
            if (!path.contains(endpointHost)) {
                return path;
            }
            // 如果是MinIO URL，提取objectKey重新生成签名
            try {
                String bucketPath = "/" + config.getBucketName() + "/";
                int bucketIndex = path.indexOf(bucketPath);
                if (bucketIndex > 0) {
                    String objectKey = path.substring(bucketIndex + bucketPath.length());
                    // 去掉查询参数
                    int queryIndex = objectKey.indexOf("?");
                    if (queryIndex > 0) {
                        objectKey = objectKey.substring(0, queryIndex);
                    }
                    // 检查 objectKey 是否有效（不应包含特殊编码）
                    if (objectKey.contains("%3A") || objectKey.contains(":")) {
                        log.warn("检测到无效的objectKey: {}", objectKey);
                        return null;
                    }
                    return generatePresignedUrl(objectKey);
                }
            } catch (Exception e) {
                log.warn("解析MinIO URL失败: {}", path);
            }
            return path;
        }

        // 相对路径，直接生成签名URL
        return generatePresignedUrl(path);
    }

    /**
     * 生成预签名的临时访问URL
     * @param objectKey 对象键
     * @return 带签名的临时访问URL（有效期1小时）
     */
    private String generatePresignedUrl(String objectKey) {
        StorageProperties.MinioConfig config = properties.getMinio();

        try {
            // 生成预签名URL，有效期1小时
            String presignedUrl = getClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(config.getBucketName())
                            .object(objectKey)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
            log.debug("生成预签名URL: {}", presignedUrl);
            return presignedUrl;
        } catch (Exception e) {
            log.error("生成预签名URL失败", e);
            // 降级到普通URL
            String endpoint = config.getEndpoint();
            if (!endpoint.endsWith("/")) {
                endpoint += "/";
            }
            return endpoint + config.getBucketName() + "/" + objectKey;
        }
    }
}
