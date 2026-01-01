<template>
  <view class="container">
    <view class="form">
      <!-- 店铺头像 -->
      <view class="form-section">
        <view class="form-item avatar-item" @click="changeAvatar">
          <text class="label">店铺头像</text>
          <view class="avatar-wrap">
            <image :src="form.shopLogo" mode="aspectFill" />
            <uni-icons type="camera" size="16" color="#fff" />
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>
        <view class="form-item">
          <text class="label">店铺名称</text>
          <input type="text" v-model="form.shopName" placeholder="请输入店铺名称" />
        </view>
        <view class="form-item">
          <text class="label">联系电话</text>
          <input type="tel" v-model="form.phone" placeholder="请输入联系电话" />
        </view>
        <view class="form-item textarea-item">
          <text class="label">店铺公告</text>
          <textarea v-model="form.notice" placeholder="输入店铺公告，会展示在用户端首页" maxlength="200" />
        </view>
        <view class="form-item textarea-item">
          <text class="label">店铺地址</text>
          <view class="address-input" @click="chooseLocation">
            <text v-if="form.address">{{ form.address }}</text>
            <text v-else class="placeholder">点击选择地址</text>
            <uni-icons type="location" size="18" color="#1890ff" />
          </view>
        </view>
      </view>

      <!-- 营业设置 -->
      <view class="form-section">
        <view class="section-title">营业设置</view>
        <view class="form-item switch-item">
          <text class="label">营业状态</text>
          <view class="switch-wrap">
            <text class="status-text">{{ form.shopStatus === 1 ? '营业中' : '休息中' }}</text>
            <switch :checked="form.shopStatus === 1" @change="onStatusChange" color="#1890ff" />
          </view>
        </view>
        <view class="form-item">
          <text class="label">营业开始</text>
          <picker mode="time" :value="form.openTime" @change="onOpenTimeChange">
            <view class="picker">
              <text>{{ form.openTime || '请选择' }}</text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">营业结束</text>
          <picker mode="time" :value="form.closeTime" @change="onCloseTimeChange">
            <view class="picker">
              <text>{{ form.closeTime || '请选择' }}</text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>
        <view class="form-item switch-item">
          <text class="label">自动接单</text>
          <switch :checked="form.autoAccept" @change="onAutoAcceptChange" color="#1890ff" />
        </view>
      </view>

      <!-- 配送设置 -->
      <view class="form-section">
        <view class="section-title">配送设置</view>
        <view class="form-item">
          <text class="label">配送范围</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.deliveryRange" placeholder="0" />
            <text class="unit">公里</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">配送费</text>
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
import { useMerchantStore } from '@/store/merchant'
import { shopApi } from '@/api'

const merchantStore = useMerchantStore()

const form = ref({
  shopLogo: merchantStore.shopLogo,
  shopName: '',
  phone: '',
  notice: '',
  address: '',
  latitude: 0,
  longitude: 0,
  shopStatus: 1,
  openTime: '08:00',
  closeTime: '22:00',
  autoAccept: false,
  deliveryRange: '',
  deliveryFee: '',
  minOrderAmount: '',
  freeDeliveryAmount: ''
})

onLoad(() => {
  loadShopInfo()
})

// 加载店铺信息
const loadShopInfo = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await shopApi.getInfo()
    if (res.code === 200 && res.data) {
      const data = res.data
      form.value = {
        shopLogo: data.shopLogo || merchantStore.shopLogo,
        shopName: data.shopName || '',
        phone: data.phone || '',
        notice: data.notice || '',
        address: data.address || '',
        latitude: data.latitude || 0,
        longitude: data.longitude || 0,
        shopStatus: data.shopStatus || 0,
        openTime: data.openTime || '08:00',
        closeTime: data.closeTime || '22:00',
        autoAccept: data.autoAccept || false,
        deliveryRange: data.deliveryRange ? String(data.deliveryRange) : '',
        deliveryFee: data.deliveryFee ? String(data.deliveryFee) : '',
        minOrderAmount: data.minOrderAmount ? String(data.minOrderAmount) : '',
        freeDeliveryAmount: data.freeDeliveryAmount ? String(data.freeDeliveryAmount) : ''
      }
    }
  } catch (e) {
    console.error('加载店铺信息失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 更换头像
const changeAvatar = async () => {
  try {
    const res = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })

    uni.showLoading({ title: '上传中...' })
    const uploadRes = await uni.uploadFile({
      url: '/api/upload',
      filePath: res.tempFilePaths[0],
      name: 'file',
      header: {
        'Authorization': `Bearer ${uni.getStorageSync('merchant_token')}`
      }
    })

    const data = JSON.parse(uploadRes.data)
    if (data.code === 200) {
      form.value.shopLogo = data.data.url
    } else {
      uni.showToast({ title: '上传失败', icon: 'none' })
    }
  } catch (e) {
    // 取消
  } finally {
    uni.hideLoading()
  }
}

// 选择位置
const chooseLocation = async () => {
  try {
    const res = await uni.chooseLocation()
    form.value.address = res.address
    form.value.latitude = res.latitude
    form.value.longitude = res.longitude
  } catch (e) {
    // 取消
  }
}

// 营业状态变更
const onStatusChange = (e) => {
  form.value.shopStatus = e.detail.value ? 1 : 0
}

// 开始时间变更
const onOpenTimeChange = (e) => {
  form.value.openTime = e.detail.value
}

// 结束时间变更
const onCloseTimeChange = (e) => {
  form.value.closeTime = e.detail.value
}

// 自动接单变更
const onAutoAcceptChange = (e) => {
  form.value.autoAccept = e.detail.value
}

// 保存
const save = async () => {
  if (!form.value.shopName.trim()) {
    uni.showToast({ title: '请输入店铺名称', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '保存中...' })

    const data = {
      ...form.value,
      deliveryRange: form.value.deliveryRange ? parseFloat(form.value.deliveryRange) : 0,
      deliveryFee: form.value.deliveryFee ? parseFloat(form.value.deliveryFee) : 0,
      minOrderAmount: form.value.minOrderAmount ? parseFloat(form.value.minOrderAmount) : 0,
      freeDeliveryAmount: form.value.freeDeliveryAmount ? parseFloat(form.value.freeDeliveryAmount) : 0
    }

    const res = await shopApi.updateInfo(data)
    if (res.code === 200) {
      // 更新本地状态
      merchantStore.updateMerchantInfo({
        shopName: form.value.shopName,
        shopLogo: form.value.shopLogo,
        shopStatus: form.value.shopStatus
      })
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
  padding-bottom: calc(140rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
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
    width: 160rpx;
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

  &.avatar-item {
    justify-content: space-between;

    .avatar-wrap {
      position: relative;

      image {
        width: 120rpx;
        height: 120rpx;
        border-radius: 16rpx;
      }

      uni-icons {
        position: absolute;
        right: -10rpx;
        bottom: -10rpx;
        width: 44rpx;
        height: 44rpx;
        background-color: $primary-color;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  &.textarea-item {
    flex-direction: column;
    align-items: flex-start;

    .label {
      margin-bottom: 16rpx;
    }

    textarea {
      width: 100%;
      height: 160rpx;
      font-size: 28rpx;
      padding: 16rpx;
      background-color: #f5f5f5;
      border-radius: $border-radius-sm;
    }

    .address-input {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx;
      background-color: #f5f5f5;
      border-radius: $border-radius-sm;

      text {
        flex: 1;
        font-size: 28rpx;
        color: #333;

        &.placeholder {
          color: #999;
        }
      }
    }
  }

  &.switch-item {
    justify-content: space-between;

    .switch-wrap {
      display: flex;
      align-items: center;

      .status-text {
        font-size: 26rpx;
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
