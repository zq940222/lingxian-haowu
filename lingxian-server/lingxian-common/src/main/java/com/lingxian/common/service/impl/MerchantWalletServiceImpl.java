package com.lingxian.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lingxian.common.entity.MerchantWallet;
import com.lingxian.common.entity.WalletRecord;
import com.lingxian.common.mapper.MerchantWalletMapper;
import com.lingxian.common.service.MerchantWalletService;
import com.lingxian.common.service.WalletRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MerchantWalletServiceImpl extends ServiceImpl<MerchantWalletMapper, MerchantWallet> implements MerchantWalletService {

    private final WalletRecordService walletRecordService;

    @Override
    public MerchantWallet getOrCreateByMerchantId(Long merchantId) {
        MerchantWallet wallet = getOne(new LambdaQueryWrapper<MerchantWallet>()
                .eq(MerchantWallet::getMerchantId, merchantId));

        if (wallet == null) {
            wallet = new MerchantWallet();
            wallet.setMerchantId(merchantId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            wallet.setTotalWithdraw(BigDecimal.ZERO);
            wallet.setPendingSettle(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            save(wallet);
        }
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addBalance(Long merchantId, BigDecimal amount, String title, String refType, Long refId) {
        MerchantWallet wallet = getOrCreateByMerchantId(merchantId);
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        // 更新钱包余额
        boolean updated = update(new LambdaUpdateWrapper<MerchantWallet>()
                .eq(MerchantWallet::getId, wallet.getId())
                .set(MerchantWallet::getBalance, balanceAfter)
                .set(MerchantWallet::getTotalIncome, wallet.getTotalIncome().add(amount)));

        if (updated) {
            // 记录流水
            WalletRecord record = new WalletRecord();
            record.setMerchantId(merchantId);
            record.setType(1); // 收入
            record.setTitle(title);
            record.setAmount(amount);
            record.setBalanceBefore(balanceBefore);
            record.setBalanceAfter(balanceAfter);
            record.setRefType(refType);
            record.setRefId(refId);
            walletRecordService.save(record);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean subtractBalance(Long merchantId, BigDecimal amount, String title, String refType, Long refId) {
        MerchantWallet wallet = getOrCreateByMerchantId(merchantId);
        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            return false; // 余额不足
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // 更新钱包余额
        boolean updated = update(new LambdaUpdateWrapper<MerchantWallet>()
                .eq(MerchantWallet::getId, wallet.getId())
                .eq(MerchantWallet::getBalance, balanceBefore) // 乐观锁
                .set(MerchantWallet::getBalance, balanceAfter));

        if (updated) {
            // 记录流水
            WalletRecord record = new WalletRecord();
            record.setMerchantId(merchantId);
            record.setType(2); // 支出
            record.setTitle(title);
            record.setAmount(amount);
            record.setBalanceBefore(balanceBefore);
            record.setBalanceAfter(balanceAfter);
            record.setRefType(refType);
            record.setRefId(refId);
            walletRecordService.save(record);
        }

        return updated;
    }
}
