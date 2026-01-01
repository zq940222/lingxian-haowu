package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Category;
import com.lingxian.common.mapper.CategoryMapper;
import com.lingxian.common.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getTreeList() {
        List<Category> allCategories = list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));

        return buildTree(allCategories, 0L);
    }

    private List<Category> buildTree(List<Category> categories, Long parentId) {
        List<Category> result = new ArrayList<>();
        for (Category category : categories) {
            if (parentId.equals(category.getParentId())) {
                category.setChildren(buildTree(categories, category.getId()));
                result.add(category);
            }
        }
        return result;
    }
}
