package com.lingxian.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lingxian.common.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树形结构
     */
    List<Category> getTreeList();
}
