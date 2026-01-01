<template>
  <view class="container">
    <!-- 可提现余额 -->
    <view class="balance-info">
      <text class="label">可提现余额</text>
      <text class="amount">¥{{ balance }}</text>
    </view>

    <!-- 提现金额 -->
    <view class="amount-section">
      <view class="section-title">提现金额</view>
      <view class="amount-input">
        <text class="symbol">¥</text>
        <input
          type="digit"
          v-model="amount"
          placeholder="0.00"
          @input="onAmountInput"
        />
      </view>
      <view class="quick-amounts">
        <view
          class="quick-item"
          v-for="item in quickAmounts"
          :key="item"
          @click="selectAmount(item)"
        >¥{{ item }}</view>
        <view class="quick-item" @click="selectAll">全部提现</view>
      </view>
      <view class="tips">
        <text>最低提现金额：¥{{ minAmount }}，手续费：{{ feeRate }}%</text>
      </view>
    </view>

    <!-- 提现账户 -->
    <view class="account-section">
      <view class="section-title">提现到</view>
      <view class="account-selector" @click="showAccountPicker = true">
        <template v-if="selectedAccount">
          <view class="account-info">
            <view class="icon-wrap" :class="selectedAccount.type">
              <text v-if="selectedAccount.type === 'wechat'" class="icon-text">微</text>
              <text v-else-if="selectedAccount.type === 'alipay'" class="icon-text">支</text>
              <text v-else class="icon-text">银</text>
            </view>
            <view class="info">
              <text class="name">{{ selectedAccount.name }}</text>
              <text class="no">{{ selectedAccount.accountNo }}</text>
            </view>
          </view>
        </template>
        <template v-else>
          <text class="placeholder">请选择提现账户</text>
        </template>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="add-account" @click="goAddAccount">
        <uni-icons type="plusempty" size="16" color="#1890ff" />
        <text>添加提现账户</text>
      </view>
    </view>

    <!-- 提现说明 -->
    <view class="notice-section">
      <view class="section-title">提现说明</view>
      <view class="notice-list">
        <text>1. 提现申请提交后，预计1-3个工作日内到账</text>
        <text>2. 提现手续费为提现金额的{{ feeRate }}%，最低{{ minFee }}元</text>
        <text>3. 每日最多可提现3次</text>
        <text>4. 如有疑问请联系客服</text>
      </view>
    </view>

    <!-- 提现按钮 -->
    <view class="submit-section">
      <view class="fee-info" v-if="amount">
        <text>实际到账：¥{{ actualAmount }} (手续费：¥{{ fee }})</text>
      </view>
      <view class="submit-btn" :class="{ disabled: !canSubmit }" @click="handleWithdraw">
        确认提现
      </view>
    </view>

    <!-- 账户选择弹窗 -->
    <uni-popup ref="accountPopup" type="bottom" :show="showAccountPicker" @close="showAccountPicker = false">
      <view class="account-popup">
        <view class="popup-header">
          <text>选择提现账户</text>
          <uni-icons type="closeempty" size="20" color="#999" @click="showAccountPicker = false" />
        </view>
        <view class="account-list">
          <view
            class="account-option"
            v-for="item in accounts"
            :key="item.id"
            :class="{ active: selectedAccount && selectedAccount.id === item.id }"
            @click="selectAccountItem(item)"
          >
            <view class="icon-wrap" :class="item.type">
              <text v-if="item.type === 'wechat'" class="icon-text">微</text>
              <text v-else-if="item.type === 'alipay'" class="icon-text">支</text>
              <text v-else class="icon-text">银</text>
            </view>
            <view class="info">
              <text class="name">{{ item.name }}</text>
              <text class="no">{{ item.accountNo }}</text>
            </view>
            <uni-icons v-if="selectedAccount && selectedAccount.id === item.id" type="checkmarkempty" size="20" color="#1890ff" />
          </view>
          <view class="empty" v-if="accounts.length === 0">
            <text>暂无提现账户</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { walletApi } from '@/api'

const balance = ref('0.00')
const amount = ref('')
const accounts = ref([])
const selectedAccount = ref(null)
const showAccountPicker = ref(false)

const minAmount = ref(10)
const feeRate = ref(0.6)
const minFee = ref(1)
const quickAmounts = [50, 100, 200, 500]

// 计算手续费
const fee = computed(() => {
  if (!amount.value) return '0.00'
  const amountNum = parseFloat(amount.value)
  let feeNum = amountNum * feeRate.value / 100
  if (feeNum < minFee.value) feeNum = minFee.value
  return feeNum.toFixed(2)
})

// 实际到账金额
const actualAmount = computed(() => {
  if (!amount.value) return '0.00'
  const amountNum = parseFloat(amount.value)
  return (amountNum - parseFloat(fee.value)).toFixed(2)
})

// 是否可以提交
const canSubmit = computed(() => {
  if (!amount.value || !selectedAccount.value) return false
  const amountNum = parseFloat(amount.value)
  if (amountNum < minAmount.value) return false
  if (amountNum > parseFloat(balance.value)) return false
  return true
})

onLoad(() => {
  uni.setNavigationBarTitle({ title: '提现' })
})

onShow(() => {
  loadBalance()
  loadAccounts()
})

// 加载余额
const loadBalance = async () => {
  try {
    const res = await walletApi.getBalance()
    if (res.code === 200) {
      balance.value = res.data.balance || '0.00'
    }
  } catch (e) {
    console.error('获取余额失败', e)
  }
}

// 加载账户列表
const loadAccounts = async () => {
  try {
    const res = await walletApi.getAccounts()
    if (res.code === 200) {
      accounts.value = res.data || []
      // 自动选择默认账户
      const defaultAccount = accounts.value.find(a => a.isDefault)
      if (defaultAccount) {
        selectedAccount.value = defaultAccount
      } else if (accounts.value.length > 0) {
        selectedAccount.value = accounts.value[0]
      }
    }
  } catch (e) {
    console.error('加载账户失败', e)
  }
}

// 金额输入
const onAmountInput = (e) => {
  let value = e.detail.value
  // 限制两位小数
  if (value.includes('.')) {
    const parts = value.split('.')
    if (parts[1].length > 2) {
      value = parts[0] + '.' + parts[1].substring(0, 2)
    }
  }
  amount.value = value
}

// 选择快捷金额
const selectAmount = (value) => {
  amount.value = String(value)
}

// 全部提现
const selectAll = () => {
  amount.value = balance.value
}

// 选择账户
const selectAccountItem = (item) => {
  selectedAccount.value = item
  showAccountPicker.value = false
}

// 添加账户
const goAddAccount = () => {
  uni.navigateTo({ url: '/pages/wallet/account' })
}

// 提现
const handleWithdraw = async () => {
  if (!canSubmit.value) {
    if (!selectedAccount.value) {
      uni.showToast({ title: '请选择提现账户', icon: 'none' })
      return
    }
    const amountNum = parseFloat(amount.value)
    if (!amountNum || amountNum < minAmount.value) {
      uni.showToast({ title: `最低提现金额${minAmount.value}元`, icon: 'none' })
      return
    }
    if (amountNum > parseFloat(balance.value)) {
      uni.showToast({ title: '提现金额不能超过可用余额', icon: 'none' })
      return
    }
    return
  }

  try {
    await uni.showModal({
      title: '确认提现',
      content: `提现金额：¥${amount.value}\n手续费：¥${fee.value}\n实际到账：¥${actualAmount.value}`
    })

    uni.showLoading({ title: '提交中...' })
    const res = await walletApi.withdraw({
      amount: parseFloat(amount.value),
      accountId: selectedAccount.value.id
    })

    if (res.code === 200) {
      uni.showToast({ title: '提现申请已提交', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (e) {
    // 取消或失败
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 200rpx;
}

/* 余额信息 */
.balance-info {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  padding: 40rpx 30rpx;
  text-align: center;

  .label {
    display: block;
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 16rpx;
  }

  .amount {
    font-size: 56rpx;
    font-weight: bold;
    color: #fff;
  }
}

/* 提现金额 */
.amount-section {
  background-color: #fff;
  margin: 20rpx;
  border-radius: $border-radius-lg;
  padding: 30rpx;

  .section-title {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
  }

  .amount-input {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 2rpx solid #1890ff;

    .symbol {
      font-size: 48rpx;
      font-weight: bold;
      color: #333;
      margin-right: 10rpx;
    }

    input {
      flex: 1;
      font-size: 48rpx;
      font-weight: bold;
    }
  }

  .quick-amounts {
    display: flex;
    flex-wrap: wrap;
    margin-top: 20rpx;

    .quick-item {
      padding: 16rpx 24rpx;
      margin: 10rpx 10rpx 10rpx 0;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      font-size: 26rpx;
      color: #666;
    }
  }

  .tips {
    margin-top: 20rpx;

    text {
      font-size: 24rpx;
      color: #999;
    }
  }
}

/* 提现账户 */
.account-section {
  background-color: #fff;
  margin: 20rpx;
  border-radius: $border-radius-lg;
  padding: 30rpx;

  .section-title {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
  }

  .account-selector {
    display: flex;
    align-items: center;
    padding: 20rpx;
    background-color: #f5f5f5;
    border-radius: 12rpx;

    .account-info {
      flex: 1;
      display: flex;
      align-items: center;

      .icon-wrap {
        width: 64rpx;
        height: 64rpx;
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;

        &.wechat { background-color: #07c160; }
        &.alipay { background-color: #1677ff; }
        &.bank { background-color: #1890ff; }

        .icon-text {
          font-size: 28rpx;
          font-weight: bold;
          color: #fff;
        }
      }

      .info {
        .name {
          display: block;
          font-size: 28rpx;
          color: #333;
        }
        .no {
          font-size: 24rpx;
          color: #999;
        }
      }
    }

    .placeholder {
      flex: 1;
      font-size: 28rpx;
      color: #999;
    }
  }

  .add-account {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 20rpx;

    text {
      font-size: 26rpx;
      color: #1890ff;
      margin-left: 8rpx;
    }
  }
}

/* 提现说明 */
.notice-section {
  background-color: #fff;
  margin: 20rpx;
  border-radius: $border-radius-lg;
  padding: 30rpx;

  .section-title {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
  }

  .notice-list {
    text {
      display: block;
      font-size: 24rpx;
      color: #999;
      line-height: 1.8;
    }
  }
}

/* 提交按钮 */
.submit-section {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #fff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

  .fee-info {
    text-align: center;
    margin-bottom: 16rpx;

    text {
      font-size: 24rpx;
      color: #999;
    }
  }

  .submit-btn {
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    background-color: #1890ff;
    color: #fff;
    border-radius: 44rpx;
    font-size: 32rpx;

    &.disabled {
      background-color: #ccc;
    }
  }
}

/* 账户选择弹窗 */
.account-popup {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;
  max-height: 60vh;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .account-list {
    max-height: 50vh;
    overflow-y: auto;
  }

  .account-option {
    display: flex;
    align-items: center;
    padding: 24rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &.active {
      background-color: #e6f7ff;
    }

    .icon-wrap {
      width: 64rpx;
      height: 64rpx;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;

      &.wechat { background-color: #07c160; }
      &.alipay { background-color: #1677ff; }
      &.bank { background-color: #1890ff; }

      .icon-text {
        font-size: 28rpx;
        font-weight: bold;
        color: #fff;
      }
    }

    .info {
      flex: 1;

      .name {
        display: block;
        font-size: 28rpx;
        color: #333;
      }
      .no {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .empty {
    text-align: center;
    padding: 60rpx;

    text {
      font-size: 28rpx;
      color: #999;
    }
  }
}
</style>
