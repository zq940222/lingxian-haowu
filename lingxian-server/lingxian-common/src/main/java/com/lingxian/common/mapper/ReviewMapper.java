package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价 Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
