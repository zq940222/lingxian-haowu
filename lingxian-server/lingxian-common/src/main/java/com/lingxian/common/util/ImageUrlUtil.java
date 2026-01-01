package com.lingxian.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingxian.common.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片URL处理工具类
 * 用于将数据库中存储的相对路径转换为带签名的完整URL
 */
@Slf4j
@Component
public class ImageUrlUtil {

    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public ImageUrlUtil(StorageService storageService, ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成单个图片的访问URL
     *
     * @param path 图片路径（相对路径或完整URL）
     * @return 带签名的访问URL
     */
    public String generateUrl(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return storageService.generateUrl(path);
    }

    /**
     * 生成多个图片的访问URL
     *
     * @param paths 图片路径列表
     * @return 带签名的访问URL列表
     */
    public List<String> generateUrls(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return paths;
        }
        return paths.stream()
                .map(this::generateUrl)
                .collect(Collectors.toList());
    }

    /**
     * 解析JSON格式的图片数组并生成访问URL
     *
     * @param imagesJson JSON格式的图片数组字符串，如 ["path1", "path2"]
     * @return 带签名URL的JSON字符串
     */
    public String generateUrlsFromJson(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return imagesJson;
        }
        try {
            List<String> paths = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            List<String> urls = generateUrls(paths);
            return objectMapper.writeValueAsString(urls);
        } catch (Exception e) {
            log.warn("解析图片JSON失败: {}", imagesJson, e);
            return imagesJson;
        }
    }

    /**
     * 解析JSON格式的图片数组并生成访问URL列表
     *
     * @param imagesJson JSON格式的图片数组字符串
     * @return URL列表
     */
    public List<String> parseAndGenerateUrls(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return null;
        }
        try {
            List<String> paths = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            return generateUrls(paths);
        } catch (Exception e) {
            log.warn("解析图片JSON失败: {}", imagesJson, e);
            return null;
        }
    }
}
