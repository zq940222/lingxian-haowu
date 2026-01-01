package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Review;
import com.lingxian.common.mapper.ReviewMapper;
import com.lingxian.common.service.ReviewService;
import org.springframework.stereotype.Service;

/**
 * 评价服务实现
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
}
