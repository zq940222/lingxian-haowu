<template>
  <view class="container">
    <view class="form">
      <!-- 订单通知 -->
      <view class="form-section">
        <view class="section-title">订单通知</view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">新订单提醒</text>
            <text class="desc">有新订单时推送通知</text>
          </view>
          <switch :checked="settings.newOrder" @change="onChange('newOrder', $event)" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">订单取消提醒</text>
            <text class="desc">用户取消订单时推送通知</text>
          </view>
          <switch :checked="settings.orderCancel" @change="onChange('orderCancel', $event)" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">退款申请提醒</text>
            <text class="desc">用户申请退款时推送通知</text>
          </view>
          <switch :checked="settings.refundApply" @change="onChange('refundApply', $event)" color="#1890ff" />
        </view>
      </view>

      <!-- 提醒方式 -->
      <view class="form-section">
        <view class="section-title">提醒方式</view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">声音提醒</text>
            <text class="desc">收到通知时播放提示音</text>
          </view>
          <switch :checked="settings.sound" @change="onChange('sound', $event)" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">震动提醒</text>
            <text class="desc">收到通知时手机震动</text>
          </view>
          <switch :checked="settings.vibrate" @change="onChange('vibrate', $event)" color="#1890ff" />
        </view>
        <view class="form-item" @click="selectRingtone">
          <view class="item-left">
            <text class="label">提示音</text>
            <text class="desc">{{ settings.ringtoneName || '默认' }}</text>
          </view>
          <uni-icons type="right" size="16" color="#999" />
        </view>
      </view>

      <!-- 免打扰 -->
      <view class="form-section">
        <view class="section-title">免打扰设置</view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">开启免打扰</text>
            <text class="desc">在指定时段内不接收通知</text>
          </view>
          <switch :checked="settings.doNotDisturb" @change="onChange('doNotDisturb', $event)" color="#1890ff" />
        </view>
        <template v-if="settings.doNotDisturb">
          <view class="form-item">
            <text class="label">开始时间</text>
            <picker mode="time" :value="settings.dndStartTime" @change="onDndStartTimeChange">
              <view class="picker">
                <text>{{ settings.dndStartTime || '22:00' }}</text>
                <uni-icons type="right" size="16" color="#999" />
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="label">结束时间</text>
            <picker mode="time" :value="settings.dndEndTime" @change="onDndEndTimeChange">
              <view class="picker">
                <text>{{ settings.dndEndTime || '08:00' }}</text>
                <uni-icons type="right" size="16" color="#999" />
              </view>
            </picker>
          </view>
        </template>
      </view>

      <!-- 系统通知 -->
      <view class="form-section">
        <view class="section-title">系统通知</view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">系统公告</text>
            <text class="desc">接收平台公告和通知</text>
          </view>
          <switch :checked="settings.systemNotice" @change="onChange('systemNotice', $event)" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <view class="item-left">
            <text class="label">活动通知</text>
            <text class="desc">接收促销活动通知</text>
          </view>
          <switch :checked="settings.activityNotice" @change="onChange('activityNotice', $event)" color="#1890ff" />
        </view>
      </view>

      <!-- 通知权限 -->
      <view class="permission-section" v-if="!hasPermission">
        <uni-icons type="notification" size="40" color="#ff9800" />
        <view class="permission-info">
          <text class="title">未开启通知权限</text>
          <text class="desc">开启后才能接收订单提醒</text>
        </view>
        <view class="permission-btn" @click="openSystemSettings">
          <text>去开启</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { notificationApi } from '@/api'

const hasPermission = ref(true)
const settings = ref({
  newOrder: true,
  orderCancel: true,
  refundApply: true,
  sound: true,
  vibrate: true,
  ringtoneName: '默认',
  doNotDisturb: false,
  dndStartTime: '22:00',
  dndEndTime: '08:00',
  systemNotice: true,
  activityNotice: false
})

onLoad(() => {
  loadSettings()
})

onShow(() => {
  checkPermission()
})

// 检查通知权限
const checkPermission = () => {
  // #ifdef APP-PLUS
  const main = plus.android.runtimeMainActivity()
  const NotificationManagerCompat = plus.android.importClass('androidx.core.app.NotificationManagerCompat')
  hasPermission.value = NotificationManagerCompat.from(main).areNotificationsEnabled()
  // #endif
}

// 加载设置
const loadSettings = async () => {
  try {
    const res = await notificationApi.getSettings()
    if (res.code === 200 && res.data) {
      settings.value = { ...settings.value, ...res.data }
    }
  } catch (e) {
    console.error('加载通知设置失败', e)
  }
}

// 保存设置
const saveSettings = async () => {
  try {
    await notificationApi.updateSettings(settings.value)
  } catch (e) {
    console.error('保存设置失败', e)
  }
}

// 切换设置
const onChange = (key, e) => {
  settings.value[key] = e.detail.value
  saveSettings()
}

// 选择提示音
const selectRingtone = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 免打扰开始时间
const onDndStartTimeChange = (e) => {
  settings.value.dndStartTime = e.detail.value
  saveSettings()
}

// 免打扰结束时间
const onDndEndTimeChange = (e) => {
  settings.value.dndEndTime = e.detail.value
  saveSettings()
}

// 打开系统设置
const openSystemSettings = () => {
  // #ifdef APP-PLUS
  const Intent = plus.android.importClass('android.content.Intent')
  const Settings = plus.android.importClass('android.provider.Settings')
  const Uri = plus.android.importClass('android.net.Uri')
  const main = plus.android.runtimeMainActivity()

  const intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
  intent.putExtra(Settings.EXTRA_APP_PACKAGE, main.getPackageName())
  main.startActivity(intent)
  // #endif

  // #ifdef MP-WEIXIN
  uni.openSetting()
  // #endif
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
  }

  .item-left {
    flex: 1;

    .label {
      display: block;
    }

    .desc {
      font-size: 24rpx;
      color: #999;
      margin-top: 6rpx;
      display: block;
    }
  }

  .picker {
    display: flex;
    align-items: center;

    text {
      font-size: 28rpx;
      color: #666;
      margin-right: 10rpx;
    }
  }
}

.permission-section {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background-color: #fff7e6;
  border-radius: $border-radius-lg;
  margin-top: 20rpx;

  .permission-info {
    flex: 1;
    margin-left: 20rpx;

    .title {
      font-size: 28rpx;
      color: #333;
      display: block;
    }

    .desc {
      font-size: 24rpx;
      color: #999;
      margin-top: 4rpx;
      display: block;
    }
  }

  .permission-btn {
    padding: 12rpx 32rpx;
    background-color: #ff9800;
    border-radius: 32rpx;

    text {
      font-size: 26rpx;
      color: #fff;
    }
  }
}
</style>
