<template>
  <view class="container">
    <!-- 用户信息 -->
    <view class="user-header">
      <view class="user-info" @click="handleLogin">
        <image class="avatar" :src="userStore.avatar" mode="aspectFill" />
        <view class="info">
          <text class="nickname">{{ userStore.nickname }}</text>
          <text class="tip" v-if="!userStore.isLogin">点击登录</text>
        </view>
      </view>
      <view class="settings" @click="goSettings">
        <uni-icons type="gear" size="24" color="#fff" />
      </view>
    </view>

    <!-- 资产信息 -->
    <view class="assets-card">
      <view class="asset-item" @click="goPoints">
        <text class="value">{{ userStore.points }}</text>
        <text class="label">积分</text>
      </view>
      <view class="divider"></view>
      <view class="asset-item" @click="goCoupon">
        <text class="value">0</text>
        <text class="label">优惠券</text>
      </view>
      <view class="divider"></view>
      <view class="asset-item" @click="goBalance">
        <text class="value">0.00</text>
        <text class="label">余额</text>
      </view>
    </view>

    <!-- 订单入口 -->
    <view class="order-card">
      <view class="card-header" @click="goOrderList(-1)">
        <text class="title">我的订单</text>
        <view class="more">
          <text>全部订单</text>
          <uni-icons type="right" size="14" color="#999" />
        </view>
      </view>
      <view class="order-nav">
        <view class="nav-item" @click="goOrderList(0)">
          <view class="icon-wrap">
            <uni-icons type="wallet" size="28" color="#666" />
          </view>
          <text>待付款</text>
        </view>
        <view class="nav-item" @click="goOrderList(1)">
          <view class="icon-wrap">
            <uni-icons type="cart" size="28" color="#666" />
          </view>
          <text>待发货</text>
        </view>
        <view class="nav-item" @click="goOrderList(2)">
          <view class="icon-wrap">
            <uni-icons type="car" size="28" color="#666" />
          </view>
          <text>配送中</text>
        </view>
        <view class="nav-item" @click="goOrderList(3)">
          <view class="icon-wrap">
            <uni-icons type="checkbox" size="28" color="#666" />
          </view>
          <text>已完成</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goAddress">
        <uni-icons type="location" size="22" color="#666" />
        <text class="label">收货地址</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="menu-item" @click="goMyGroup">
        <uni-icons type="staff" size="22" color="#666" />
        <text class="label">我的拼团</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="menu-item" @click="goPoints">
        <uni-icons type="medal" size="22" color="#666" />
        <text class="label">我的积分</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="menu-item" @click="goHelp">
        <uni-icons type="help" size="22" color="#666" />
        <text class="label">帮助中心</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
      <view class="menu-item" @click="goAbout">
        <uni-icons type="info" size="22" color="#666" />
        <text class="label">关于我们</text>
        <uni-icons type="right" size="16" color="#ccc" />
      </view>
    </view>

    <!-- 签到提示 -->
    <view class="sign-tip" v-if="userStore.isLogin" @click="signIn">
      <uni-icons type="calendar" size="20" color="#ff9800" />
      <text>每日签到领积分</text>
      <uni-icons type="right" size="14" color="#999" />
    </view>
  </view>
</template>

<script setup>
import { useUserStore } from '@/store/user'
import { pointsApi } from '@/api'

const userStore = useUserStore()

// 处理登录
const handleLogin = async () => {
  if (userStore.isLogin) return

  try {
    await userStore.wxLogin()
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e || '登录失败', icon: 'none' })
  }
}

// 签到
const signIn = async () => {
  if (!userStore.isLogin) {
    return handleLogin()
  }

  try {
    const res = await pointsApi.signIn()
    if (res.code === 200) {
      uni.showToast({ title: `签到成功，+${res.data.points}积分`, icon: 'success' })
      userStore.fetchUserInfo()
    }
  } catch (e) {
    // 已签到或其他错误
  }
}

// 跳转设置
const goSettings = () => {
  uni.navigateTo({ url: '/pages/user/settings' })
}

// 跳转积分
const goPoints = () => {
  if (!userStore.isLogin) return handleLogin()
  uni.navigateTo({ url: '/pages/user/points' })
}

// 跳转优惠券
const goCoupon = () => {
  if (!userStore.isLogin) return handleLogin()
  uni.showToast({ title: '暂未开放', icon: 'none' })
}

// 跳转余额
const goBalance = () => {
  if (!userStore.isLogin) return handleLogin()
  uni.showToast({ title: '暂未开放', icon: 'none' })
}

// 跳转订单列表
const goOrderList = (status) => {
  if (!userStore.isLogin) return handleLogin()
  uni.navigateTo({ url: `/pages/order/list?status=${status}` })
}

// 跳转地址管理
const goAddress = () => {
  if (!userStore.isLogin) return handleLogin()
  uni.navigateTo({ url: '/pages/address/list' })
}

// 跳转我的拼团
const goMyGroup = () => {
  if (!userStore.isLogin) return handleLogin()
  uni.navigateTo({ url: '/pages/group/my' })
}

// 跳转帮助中心
const goHelp = () => {
  uni.showToast({ title: '暂未开放', icon: 'none' })
}

// 跳转关于我们
const goAbout = () => {
  uni.showToast({ title: '暂未开放', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 用户信息头部 */
.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 60rpx 30rpx 40rpx;
  background: linear-gradient(135deg, $primary-color, #36cfc9);
}

.user-info {
  display: flex;
  align-items: center;

  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
  }

  .info {
    margin-left: 24rpx;

    .nickname {
      font-size: 36rpx;
      color: #fff;
      font-weight: bold;
      display: block;
    }

    .tip {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 8rpx;
      display: block;
    }
  }
}

.settings {
  padding: 20rpx;
}

/* 资产卡片 */
.assets-card {
  display: flex;
  margin: -40rpx 20rpx 20rpx;
  padding: 30rpx 0;
  background-color: #fff;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow;

  .asset-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .value {
      font-size: 36rpx;
      color: #333;
      font-weight: bold;
    }

    .label {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }

  .divider {
    width: 1rpx;
    height: 60rpx;
    background-color: #f0f0f0;
    align-self: center;
  }
}

/* 订单卡片 */
.order-card {
  margin: 0 20rpx 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .title {
      font-size: 30rpx;
      color: #333;
      font-weight: bold;
    }

    .more {
      display: flex;
      align-items: center;
      font-size: 24rpx;
      color: #999;
    }
  }

  .order-nav {
    display: flex;
    padding: 30rpx 0;

    .nav-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .icon-wrap {
        width: 80rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;
        border-radius: 50%;
        margin-bottom: 12rpx;
      }

      text {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

/* 菜单卡片 */
.menu-card {
  margin: 0 20rpx 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 28rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      flex: 1;
      font-size: 28rpx;
      color: #333;
      margin-left: 20rpx;
    }
  }
}

/* 签到提示 */
.sign-tip {
  display: flex;
  align-items: center;
  margin: 0 20rpx;
  padding: 24rpx 30rpx;
  background-color: #fff8e6;
  border-radius: $border-radius-lg;

  text {
    flex: 1;
    font-size: 26rpx;
    color: #ff9800;
    margin-left: 12rpx;
  }
}
</style>
