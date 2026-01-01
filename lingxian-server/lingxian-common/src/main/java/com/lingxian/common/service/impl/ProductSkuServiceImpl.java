package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.ProductSku;
import com.lingxian.common.mapper.ProductSkuMapper;
import com.lingxian.common.service.ProductSkuService;
import org.springframework.stereotype.Service;

@Service
public class ProductSkuServiceImpl extends ServiceImpl<ProductSkuMapper, ProductSku> implements ProductSkuService {
}
