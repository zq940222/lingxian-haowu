package com.lingxian.admin.controller;

import com.lingxian.common.result.Result;
import com.lingxian.common.storage.StorageService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/admin/upload")
@RequiredArgsConstructor
@Tag(name = "管理后台-文件上传", description = "文件上传相关接口")
public class AdminUploadController {

    private final StorageService storageService;
    private final ImageUrlUtil imageUrlUtil;

    @PostMapping("/image")
    @Operation(summary = "上传图片")
    public Result<Map<String, Object>> uploadImage(@RequestParam MultipartFile file) {
        log.info("上传图片: filename={}, size={}", file.getOriginalFilename(), file.getSize());

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.failed("文件名不能为空");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        if (!allowedTypes.contains(extension)) {
            return Result.failed("不支持的图片格式，仅支持: " + String.join(", ", allowedTypes));
        }

        // 校验文件大小 (最大5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.failed("图片大小不能超过5MB");
        }

        try {
            // 上传到存储服务，返回存储路径
            String storagePath = storageService.upload(file);
            // 生成可预览的完整URL
            String previewUrl = imageUrlUtil.generateUrl(storagePath);

            Map<String, Object> result = new HashMap<>();
            result.put("url", storagePath);  // 存储路径，用于保存到数据库
            result.put("previewUrl", previewUrl);  // 预览URL，用于前端显示
            result.put("fileName", originalFilename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());
            result.put("type", file.getContentType());

            return Result.success(result);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.failed("图片上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/images")
    @Operation(summary = "批量上传图片")
    public Result<List<Map<String, Object>>> uploadImages(@RequestParam MultipartFile[] files) {
        log.info("批量上传图片: count={}", files.length);

        if (files.length > 9) {
            return Result.failed("一次最多上传9张图片");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        List<String> allowedTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                continue;
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!allowedTypes.contains(extension)) {
                continue;
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                continue;
            }

            try {
                String fileUrl = storageService.upload(file);

                Map<String, Object> result = new HashMap<>();
                result.put("url", fileUrl);
                result.put("fileName", originalFilename);
                result.put("originalName", originalFilename);
                result.put("size", file.getSize());
                result.put("type", file.getContentType());
                results.add(result);
            } catch (Exception e) {
                log.error("批量上传图片失败: {}", originalFilename, e);
            }
        }

        return Result.success(results);
    }

    @PostMapping("/video")
    @Operation(summary = "上传视频")
    public Result<Map<String, Object>> uploadVideo(@RequestParam MultipartFile file) {
        log.info("上传视频: filename={}, size={}", file.getOriginalFilename(), file.getSize());

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.failed("文件名不能为空");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedTypes = Arrays.asList("mp4", "avi", "mov", "wmv");
        if (!allowedTypes.contains(extension)) {
            return Result.failed("不支持的视频格式，仅支持: " + String.join(", ", allowedTypes));
        }

        // 校验文件大小 (最大100MB)
        if (file.getSize() > 100 * 1024 * 1024) {
            return Result.failed("视频大小不能超过100MB");
        }

        try {
            String fileUrl = storageService.upload(file);

            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", originalFilename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());
            result.put("type", file.getContentType());

            return Result.success(result);
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return Result.failed("视频上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/file")
    @Operation(summary = "上传文件")
    public Result<Map<String, Object>> uploadFile(@RequestParam MultipartFile file) {
        log.info("上传文件: filename={}, size={}", file.getOriginalFilename(), file.getSize());

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.failed("文件名不能为空");
        }

        // 校验文件大小 (最大50MB)
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.failed("文件大小不能超过50MB");
        }

        try {
            String fileUrl = storageService.upload(file);

            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", originalFilename);
            result.put("originalName", originalFilename);
            result.put("size", file.getSize());
            result.put("type", file.getContentType());

            return Result.success(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.failed("文件上传失败: " + e.getMessage());
        }
    }
}
