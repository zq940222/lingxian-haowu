package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.Merchant;
import com.lingxian.common.mapper.MerchantMapper;
import com.lingxian.common.service.MerchantService;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {
}
