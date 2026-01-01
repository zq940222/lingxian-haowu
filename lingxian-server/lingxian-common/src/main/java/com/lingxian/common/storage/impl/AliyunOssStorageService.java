package com.lingxian.common.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.lingxian.common.storage.StorageProperties;
import com.lingxian.common.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS存储服务实现
 */
@Slf4j
public class AliyunOssStorageService implements StorageService {

    private final StorageProperties properties;
    private OSS ossClient;

    public AliyunOssStorageService(StorageProperties properties) {
        this.properties = properties;
        initClient();
    }

    private void initClient() {
        StorageProperties.AliyunConfig config = properties.getAliyun();
        if (config.getEndpoint() != null && config.getAccessKeyId() != null) {
            this.ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKeyId(),
                    config.getAccessKeySecret()
            );
        }
    }

    private OSS getClient() {
        if (ossClient == null) {
            initClient();
        }
        return ossClient;
    }

    @Override
    public String upload(MultipartFile file, String path) {
        try {
            StorageProperties.AliyunConfig config = properties.getAliyun();
            String objectKey = config.getPrefix() + path;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            getClient().putObject(config.getBucketName(), objectKey, file.getInputStream(), metadata);

            // 返回相对路径
            return objectKey;
        } catch (IOException e) {
            log.error("阿里云OSS文件上传失败", e);
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
        StorageProperties.AliyunConfig config = properties.getAliyun();
        String objectKey = config.getPrefix() + path;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(bytes.length);

        getClient().putObject(config.getBucketName(), objectKey, new ByteArrayInputStream(bytes), metadata);

        // 返回相对路径
        return objectKey;
    }

    @Override
    public void delete(String path) {
        try {
            StorageProperties.AliyunConfig config = properties.getAliyun();
            String objectKey = config.getPrefix() + path;
            getClient().deleteObject(config.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("阿里云OSS文件删除失败", e);
        }
    }

    @Override
    public boolean testConnection() {
        try {
            StorageProperties.AliyunConfig config = properties.getAliyun();
            return getClient().doesBucketExist(config.getBucketName());
        } catch (Exception e) {
            log.error("阿里云OSS连接测试失败", e);
            return false;
        }
    }

    @Override
    public String getType() {
        return "aliyun";
    }

    @Override
    public String generateUrl(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        // 如果已经是完整URL，直接返回
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        // 生成完整URL
        StorageProperties.AliyunConfig config = properties.getAliyun();
        String domain = config.getDomain();

        if (domain != null && !domain.isEmpty()) {
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            return domain + path;
        }

        // 使用默认域名
        return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + path;
    }

    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }
}
