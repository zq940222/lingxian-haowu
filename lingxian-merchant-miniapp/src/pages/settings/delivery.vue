<template>
  <view class="container">
    <view class="form">
      <!-- 配送范围设置 -->
      <view class="form-section">
        <view class="section-title">配送范围</view>
        <view class="form-item">
          <text class="label">配送范围</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.deliveryRange" placeholder="0" />
            <text class="unit">公里</text>
          </view>
        </view>
        <view class="form-item switch-item">
          <text class="label">范围外可配送</text>
          <view class="switch-wrap">
            <text class="desc">超出范围加收配送费</text>
            <switch :checked="form.allowOutOfRange" @change="onAllowOutOfRangeChange" color="#1890ff" />
          </view>
        </view>
        <view class="form-item" v-if="form.allowOutOfRange">
          <text class="label">超出加收</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.extraFeePerKm" placeholder="0.00" />
            <text class="unit">元/公里</text>
          </view>
        </view>
      </view>

      <!-- 配送费设置 -->
      <view class="form-section">
        <view class="section-title">配送费用</view>
        <view class="form-item">
          <text class="label">基础配送费</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.deliveryFee" placeholder="0.00" />
            <text class="unit">元</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">起送金额</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.minOrderAmount" placeholder="0.00" />
            <text class="unit">元</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">免配送费</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.freeDeliveryAmount" placeholder="0.00" />
            <text class="unit">元起</text>
          </view>
        </view>
      </view>

      <!-- 配送时间设置 -->
      <view class="form-section">
        <view class="section-title">配送时间</view>
        <view class="form-item switch-item">
          <text class="label">预约配送</text>
          <switch :checked="form.allowReservation" @change="onAllowReservationChange" color="#1890ff" />
        </view>
        <view class="form-item" v-if="form.allowReservation">
          <text class="label">可预约天数</text>
          <view class="input-with-unit">
            <input type="number" v-model="form.reservationDays" placeholder="3" />
            <text class="unit">天</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">配送开始</text>
          <picker mode="time" :value="form.deliveryStartTime" @change="onDeliveryStartTimeChange">
            <view class="picker">
              <text>{{ form.deliveryStartTime || '请选择' }}</text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">配送结束</text>
          <picker mode="time" :value="form.deliveryEndTime" @change="onDeliveryEndTimeChange">
            <view class="picker">
              <text>{{ form.deliveryEndTime || '请选择' }}</text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">预计送达</text>
          <view class="input-with-unit">
            <input type="number" v-model="form.estimatedDeliveryTime" placeholder="30" />
            <text class="unit">分钟</text>
          </view>
        </view>
      </view>

      <!-- 配送员设置 -->
      <view class="form-section">
        <view class="section-title">配送员管理</view>
        <view class="form-item switch-item">
          <text class="label">自动分配配送员</text>
          <switch :checked="form.autoAssign" @change="onAutoAssignChange" color="#1890ff" />
        </view>
        <view class="form-item" @click="goDeliverymanList">
          <text class="label">配送员列表</text>
          <view class="value-wrap">
            <text class="value">{{ form.deliverymanCount }}人</text>
            <uni-icons type="right" size="16" color="#999" />
          </view>
        </view>
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="save-btn" @click="save">
      <text>保存设置</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { shopApi } from '@/api'

const form = ref({
  deliveryRange: '',
  allowOutOfRange: false,
  extraFeePerKm: '',
  deliveryFee: '',
  minOrderAmount: '',
  freeDeliveryAmount: '',
  allowReservation: true,
  reservationDays: '3',
  deliveryStartTime: '08:00',
  deliveryEndTime: '22:00',
  estimatedDeliveryTime: '30',
  autoAssign: false,
  deliverymanCount: 0
})

onLoad(() => {
  loadDeliverySettings()
})

// 加载配送设置
const loadDeliverySettings = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await shopApi.getDeliverySettings()
    if (res.code === 200 && res.data) {
      const data = res.data
      form.value = {
        deliveryRange: data.deliveryRange ? String(data.deliveryRange) : '',
        allowOutOfRange: data.allowOutOfRange || false,
        extraFeePerKm: data.extraFeePerKm ? String(data.extraFeePerKm) : '',
        deliveryFee: data.deliveryFee ? String(data.deliveryFee) : '',
        minOrderAmount: data.minOrderAmount ? String(data.minOrderAmount) : '',
        freeDeliveryAmount: data.freeDeliveryAmount ? String(data.freeDeliveryAmount) : '',
        allowReservation: data.allowReservation !== false,
        reservationDays: data.reservationDays ? String(data.reservationDays) : '3',
        deliveryStartTime: data.deliveryStartTime || '08:00',
        deliveryEndTime: data.deliveryEndTime || '22:00',
        estimatedDeliveryTime: data.estimatedDeliveryTime ? String(data.estimatedDeliveryTime) : '30',
        autoAssign: data.autoAssign || false,
        deliverymanCount: data.deliverymanCount || 0
      }
    }
  } catch (e) {
    console.error('加载配送设置失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 范围外可配送切换
const onAllowOutOfRangeChange = (e) => {
  form.value.allowOutOfRange = e.detail.value
}

// 预约配送切换
const onAllowReservationChange = (e) => {
  form.value.allowReservation = e.detail.value
}

// 配送开始时间
const onDeliveryStartTimeChange = (e) => {
  form.value.deliveryStartTime = e.detail.value
}

// 配送结束时间
const onDeliveryEndTimeChange = (e) => {
  form.value.deliveryEndTime = e.detail.value
}

// 自动分配切换
const onAutoAssignChange = (e) => {
  form.value.autoAssign = e.detail.value
}

// 配送员列表
const goDeliverymanList = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 保存
const save = async () => {
  try {
    uni.showLoading({ title: '保存中...' })

    const data = {
      deliveryRange: form.value.deliveryRange ? parseFloat(form.value.deliveryRange) : 0,
      allowOutOfRange: form.value.allowOutOfRange,
      extraFeePerKm: form.value.extraFeePerKm ? parseFloat(form.value.extraFeePerKm) : 0,
      deliveryFee: form.value.deliveryFee ? parseFloat(form.value.deliveryFee) : 0,
      minOrderAmount: form.value.minOrderAmount ? parseFloat(form.value.minOrderAmount) : 0,
      freeDeliveryAmount: form.value.freeDeliveryAmount ? parseFloat(form.value.freeDeliveryAmount) : 0,
      allowReservation: form.value.allowReservation,
      reservationDays: form.value.reservationDays ? parseInt(form.value.reservationDays) : 3,
      deliveryStartTime: form.value.deliveryStartTime,
      deliveryEndTime: form.value.deliveryEndTime,
      estimatedDeliveryTime: form.value.estimatedDeliveryTime ? parseInt(form.value.estimatedDeliveryTime) : 30,
      autoAssign: form.value.autoAssign
    }

    const res = await shopApi.updateDeliverySettings(data)
    if (res.code === 200) {
      uni.showToast({ title: '保存成功', icon: 'success' })
    }
  } catch (e) {
    console.error('保存失败', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
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
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .label {
    width: 200rpx;
    font-size: 28rpx;
    color: #333;
  }

  input {
    flex: 1;
    height: 60rpx;
    font-size: 28rpx;
    text-align: right;
  }

  .picker {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    text {
      font-size: 28rpx;
      color: #333;
    }
  }

  &.switch-item {
    justify-content: space-between;

    .switch-wrap {
      display: flex;
      align-items: center;

      .desc {
        font-size: 24rpx;
        color: #999;
        margin-right: 16rpx;
      }
    }
  }

  .input-with-unit {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    input {
      width: 200rpx;
    }

    .unit {
      font-size: 28rpx;
      color: #666;
      margin-left: 10rpx;
    }
  }

  .value-wrap {
    display: flex;
    align-items: center;

    .value {
      font-size: 28rpx;
      color: #666;
      margin-right: 10rpx;
    }
  }
}

/* 保存按钮 */
.save-btn {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: 40rpx;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  background: linear-gradient(135deg, $primary-color, #36cfc9);
  border-radius: 48rpx;
  margin-bottom: env(safe-area-inset-bottom);

  text {
    font-size: 32rpx;
    color: #fff;
  }
}
</style>
