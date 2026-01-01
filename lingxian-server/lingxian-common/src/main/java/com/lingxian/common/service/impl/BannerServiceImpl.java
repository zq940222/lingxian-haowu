package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Banner;
import com.lingxian.common.mapper.BannerMapper;
import com.lingxian.common.service.BannerService;
import org.springframework.stereotype.Service;

/**
 * 轮播图服务实现
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {
}
