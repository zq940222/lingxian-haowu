package com.lingxian.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 存储服务接口
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 存储路径（相对路径）
     * @return 文件访问URL
     */
    String upload(MultipartFile file, String path);

    /**
     * 上传文件（自动生成路径）
     *
     * @param file 文件
     * @return 文件访问URL
     */
    String upload(MultipartFile file);

    /**
     * 上传字节数组
     *
     * @param bytes       字节数组
     * @param path        存储路径
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String upload(byte[] bytes, String path, String contentType);

    /**
     * 删除文件
     *
     * @param path 文件路径
     */
    void delete(String path);

    /**
     * 测试连接
     *
     * @return 是否连接成功
     */
    boolean testConnection();

    /**
     * 获取存储类型
     *
     * @return 存储类型
     */
    String getType();

    /**
     * 根据存储路径生成临时访问URL
     *
     * @param path 存储路径（相对路径或完整路径）
     * @return 带签名的临时访问URL
     */
    String generateUrl(String path);

    /**
     * 批量生成临时访问URL
     *
     * @param paths 存储路径列表
     * @return 带签名的临时访问URL列表
     */
    default java.util.List<String> generateUrls(java.util.List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return paths.stream().map(this::generateUrl).toList();
    }
}
