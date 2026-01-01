package com.lingxian.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxian.common.entity.MerchantUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户端用户Mapper
 */
@Mapper
public interface MerchantUserMapper extends BaseMapper<MerchantUser> {
}
