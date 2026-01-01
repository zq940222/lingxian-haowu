<template>
  <view class="container">
    <!-- 商户信息 -->
    <view class="profile-card" @click="goShopSettings">
      <image class="avatar" :src="merchantStore.shopLogo" mode="aspectFill" />
      <view class="info">
        <text class="name">{{ merchantStore.shopName }}</text>
        <text class="status" :class="{ open: merchantStore.shopStatus === 1 }">
          {{ merchantStore.shopStatus === 1 ? '营业中' : '休息中' }}
        </text>
      </view>
      <uni-icons type="right" size="18" color="#999" />
    </view>

    <!-- 资金管理 -->
    <view class="menu-section">
      <view class="section-title">资金管理</view>
      <view class="menu-item" @click="goWallet">
        <view class="icon-wrap wallet">
          <uni-icons type="wallet" size="22" color="#fff" />
        </view>
        <text class="label">我的钱包</text>
        <text class="balance">¥{{ balance }}</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goWithdraw">
        <view class="icon-wrap withdraw">
          <uni-icons type="redo" size="22" color="#fff" />
        </view>
        <text class="label">提现</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goWithdrawAccount">
        <view class="icon-wrap bank">
          <uni-icons type="creditcard" size="22" color="#fff" />
        </view>
        <text class="label">提现账户</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <!-- 店铺设置 -->
    <view class="menu-section">
      <view class="section-title">店铺设置</view>
      <view class="menu-item" @click="goShopSettings">
        <view class="icon-wrap shop">
          <uni-icons type="shop" size="22" color="#fff" />
        </view>
        <text class="label">店铺设置</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goDeliverySettings">
        <view class="icon-wrap delivery">
          <uni-icons type="car" size="22" color="#fff" />
        </view>
        <text class="label">配送设置</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <!-- 账号与其他 -->
    <view class="menu-section">
      <view class="section-title">其他</view>
      <view class="menu-item" @click="goAccount">
        <view class="icon-wrap account">
          <uni-icons type="person" size="22" color="#fff" />
        </view>
        <text class="label">账号安全</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goAbout">
        <view class="icon-wrap about">
          <uni-icons type="info" size="22" color="#fff" />
        </view>
        <text class="label">关于我们</text>
        <text class="version">v1.0.0</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" @click="handleLogout">
      <text>退出登录</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useMerchantStore, VerifyStatus } from '@/store/merchant'
import { walletApi } from '@/api'

const merchantStore = useMerchantStore()
const balance = ref('0.00')

onShow(async () => {
  // 检查登录状态
  if (!merchantStore.checkLoginStatus()) {
    return
  }

  // 从服务器获取最新的审核状态
  try {
    const statusData = await merchantStore.fetchApplyStatus()
    if (statusData.verifyStatus !== VerifyStatus.APPROVED) {
      uni.reLaunch({ url: '/pages/apply/status' })
      return
    }
  } catch (e) {
    if (merchantStore.verifyStatus !== VerifyStatus.APPROVED) {
      uni.reLaunch({ url: '/pages/apply/status' })
      return
    }
  }

  // 获取钱包余额
  loadBalance()
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

// 我的钱包
const goWallet = () => {
  uni.navigateTo({ url: '/pages/wallet/index' })
}

// 提现
const goWithdraw = () => {
  uni.navigateTo({ url: '/pages/wallet/withdraw' })
}

// 提现账户
const goWithdrawAccount = () => {
  uni.navigateTo({ url: '/pages/wallet/account' })
}

// 店铺设置
const goShopSettings = () => {
  uni.navigateTo({ url: '/pages/settings/shop' })
}

// 配送设置
const goDeliverySettings = () => {
  uni.navigateTo({ url: '/pages/settings/delivery' })
}

// 账号安全
const goAccount = () => {
  uni.navigateTo({ url: '/pages/settings/account' })
}

// 关于我们
const goAbout = () => {
  uni.navigateTo({ url: '/pages/settings/about' })
}

// 退出登录
const handleLogout = async () => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要退出登录吗？'
    })
    merchantStore.logout()
  } catch (e) {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

/* 商户信息卡片 */
.profile-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;

  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 16rpx;
    margin-right: 24rpx;
  }

  .info {
    flex: 1;

    .name {
      font-size: 34rpx;
      font-weight: bold;
      color: #333;
      display: block;
    }

    .status {
      display: inline-block;
      font-size: 24rpx;
      color: #ff4d4f;
      background-color: rgba(255, 77, 79, 0.1);
      padding: 6rpx 16rpx;
      border-radius: 20rpx;
      margin-top: 12rpx;

      &.open {
        color: $success-color;
        background-color: rgba(82, 196, 26, 0.1);
      }
    }
  }
}

/* 菜单区块 */
.menu-section {
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
  overflow: hidden;

  .section-title {
    padding: 24rpx 30rpx 12rpx;
    font-size: 26rpx;
    color: #999;
  }
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .icon-wrap {
    width: 64rpx;
    height: 64rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;

    &.wallet { background-color: #ff9800; }
    &.withdraw { background-color: #4caf50; }
    &.bank { background-color: #2196f3; }
    &.shop { background-color: #1890ff; }
    &.delivery { background-color: #fa8c16; }
    &.account { background-color: #722ed1; }
    &.about { background-color: #8c8c8c; }
  }

  .label {
    flex: 1;
    font-size: 28rpx;
    color: #333;
  }

  .balance {
    font-size: 28rpx;
    color: #ff9800;
    font-weight: bold;
    margin-right: 16rpx;
  }

  .version {
    font-size: 24rpx;
    color: #999;
    margin-right: 16rpx;
  }
}

/* 退出按钮 */
.logout-btn {
  margin-top: 60rpx;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  background-color: #fff;
  border-radius: $border-radius-lg;

  text {
    font-size: 32rpx;
    color: $danger-color;
  }
}
</style>
