package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.WithdrawAccount;
import com.lingxian.common.mapper.WithdrawAccountMapper;
import com.lingxian.common.service.WithdrawAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WithdrawAccountServiceImpl extends ServiceImpl<WithdrawAccountMapper, WithdrawAccount> implements WithdrawAccountService {

    @Override
    public List<WithdrawAccount> getByMerchantId(Long merchantId) {
        return list(new LambdaQueryWrapper<WithdrawAccount>()
                .eq(WithdrawAccount::getMerchantId, merchantId)
                .orderByDesc(WithdrawAccount::getIsDefault)
                .orderByDesc(WithdrawAccount::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefault(Long merchantId, Long accountId) {
        // 先取消所有默认
        update(new LambdaUpdateWrapper<WithdrawAccount>()
                .eq(WithdrawAccount::getMerchantId, merchantId)
                .set(WithdrawAccount::getIsDefault, 0));

        // 设置指定账户为默认
        return update(new LambdaUpdateWrapper<WithdrawAccount>()
                .eq(WithdrawAccount::getId, accountId)
                .eq(WithdrawAccount::getMerchantId, merchantId)
                .set(WithdrawAccount::getIsDefault, 1));
    }
}
