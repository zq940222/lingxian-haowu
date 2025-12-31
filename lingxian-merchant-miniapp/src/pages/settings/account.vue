<template>
  <view class="container">
    <view class="form">
      <!-- 账号信息 -->
      <view class="form-section">
        <view class="section-title">账号信息</view>
        <view class="form-item">
          <text class="label">手机号</text>
          <view class="value-wrap">
            <text class="value">{{ maskPhone(accountInfo.phone) }}</text>
            <view class="change-btn" @click="changePhone">更换</view>
          </view>
        </view>
        <view class="form-item" @click="changePassword">
          <text class="label">修改密码</text>
          <uni-icons type="right" size="16" color="#999" />
        </view>
      </view>

      <!-- 安全设置 -->
      <view class="form-section">
        <view class="section-title">安全设置</view>
        <view class="form-item switch-item">
          <text class="label">登录验证</text>
          <view class="switch-wrap">
            <text class="desc">每次登录需验证码</text>
            <switch :checked="accountInfo.loginVerify" @change="onLoginVerifyChange" color="#1890ff" />
          </view>
        </view>
        <view class="form-item switch-item">
          <text class="label">支付密码</text>
          <view class="switch-wrap">
            <text class="desc" v-if="accountInfo.hasPayPassword">已设置</text>
            <text class="desc" v-else>未设置</text>
            <view class="set-btn" @click="setPayPassword">
              {{ accountInfo.hasPayPassword ? '修改' : '设置' }}
            </view>
          </view>
        </view>
      </view>

      <!-- 登录设备 -->
      <view class="form-section">
        <view class="section-title">登录设备</view>
        <view class="device-item" v-for="(device, index) in accountInfo.devices" :key="index">
          <view class="device-info">
            <uni-icons :type="device.type === 'mobile' ? 'phone' : 'computer'" size="24" color="#1890ff" />
            <view class="device-detail">
              <text class="device-name">{{ device.name }}</text>
              <text class="device-time">{{ device.lastLoginTime }}</text>
            </view>
          </view>
          <view class="current-tag" v-if="device.isCurrent">当前</view>
          <view class="logout-btn" v-else @click="removeDevice(device)">移除</view>
        </view>
        <view class="empty-device" v-if="!accountInfo.devices || accountInfo.devices.length === 0">
          <text>暂无登录设备</text>
        </view>
      </view>

      <!-- 账号注销 -->
      <view class="form-section danger-section">
        <view class="form-item" @click="deleteAccount">
          <text class="label danger">注销账号</text>
          <uni-icons type="right" size="16" color="#ff4d4f" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { userApi } from '@/api'

const accountInfo = ref({
  phone: '',
  loginVerify: false,
  hasPayPassword: false,
  devices: []
})

onLoad(() => {
  loadAccountInfo()
})

// 加载账号信息
const loadAccountInfo = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await userApi.getAccountInfo()
    if (res.code === 200 && res.data) {
      accountInfo.value = res.data
    }
  } catch (e) {
    console.error('加载账号信息失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 手机号脱敏
const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 更换手机号
const changePhone = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 修改密码
const changePassword = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 登录验证切换
const onLoginVerifyChange = async (e) => {
  try {
    const res = await userApi.updateLoginVerify(e.detail.value)
    if (res.code === 200) {
      accountInfo.value.loginVerify = e.detail.value
      uni.showToast({ title: '设置成功', icon: 'success' })
    }
  } catch (e) {
    console.error('设置失败', e)
  }
}

// 设置支付密码
const setPayPassword = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 移除设备
const removeDevice = async (device) => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要移除该设备吗？移除后该设备需重新登录'
    })

    const res = await userApi.removeDevice(device.id)
    if (res.code === 200) {
      uni.showToast({ title: '移除成功', icon: 'success' })
      loadAccountInfo()
    }
  } catch (e) {
    // 取消
  }
}

// 注销账号
const deleteAccount = async () => {
  try {
    await uni.showModal({
      title: '警告',
      content: '注销账号后，所有数据将被删除且无法恢复，确定要注销吗？',
      confirmColor: '#ff4d4f'
    })

    await uni.showModal({
      title: '再次确认',
      content: '请再次确认注销账号',
      confirmText: '确定注销',
      confirmColor: '#ff4d4f'
    })

    uni.showToast({ title: '功能开发中', icon: 'none' })
  } catch (e) {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.form {
  padding: 20rpx;
}

.form-section {
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
  overflow: hidden;

  .section-title {
    padding: 24rpx 30rpx;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    border-bottom: 1rpx solid #f0f0f0;
  }

  &.danger-section {
    margin-top: 40rpx;
  }
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .label {
    font-size: 28rpx;
    color: #333;

    &.danger {
      color: #ff4d4f;
    }
  }

  &.switch-item {
    .switch-wrap {
      display: flex;
      align-items: center;

      .desc {
        font-size: 24rpx;
        color: #999;
        margin-right: 16rpx;
      }

      .set-btn {
        font-size: 26rpx;
        color: $primary-color;
        padding: 8rpx 20rpx;
        border: 1rpx solid $primary-color;
        border-radius: 20rpx;
      }
    }
  }

  .value-wrap {
    display: flex;
    align-items: center;

    .value {
      font-size: 28rpx;
      color: #666;
      margin-right: 16rpx;
    }

    .change-btn {
      font-size: 26rpx;
      color: $primary-color;
      padding: 8rpx 20rpx;
      border: 1rpx solid $primary-color;
      border-radius: 20rpx;
    }
  }
}

.device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .device-info {
    display: flex;
    align-items: center;

    .device-detail {
      margin-left: 20rpx;

      .device-name {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .device-time {
        font-size: 24rpx;
        color: #999;
        margin-top: 6rpx;
        display: block;
      }
    }
  }

  .current-tag {
    font-size: 24rpx;
    color: $success-color;
    padding: 6rpx 16rpx;
    background-color: rgba(82, 196, 26, 0.1);
    border-radius: 16rpx;
  }

  .logout-btn {
    font-size: 24rpx;
    color: #ff4d4f;
    padding: 6rpx 16rpx;
    border: 1rpx solid #ff4d4f;
    border-radius: 16rpx;
  }
}

.empty-device {
  padding: 40rpx;
  text-align: center;

  text {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
