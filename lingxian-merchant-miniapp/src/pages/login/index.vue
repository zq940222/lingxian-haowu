<template>
  <view class="container">
    <view class="login-box">
      <!-- Logo -->
      <view class="logo-section">
        <image src="/static/images/logo.png" mode="aspectFit" />
        <text class="title">铃鲜好物商家版</text>
        <text class="subtitle">让生意更简单</text>
      </view>

      <!-- 登录表单 -->
      <view class="form-section">
        <view class="input-item">
          <uni-icons type="phone" size="22" color="#999" />
          <input
            type="number"
            v-model="phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </view>
        <view class="input-item">
          <uni-icons type="locked" size="22" color="#999" />
          <input
            type="password"
            v-model="password"
            placeholder="请输入密码"
          />
        </view>

        <view class="login-btn" @click="handleLogin">
          <text>登 录</text>
        </view>

        <view class="divider">
          <view class="line"></view>
          <text>或</text>
          <view class="line"></view>
        </view>

        <view class="wx-login-btn" @click="handleWxLogin">
          <uni-icons type="weixin" size="22" color="#07c160" />
          <text>微信一键登录</text>
        </view>
      </view>

      <!-- 协议 -->
      <view class="agreement">
        <view class="checkbox" @tap="toggleAgreed">
          <uni-icons
            :type="agreed ? 'checkbox-filled' : 'circle'"
            :color="agreed ? '#22c55e' : '#ccc'"
            size="20"
          />
        </view>
        <text @tap="toggleAgreed">我已阅读并同意</text>
        <text class="link" @tap.stop="showUserAgreement">《用户协议》</text>
        <text @tap="toggleAgreed">和</text>
        <text class="link" @tap.stop="showPrivacyPolicy">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useMerchantStore, VerifyStatus } from '@/store/merchant'

const merchantStore = useMerchantStore()

const phone = ref('')
const password = ref('')
const agreed = ref(false)

// 切换协议同意状态
const toggleAgreed = () => {
  agreed.value = !agreed.value
}

// 显示用户协议
const showUserAgreement = () => {
  uni.showModal({
    title: '用户协议',
    content: '用户协议内容...',
    showCancel: false
  })
}

// 显示隐私政策
const showPrivacyPolicy = () => {
  uni.showModal({
    title: '隐私政策',
    content: '隐私政策内容...',
    showCancel: false
  })
}

// 根据审核状态跳转
const navigateByStatus = (verifyStatus) => {
  if (verifyStatus === VerifyStatus.APPROVED) {
    // 审核通过，进入工作台
    uni.switchTab({ url: '/pages/index/index' })
  } else {
    // 未通过审核（未提交、待审核、被拒绝），进入申请状态页
    uni.redirectTo({ url: '/pages/apply/status' })
  }
}

// 账号密码登录
const handleLogin = async () => {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
    return
  }

  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  if (!password.value || password.value.length < 6) {
    uni.showToast({ title: '请输入6位以上密码', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '登录中...' })
    const data = await merchantStore.login(phone.value, password.value)
    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      navigateByStatus(data.verifyStatus)
    }, 1500)
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  }
}

// 微信登录
const handleWxLogin = async () => {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '登录中...' })
    const data = await merchantStore.wxLogin()
    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      navigateByStatus(data.verifyStatus)
    }, 1500)
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: e || '登录失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, $primary-color 0%, #f5f5f5 50%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx;
}

.login-box {
  width: 100%;
  background-color: #fff;
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  box-shadow: $box-shadow-lg;
}

/* Logo区域 */
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;

  image {
    width: 120rpx;
    height: 120rpx;
  }

  .title {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    margin-top: 20rpx;
  }

  .subtitle {
    font-size: 26rpx;
    color: #999;
    margin-top: 10rpx;
  }
}

/* 表单区域 */
.form-section {
  .input-item {
    display: flex;
    align-items: center;
    height: 100rpx;
    padding: 0 24rpx;
    background-color: #f8f8f8;
    border-radius: 50rpx;
    margin-bottom: 30rpx;

    input {
      flex: 1;
      height: 100%;
      margin-left: 20rpx;
      font-size: 28rpx;
    }
  }

  .login-btn {
    height: 100rpx;
    line-height: 100rpx;
    text-align: center;
    background: linear-gradient(135deg, $primary-color, #36cfc9);
    color: #fff;
    font-size: 32rpx;
    border-radius: 50rpx;
    margin-top: 20rpx;

    &:active {
      opacity: 0.8;
    }
  }

  .divider {
    display: flex;
    align-items: center;
    margin: 40rpx 0;

    .line {
      flex: 1;
      height: 1rpx;
      background-color: #e5e5e5;
    }

    text {
      padding: 0 20rpx;
      font-size: 26rpx;
      color: #999;
    }
  }

  .wx-login-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100rpx;
    border: 1rpx solid #07c160;
    border-radius: 50rpx;

    text {
      font-size: 28rpx;
      color: #07c160;
      margin-left: 12rpx;
    }

    &:active {
      background-color: #f0fff0;
    }
  }
}

/* 协议 */
.agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 40rpx;
  font-size: 26rpx;
  color: #666;
  padding: 20rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;

  .checkbox {
    margin-right: 10rpx;
    padding: 10rpx;
  }

  .link {
    color: #22c55e;
    padding: 10rpx 0;
    font-weight: 500;
  }
}
</style>
