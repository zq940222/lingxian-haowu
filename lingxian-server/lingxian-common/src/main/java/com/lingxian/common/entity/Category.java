package com.lingxian.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class Category extends BaseEntity {

    private Long parentId;
    private String name;
    private String icon;
    private String image;
    private Integer level;
    private Integer sort;
    private Integer status;

    // 非数据库字段
    @TableField(exist = false)
    private List<Category> children;
    @TableField(exist = false)
    private Integer productCount;
}
