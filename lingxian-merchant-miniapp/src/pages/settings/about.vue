<template>
  <view class="container">
    <!-- Logo和版本 -->
    <view class="header">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="app-name">铃鲜好物商家版</text>
      <text class="version">版本 {{ version }}</text>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <view class="menu-item" @click="checkUpdate">
        <text class="label">检查更新</text>
        <view class="value-wrap">
          <text class="value" v-if="hasNewVersion">有新版本</text>
          <uni-icons type="right" size="16" color="#999" />
        </view>
      </view>
      <view class="menu-item" @click="goUserAgreement">
        <text class="label">用户协议</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goPrivacyPolicy">
        <text class="label">隐私政策</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @click="goOpenSource">
        <text class="label">开源许可</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <!-- 关注我们 -->
    <view class="menu-section">
      <view class="section-title">关注我们</view>
      <view class="social-list">
        <view class="social-item" @click="copyWechat">
          <image src="/static/icons/wechat.png" mode="aspectFit" />
          <text>微信公众号</text>
        </view>
        <view class="social-item" @click="openWeibo">
          <image src="/static/icons/weibo.png" mode="aspectFit" />
          <text>官方微博</text>
        </view>
        <view class="social-item" @click="openWebsite">
          <image src="/static/icons/website.png" mode="aspectFit" />
          <text>官方网站</text>
        </view>
      </view>
    </view>

    <!-- 公司信息 -->
    <view class="company-info">
      <text>铃鲜好物科技有限公司</text>
      <text>客服热线：400-123-4567</text>
      <text>工作时间：9:00-22:00</text>
    </view>

    <!-- 版权信息 -->
    <view class="copyright">
      <text>Copyright © 2024 铃鲜好物</text>
      <text>All Rights Reserved</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const version = ref('1.0.0')
const hasNewVersion = ref(false)

onLoad(() => {
  getVersion()
})

// 获取版本号
const getVersion = () => {
  // #ifdef APP-PLUS
  plus.runtime.getProperty(plus.runtime.appid, (info) => {
    version.value = info.version
  })
  // #endif
}

// 检查更新
const checkUpdate = async () => {
  uni.showLoading({ title: '检查中...' })

  // 模拟检查更新
  setTimeout(() => {
    uni.hideLoading()
    if (hasNewVersion.value) {
      uni.showModal({
        title: '发现新版本',
        content: '新版本 v1.1.0 已发布，是否立即更新？',
        success: (res) => {
          if (res.confirm) {
            // 执行更新
            uni.showToast({ title: '开始下载...', icon: 'none' })
          }
        }
      })
    } else {
      uni.showToast({ title: '已是最新版本', icon: 'success' })
    }
  }, 1000)
}

// 用户协议
const goUserAgreement = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 隐私政策
const goPrivacyPolicy = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 开源许可
const goOpenSource = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 复制微信公众号
const copyWechat = () => {
  uni.setClipboardData({
    data: 'lingxianhaowu',
    success: () => {
      uni.showToast({ title: '已复制，请到微信搜索关注', icon: 'none' })
    }
  })
}

// 打开微博
const openWeibo = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 打开官网
const openWebsite = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

/* 头部 */
.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 60rpx;
  background-color: #fff;

  .logo {
    width: 160rpx;
    height: 160rpx;
    border-radius: 32rpx;
  }

  .app-name {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-top: 24rpx;
  }

  .version {
    font-size: 26rpx;
    color: #999;
    margin-top: 12rpx;
  }
}

/* 菜单 */
.menu-section {
  margin: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .section-title {
    padding: 24rpx 30rpx;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    border-bottom: 1rpx solid #f0f0f0;
  }
}

.menu-item {
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
  }

  .value-wrap {
    display: flex;
    align-items: center;

    .value {
      font-size: 26rpx;
      color: $danger-color;
      margin-right: 10rpx;
    }
  }
}

/* 社交媒体 */
.social-list {
  display: flex;
  justify-content: space-around;
  padding: 40rpx 0;

  .social-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    image {
      width: 80rpx;
      height: 80rpx;
    }

    text {
      font-size: 24rpx;
      color: #666;
      margin-top: 12rpx;
    }
  }
}

/* 公司信息 */
.company-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;

  text {
    font-size: 24rpx;
    color: #999;
    line-height: 2;
  }
}

/* 版权信息 */
.copyright {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;

  text {
    font-size: 22rpx;
    color: #ccc;
    line-height: 1.8;
  }
}
</style>
