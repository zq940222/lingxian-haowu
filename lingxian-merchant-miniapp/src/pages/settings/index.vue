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

    <!-- 设置菜单 -->
    <view class="menu-section">
      <view class="menu-item" @click="goShopSettings">
        <view class="icon-wrap shop">
          <uni-icons type="shop" size="22" color="#fff" />
        </view>
        <text class="label">店铺设置</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goCategories">
        <view class="icon-wrap category">
          <uni-icons type="list" size="22" color="#fff" />
        </view>
        <text class="label">分类管理</text>
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

    <view class="menu-section">
      <view class="menu-item" @click="goAccount">
        <view class="icon-wrap account">
          <uni-icons type="person" size="22" color="#fff" />
        </view>
        <text class="label">账号安全</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goPrinter">
        <view class="icon-wrap printer">
          <uni-icons type="paperplane" size="22" color="#fff" />
        </view>
        <text class="label">打印设置</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goNotification">
        <view class="icon-wrap notification">
          <uni-icons type="notification" size="22" color="#fff" />
        </view>
        <text class="label">消息通知</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-item" @click="goHelp">
        <view class="icon-wrap help">
          <uni-icons type="help" size="22" color="#fff" />
        </view>
        <text class="label">帮助中心</text>
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
import { useMerchantStore } from '@/store/merchant'

const merchantStore = useMerchantStore()

// 店铺设置
const goShopSettings = () => {
  uni.navigateTo({ url: '/pages/settings/shop' })
}

// 分类管理
const goCategories = () => {
  uni.navigateTo({ url: '/pages/settings/categories' })
}

// 配送设置
const goDeliverySettings = () => {
  uni.navigateTo({ url: '/pages/settings/delivery' })
}

// 账号安全
const goAccount = () => {
  uni.navigateTo({ url: '/pages/settings/account' })
}

// 打印设置
const goPrinter = () => {
  uni.navigateTo({ url: '/pages/settings/printer' })
}

// 消息通知
const goNotification = () => {
  uni.navigateTo({ url: '/pages/settings/notification' })
}

// 帮助中心
const goHelp = () => {
  uni.navigateTo({ url: '/pages/settings/help' })
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

    &.shop { background-color: #1890ff; }
    &.category { background-color: #52c41a; }
    &.delivery { background-color: #fa8c16; }
    &.account { background-color: #722ed1; }
    &.printer { background-color: #eb2f96; }
    &.notification { background-color: #13c2c2; }
    &.help { background-color: #faad14; }
    &.about { background-color: #8c8c8c; }
  }

  .label {
    flex: 1;
    font-size: 28rpx;
    color: #333;
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
