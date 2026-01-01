package com.lingxian.merchant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxian.common.entity.*;
import com.lingxian.common.result.PageResult;
import com.lingxian.common.result.Result;
import com.lingxian.common.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/merchant/wallet")
@RequiredArgsConstructor
@Tag(name = "商户端-钱包管理", description = "商户钱包和提现管理接口")
public class MerchantWalletController {

    private final MerchantUserService merchantUserService;
    private final MerchantWalletService merchantWalletService;
    private final WithdrawAccountService withdrawAccountService;
    private final WithdrawRecordService withdrawRecordService;
    private final WalletRecordService walletRecordService;

    // 手续费率 0.6%
    private static final BigDecimal FEE_RATE = new BigDecimal("0.006");
    // 最低手续费 1元
    private static final BigDecimal MIN_FEE = new BigDecimal("1");
    // 最低提现金额 10元
    private static final BigDecimal MIN_WITHDRAW = new BigDecimal("10");

    @GetMapping("/balance")
    @Operation(summary = "获取钱包余额")
    public Result<Map<String, Object>> getBalance(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        MerchantWallet wallet = merchantWalletService.getOrCreateByMerchantId(merchantUser.getMerchantId());

        Map<String, Object> data = new HashMap<>();
        data.put("balance", wallet.getBalance().setScale(2, RoundingMode.HALF_UP).toString());
        data.put("totalIncome", wallet.getTotalIncome().setScale(2, RoundingMode.HALF_UP).toString());
        data.put("totalWithdraw", wallet.getTotalWithdraw().setScale(2, RoundingMode.HALF_UP).toString());
        data.put("pendingSettle", wallet.getPendingSettle().setScale(2, RoundingMode.HALF_UP).toString());

        return Result.success(data);
    }

    @GetMapping("/records")
    @Operation(summary = "获取收支明细")
    public Result<PageResult<Map<String, Object>>> getRecords(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer type) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Page<WalletRecord> pageData = walletRecordService.getByMerchantId(
                merchantUser.getMerchantId(), type, page, pageSize);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> records = new ArrayList<>();
        for (WalletRecord record : pageData.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("title", record.getTitle());
            item.put("type", record.getType());
            item.put("amount", record.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
            item.put("createTime", record.getCreateTime().format(formatter));
            records.add(item);
        }

        PageResult<Map<String, Object>> result = PageResult.of(
                pageData.getTotal(),
                (long) page,
                (long) pageSize,
                records
        );
        return Result.success(result);
    }

    @GetMapping("/accounts")
    @Operation(summary = "获取提现账户列表")
    public Result<List<Map<String, Object>>> getAccounts(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        List<WithdrawAccount> accounts = withdrawAccountService.getByMerchantId(merchantUser.getMerchantId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (WithdrawAccount account : accounts) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", account.getId());
            item.put("type", account.getType());
            item.put("name", account.getRealName());
            item.put("accountNo", maskAccountNo(account.getAccountNo(), account.getType()));
            item.put("isDefault", account.getIsDefault() == 1);
            if ("bank".equals(account.getType())) {
                item.put("bankName", account.getBankName());
            }
            result.add(item);
        }

        return Result.success(result);
    }

    @PostMapping("/accounts")
    @Operation(summary = "添加提现账户")
    public Result<Long> addAccount(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {

        log.info("添加提现账户: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        String type = (String) body.get("type");
        String realName = (String) body.get("realName");
        String accountNo = (String) body.get("accountNo");
        String bankName = (String) body.get("bankName");
        String bankBranch = (String) body.get("bankBranch");
        Boolean isDefault = (Boolean) body.getOrDefault("isDefault", false);

        if (realName == null || realName.trim().isEmpty()) {
            return Result.failed("请输入真实姓名");
        }
        if (accountNo == null || accountNo.trim().isEmpty()) {
            return Result.failed("请输入账号");
        }
        if ("bank".equals(type) && (bankName == null || bankName.trim().isEmpty())) {
            return Result.failed("请输入开户银行");
        }

        WithdrawAccount account = new WithdrawAccount();
        account.setMerchantId(merchantUser.getMerchantId());
        account.setType(type);
        account.setRealName(realName.trim());
        account.setAccountNo(accountNo.trim());
        account.setBankName(bankName != null ? bankName.trim() : null);
        account.setBankBranch(bankBranch != null ? bankBranch.trim() : null);
        account.setIsDefault(0);

        withdrawAccountService.save(account);

        // 如果设置为默认，或者是第一个账户，则设置为默认
        List<WithdrawAccount> existingAccounts = withdrawAccountService.getByMerchantId(merchantUser.getMerchantId());
        if (Boolean.TRUE.equals(isDefault) || existingAccounts.size() == 1) {
            withdrawAccountService.setDefault(merchantUser.getMerchantId(), account.getId());
        }

        log.info("添加提现账户成功: id={}, type={}, realName={}", account.getId(), type, realName);

        return Result.success(account.getId());
    }

    @DeleteMapping("/accounts/{id}")
    @Operation(summary = "删除提现账户")
    public Result<Void> removeAccount(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id) {

        log.info("删除提现账户: userId={}, accountId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        WithdrawAccount account = withdrawAccountService.getById(id);
        if (account == null || !account.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("账户不存在");
        }

        withdrawAccountService.removeById(id);
        return Result.success();
    }

    @PutMapping("/accounts/{id}/default")
    @Operation(summary = "设置默认提现账户")
    public Result<Void> setDefaultAccount(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id) {

        log.info("设置默认提现账户: userId={}, accountId={}", userId, id);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        WithdrawAccount account = withdrawAccountService.getById(id);
        if (account == null || !account.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("账户不存在");
        }

        withdrawAccountService.setDefault(merchantUser.getMerchantId(), id);
        return Result.success();
    }

    @PostMapping("/withdraw")
    @Operation(summary = "发起提现")
    public Result<Long> withdraw(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {

        log.info("发起提现: userId={}, body={}", userId, body);

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        BigDecimal amount = body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : null;
        Long accountId = body.get("accountId") != null ? Long.valueOf(body.get("accountId").toString()) : null;

        if (amount == null || amount.compareTo(MIN_WITHDRAW) < 0) {
            return Result.failed("最低提现金额" + MIN_WITHDRAW + "元");
        }
        if (accountId == null) {
            return Result.failed("请选择提现账户");
        }

        // 校验账户
        WithdrawAccount account = withdrawAccountService.getById(accountId);
        if (account == null || !account.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("提现账户不存在");
        }

        // 校验余额
        MerchantWallet wallet = merchantWalletService.getOrCreateByMerchantId(merchantUser.getMerchantId());
        if (wallet.getBalance().compareTo(amount) < 0) {
            return Result.failed("余额不足");
        }

        // 计算手续费
        BigDecimal fee = amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        if (fee.compareTo(MIN_FEE) < 0) {
            fee = MIN_FEE;
        }
        BigDecimal actualAmount = amount.subtract(fee);

        // 创建提现记录
        WithdrawRecord record = new WithdrawRecord();
        record.setMerchantId(merchantUser.getMerchantId());
        record.setAccountId(accountId);
        record.setAccountType(account.getType());
        record.setAccountName(getAccountName(account));
        record.setAccountNo(account.getAccountNo());
        record.setRealName(account.getRealName());
        record.setAmount(amount);
        record.setFee(fee);
        record.setActualAmount(actualAmount);
        record.setStatus(0); // 待审核

        withdrawRecordService.save(record);

        // 扣减余额
        merchantWalletService.subtractBalance(
                merchantUser.getMerchantId(),
                amount,
                "提现",
                "withdraw",
                record.getId()
        );

        // 更新累计提现
        wallet.setTotalWithdraw(wallet.getTotalWithdraw().add(amount));
        merchantWalletService.updateById(wallet);

        log.info("发起提现成功: recordId={}, amount={}, fee={}", record.getId(), amount, fee);

        return Result.success(record.getId());
    }

    @GetMapping("/withdraw/records")
    @Operation(summary = "获取提现记录")
    public Result<PageResult<Map<String, Object>>> getWithdrawRecords(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        Page<WithdrawRecord> pageData = withdrawRecordService.getByMerchantId(
                merchantUser.getMerchantId(), page, pageSize);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Map<String, Object>> records = new ArrayList<>();
        for (WithdrawRecord record : pageData.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("accountType", record.getAccountType());
            item.put("accountName", record.getAccountName());
            item.put("amount", record.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
            item.put("status", record.getStatus());
            item.put("createTime", record.getCreateTime().format(formatter));
            records.add(item);
        }

        PageResult<Map<String, Object>> result = PageResult.of(
                pageData.getTotal(),
                (long) page,
                (long) pageSize,
                records
        );
        return Result.success(result);
    }

    @GetMapping("/withdraw/{id}")
    @Operation(summary = "获取提现详情")
    public Result<Map<String, Object>> getWithdrawDetail(
            @RequestHeader(value = "X-Merchant-User-Id", required = false) Long userId,
            @PathVariable Long id) {

        if (userId == null) {
            return Result.failed("请先登录");
        }

        MerchantUser merchantUser = merchantUserService.getById(userId);
        if (merchantUser == null || merchantUser.getMerchantId() == null) {
            return Result.failed("商户信息不存在");
        }

        WithdrawRecord record = withdrawRecordService.getById(id);
        if (record == null || !record.getMerchantId().equals(merchantUser.getMerchantId())) {
            return Result.failed("提现记录不存在");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", record.getId());
        detail.put("accountType", record.getAccountType());
        detail.put("accountName", record.getAccountName());
        detail.put("accountNo", maskAccountNo(record.getAccountNo(), record.getAccountType()));
        detail.put("realName", record.getRealName());
        detail.put("amount", record.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
        detail.put("fee", record.getFee().setScale(2, RoundingMode.HALF_UP).toString());
        detail.put("actualAmount", record.getActualAmount().setScale(2, RoundingMode.HALF_UP).toString());
        detail.put("status", record.getStatus());
        detail.put("statusText", getStatusText(record.getStatus()));
        detail.put("createTime", record.getCreateTime().format(formatter));
        detail.put("auditTime", record.getAuditTime() != null ? record.getAuditTime().format(formatter) : null);
        detail.put("completeTime", record.getCompleteTime() != null ? record.getCompleteTime().format(formatter) : null);
        detail.put("remark", record.getRemark());

        return Result.success(detail);
    }

    /**
     * 获取账户显示名称
     */
    private String getAccountName(WithdrawAccount account) {
        switch (account.getType()) {
            case "wechat":
                return "微信钱包";
            case "alipay":
                return "支付宝";
            case "bank":
                return account.getBankName() != null ? account.getBankName() : "银行卡";
            default:
                return "未知账户";
        }
    }

    /**
     * 掩码账号
     */
    private String maskAccountNo(String accountNo, String type) {
        if (accountNo == null || accountNo.length() < 4) {
            return accountNo;
        }
        if ("bank".equals(type)) {
            // 银行卡号只显示后4位
            return "**** **** **** " + accountNo.substring(accountNo.length() - 4);
        } else {
            // 其他账号显示前2位和后2位
            if (accountNo.length() <= 4) {
                return accountNo;
            }
            return accountNo.substring(0, 2) + "****" + accountNo.substring(accountNo.length() - 2);
        }
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "处理中";
            case 2: return "已到账";
            case 3: return "已拒绝";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}
