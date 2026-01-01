package com.lingxian.common.storage.impl;

import com.lingxian.common.storage.StorageProperties;
import com.lingxian.common.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地存储服务实现
 */
@Slf4j
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    @Override
    public String upload(MultipartFile file, String path) {
        try {
            Path storagePath = Paths.get(properties.getLocal().getPath(), path);
            Files.createDirectories(storagePath.getParent());
            Files.write(storagePath, file.getBytes());

            String domain = properties.getLocal().getDomain();
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            return domain + "uploads/" + path;
        } catch (IOException e) {
            log.error("本地文件上传失败", e);
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
            Path storagePath = Paths.get(properties.getLocal().getPath(), path);
            Files.createDirectories(storagePath.getParent());
            Files.write(storagePath, bytes);

            String domain = properties.getLocal().getDomain();
            if (!domain.endsWith("/")) {
                domain += "/";
            }
            return domain + "uploads/" + path;
        } catch (IOException e) {
            log.error("本地文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path storagePath = Paths.get(properties.getLocal().getPath(), path);
            Files.deleteIfExists(storagePath);
        } catch (IOException e) {
            log.error("本地文件删除失败", e);
        }
    }

    @Override
    public boolean testConnection() {
        try {
            Path storagePath = Paths.get(properties.getLocal().getPath());
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
            return Files.isWritable(storagePath);
        } catch (IOException e) {
            log.error("本地存储测试失败", e);
            return false;
        }
    }

    @Override
    public String getType() {
        return "local";
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
        // 本地存储直接返回域名+路径
        String domain = properties.getLocal().getDomain();
        if (!domain.endsWith("/")) {
            domain += "/";
        }
        // 如果路径已经以 uploads/ 开头，不再重复添加
        if (path.startsWith("uploads/")) {
            return domain + path;
        }
        return domain + "uploads/" + path;
    }
}
