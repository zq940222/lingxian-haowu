package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.MerchantUser;
import com.lingxian.common.mapper.MerchantUserMapper;
import com.lingxian.common.service.MerchantUserService;
import org.springframework.stereotype.Service;

/**
 * 商户端用户Service实现
 */
@Service
public class MerchantUserServiceImpl extends ServiceImpl<MerchantUserMapper, MerchantUser> implements MerchantUserService {
}
