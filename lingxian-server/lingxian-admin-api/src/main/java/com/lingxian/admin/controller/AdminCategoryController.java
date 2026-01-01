package com.lingxian.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.Category;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.CategoryService;
import com.lingxian.common.util.ImageUrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Tag(name = "管理后台-分类管理", description = "商品分类管理相关接口")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final ImageUrlUtil imageUrlUtil;

    @GetMapping
    @Operation(summary = "获取分类列表")
    public Result<PageResult<Category>> getCategoryList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "分类名称") @RequestParam(required = false) String name,
            @Parameter(description = "父级ID") @RequestParam(required = false) Long parentId) {
        log.info("获取分类列表: page={}, size={}, name={}, parentId={}", page, size, name, parentId);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Category::getName, name);
        }
        if (parentId != null) {
            wrapper.eq(Category::getParentId, parentId);
        }
        wrapper.orderByAsc(Category::getSort);
        wrapper.orderByAsc(Category::getId);

        Page<Category> pageResult = categoryService.page(new Page<>(page, size), wrapper);

        // 处理图片URL
        pageResult.getRecords().forEach(this::processCategoryImageUrls);

        return Result.success(PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords()));
    }

    @GetMapping("/tree")
    @Operation(summary = "获取分类树结构")
    public Result<List<Category>> getCategoryTree() {
        log.info("获取分类树结构");

        List<Category> tree = categoryService.getTreeList();
        // 递归处理图片URL
        processCategoryTreeImageUrls(tree);
        return Result.success(tree);
    }

    @PostMapping
    @Operation(summary = "创建分类")
    public Result<Category> createCategory(@RequestBody Category category) {
        log.info("创建分类: category={}", category);

        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryService.save(category);

        return Result.success(category);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        log.info("更新分类: id={}, category={}", id, category);

        Category existing = categoryService.getById(id);
        if (existing == null) {
            return Result.failed("分类不存在");
        }

        category.setId(id);
        category.setUpdateTime(LocalDateTime.now());

        // 如果icon是完整URL（http开头），说明没有重新上传，保持原值
        if (category.getIcon() != null && category.getIcon().startsWith("http")) {
            category.setIcon(existing.getIcon());
        }
        // 如果image是完整URL，同样保持原值
        if (category.getImage() != null && category.getImage().startsWith("http")) {
            category.setImage(existing.getImage());
        }

        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: id={}", id);

        // 检查是否有子分类
        long childCount = categoryService.count(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount > 0) {
            return Result.failed("该分类下存在子分类，无法删除");
        }

        categoryService.removeById(id);
        return Result.success();
    }

    @PutMapping("/sort")
    @Operation(summary = "更新分类排序")
    public Result<Void> updateCategorySort(@RequestBody List<Map<String, Object>> body) {
        log.info("更新分类排序: body={}", body);

        for (Map<String, Object> item : body) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sort = Integer.valueOf(item.get("sort").toString());

            Category category = new Category();
            category.setId(id);
            category.setSort(sort);
            category.setUpdateTime(LocalDateTime.now());
            categoryService.updateById(category);
        }

        return Result.success();
    }

    /**
     * 处理分类图片URL
     */
    private void processCategoryImageUrls(Category category) {
        category.setIcon(imageUrlUtil.generateUrl(category.getIcon()));
        category.setImage(imageUrlUtil.generateUrl(category.getImage()));
    }

    /**
     * 递归处理分类树图片URL
     */
    private void processCategoryTreeImageUrls(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }
        for (Category category : categories) {
            processCategoryImageUrls(category);
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                processCategoryTreeImageUrls(category.getChildren());
            }
        }
    }
}
