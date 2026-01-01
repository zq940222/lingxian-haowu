package com.lingxian.common.storage;

import com.lingxian.common.storage.impl.AliyunOssStorageService;
import com.lingxian.common.storage.impl.HuaweiObsStorageService;
import com.lingxian.common.storage.impl.LocalStorageService;
import com.lingxian.common.storage.impl.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 存储服务工厂
 * 用于根据类型动态获取存储服务实例
 */
@Slf4j
@RequiredArgsConstructor
public class StorageServiceFactory {

    private final StorageProperties storageProperties;

    /**
     * 根据类型获取存储服务
     *
     * @param type 存储类型
     * @return 存储服务实例
     */
    public StorageService getStorageService(String type) {
        return switch (type) {
            case "minio" -> new MinioStorageService(storageProperties);
            case "aliyun" -> new AliyunOssStorageService(storageProperties);
            case "huawei" -> new HuaweiObsStorageService(storageProperties);
            default -> new LocalStorageService(storageProperties);
        };
    }

    /**
     * 获取当前配置的存储服务
     *
     * @return 存储服务实例
     */
    public StorageService getCurrentStorageService() {
        return getStorageService(storageProperties.getType());
    }

    /**
     * 测试指定类型的存储连接
     *
     * @param type 存储类型
     * @return 是否连接成功
     */
    public boolean testConnection(String type) {
        try {
            StorageService service = getStorageService(type);
            return service.testConnection();
        } catch (Exception e) {
            log.error("存储连接测试失败，类型: {}", type, e);
            return false;
        }
    }
}
