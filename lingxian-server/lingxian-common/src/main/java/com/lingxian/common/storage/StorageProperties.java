package com.lingxian.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 存储类型：local-本地存储, minio-MinIO, aliyun-阿里云OSS, huawei-华为云OBS
     */
    private String type = "local";

    /**
     * 本地存储配置
     */
    private LocalConfig local = new LocalConfig();

    /**
     * MinIO配置
     */
    private MinioConfig minio = new MinioConfig();

    /**
     * 阿里云OSS配置
     */
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 华为云OBS配置
     */
    private HuaweiConfig huawei = new HuaweiConfig();

    @Data
    public static class LocalConfig {
        /**
         * 访问域名
         */
        private String domain = "http://localhost:8087";
        /**
         * 存储路径
         */
        private String path = "./uploads";
    }

    @Data
    public static class MinioConfig {
        /**
         * Endpoint
         */
        private String endpoint = "http://localhost:9000";
        /**
         * Access Key
         */
        private String accessKey;
        /**
         * Secret Key
         */
        private String secretKey;
        /**
         * Bucket名称
         */
        private String bucketName = "lingxian";
        /**
         * 访问域名（可选，用于CDN加速）
         */
        private String domain;
        /**
         * 存储目录前缀
         */
        private String prefix = "uploads/";
    }

    @Data
    public static class AliyunConfig {
        /**
         * Endpoint
         */
        private String endpoint;
        /**
         * AccessKey ID
         */
        private String accessKeyId;
        /**
         * AccessKey Secret
         */
        private String accessKeySecret;
        /**
         * Bucket名称
         */
        private String bucketName;
        /**
         * 访问域名（可选，用于CDN加速）
         */
        private String domain;
        /**
         * 存储目录前缀
         */
        private String prefix = "uploads/";
    }

    @Data
    public static class HuaweiConfig {
        /**
         * Endpoint
         */
        private String endpoint;
        /**
         * Access Key ID
         */
        private String accessKeyId;
        /**
         * Secret Access Key
         */
        private String secretAccessKey;
        /**
         * Bucket名称
         */
        private String bucketName;
        /**
         * 访问域名（可选，用于CDN加速）
         */
        private String domain;
        /**
         * 存储目录前缀
         */
        private String prefix = "uploads/";
    }
}
