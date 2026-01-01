package com.lingxian.common.storage;

import com.lingxian.common.storage.impl.AliyunOssStorageService;
import com.lingxian.common.storage.impl.HuaweiObsStorageService;
import com.lingxian.common.storage.impl.LocalStorageService;
import com.lingxian.common.storage.impl.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 存储服务配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    private final StorageProperties storageProperties;

    @Bean
    @Primary
    public StorageService storageService() {
        String type = storageProperties.getType();
        log.info("初始化存储服务，类型: {}", type);

        return switch (type) {
            case "minio" -> {
                log.info("使用MinIO存储");
                yield new MinioStorageService(storageProperties);
            }
            case "aliyun" -> {
                log.info("使用阿里云OSS存储");
                yield new AliyunOssStorageService(storageProperties);
            }
            case "huawei" -> {
                log.info("使用华为云OBS存储");
                yield new HuaweiObsStorageService(storageProperties);
            }
            default -> {
                log.info("使用本地存储");
                yield new LocalStorageService(storageProperties);
            }
        };
    }

    /**
     * 存储服务工厂，用于根据类型动态获取存储服务
     */
    @Bean
    public StorageServiceFactory storageServiceFactory() {
        return new StorageServiceFactory(storageProperties);
    }
}
