package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.ProductComment;
import com.lingxian.common.mapper.ProductCommentMapper;
import com.lingxian.common.service.ProductCommentService;
import org.springframework.stereotype.Service;

@Service
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> implements ProductCommentService {
}
