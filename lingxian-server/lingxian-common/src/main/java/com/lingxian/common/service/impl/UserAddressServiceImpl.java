package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.UserAddress;
import com.lingxian.common.mapper.UserAddressMapper;
import com.lingxian.common.service.UserAddressService;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {
}
