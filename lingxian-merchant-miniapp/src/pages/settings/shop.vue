<template>
  <view class="container">
    <!-- 审核状态提示条 -->
    <view class="audit-banner pending" v-if="auditInfo.status === 1">
      <uni-icons type="info" size="18" color="#fa8c16" />
      <text>您提交的商户信息变更正在审核中</text>
    </view>
    <view class="audit-banner rejected" v-else-if="auditInfo.status === 2">
      <uni-icons type="closeempty" size="18" color="#ff4d4f" />
      <view class="reject-content">
        <text>信息变更审核未通过</text>
        <text class="reason">原因：{{ auditInfo.rejectReason }}</text>
      </view>
    </view>

    <!-- 本月修改次数提示 -->
    <view class="modify-limit-tip" v-if="monthlyModifyCount > 0">
      <text>本月已修改 {{ monthlyModifyCount }}/3 次，剩余 {{ 3 - monthlyModifyCount }} 次</text>
    </view>

    <view class="form">
      <!-- 需要审核的信息 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">店铺信息</text>
          <text class="section-tip">修改需审核</text>
        </view>

        <!-- 店铺头像 -->
        <view class="form-item avatar-item" @click="changeAvatar">
          <text class="label">店铺头像</text>
          <view class="avatar-wrap">
            <image :src="form.shopLogo" mode="aspectFill" />
            <view class="pending-badge" v-if="auditInfo.status === 1 && auditInfo.pendingData.shopLogo">
              <text>待审</text>
            </view>
            <uni-icons type="camera" size="16" color="#fff" />
          </view>
        </view>

        <!-- 店铺名称 -->
        <view class="form-item">
          <text class="label">店铺名称</text>
          <view class="value-wrap">
            <input type="text" v-model="form.shopName" placeholder="请输入店铺名称" />
            <view class="pending-tag" v-if="auditInfo.status === 1 && auditInfo.pendingData.shopName">待审</view>
          </view>
        </view>

        <!-- 联系电话 -->
        <view class="form-item">
          <text class="label">联系电话</text>
          <view class="value-wrap">
            <input type="tel" v-model="form.phone" placeholder="请输入联系电话" />
            <view class="pending-tag" v-if="auditInfo.status === 1 && auditInfo.pendingData.phone">待审</view>
          </view>
        </view>

        <!-- 店铺地址 -->
        <view class="form-item textarea-item">
          <view class="label-row">
            <text class="label">店铺地址</text>
            <view class="pending-tag" v-if="auditInfo.status === 1 && auditInfo.pendingData.address">待审</view>
          </view>
          <view class="address-input" @click="chooseLocation">
            <text v-if="form.address">{{ form.address }}</text>
            <text v-else class="placeholder">点击选择地址</text>
            <uni-icons type="location" size="18" color="#1890ff" />
          </view>
        </view>

        <!-- 店铺公告（不需要审核） -->
        <view class="form-item textarea-item">
          <text class="label">店铺公告</text>
          <textarea v-model="form.notice" placeholder="输入店铺公告，会展示在用户端首页" maxlength="200" />
        </view>
      </view>

      <!-- 营业设置（不需要审核，即时生效） -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">营业设置</text>
          <text class="section-tip instant">即时生效</text>
        </view>
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

      <!-- 配送设置（不需要审核，即时生效） -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">配送设置</text>
          <text class="section-tip instant">即时生效</text>
        </view>
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
    <view class="save-btn" :class="{ disabled: auditInfo.status === 1 || monthlyModifyCount >= 3 }" @click="save">
      <text v-if="auditInfo.status === 1">审核中，暂不可修改</text>
      <text v-else-if="monthlyModifyCount >= 3">本月修改次数已用完</text>
      <text v-else>保存设置</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMerchantStore } from '@/store/merchant'
import { shopApi } from '@/api'

const merchantStore = useMerchantStore()

// 原始数据（用于比较是否有变更）
const originalData = ref({})

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

// 审核信息
const auditInfo = reactive({
  status: 0, // 0-无审核 1-审核中 2-审核未通过
  pendingData: {}, // 待审核的数据
  rejectReason: ''
})

// 本月修改次数
const monthlyModifyCount = ref(0)

onShow(() => {
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
      // 保存原始数据
      originalData.value = { ...form.value }

      // 加载审核状态
      auditInfo.status = data.auditStatus || 0
      auditInfo.pendingData = data.pendingData || {}
      auditInfo.rejectReason = data.auditRejectReason || ''

      // 本月修改次数
      monthlyModifyCount.value = data.monthlyModifyCount || 0
    }
  } catch (e) {
    console.error('加载店铺信息失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 更换头像
const changeAvatar = async () => {
  if (auditInfo.status === 1) {
    uni.showToast({ title: '审核中，暂不可修改', icon: 'none' })
    return
  }
  if (monthlyModifyCount.value >= 3) {
    uni.showToast({ title: '本月修改次数已用完', icon: 'none' })
    return
  }

  try {
    const res = await uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })

    uni.showLoading({ title: '上传中...' })
    const uploadRes = await uni.uploadFile({
      url: 'http://localhost:8086/api/merchant/upload',
      filePath: res.tempFilePaths[0],
      name: 'file',
      header: {
        'Authorization': `Bearer ${uni.getStorageSync('merchant_token')}`
      }
    })

    const data = JSON.parse(uploadRes.data)
    if (data.code === 200) {
      form.value.shopLogo = data.data.url
      uni.showToast({ title: '上传成功', icon: 'success' })
    } else {
      uni.showToast({ title: '上传失败', icon: 'none' })
    }
  } catch (e) {
    console.error('更换头像失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 选择位置
const chooseLocation = async () => {
  if (auditInfo.status === 1) {
    uni.showToast({ title: '审核中，暂不可修改', icon: 'none' })
    return
  }
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

// 检查需要审核的字段是否有变更
const hasAuditFieldChanged = () => {
  const auditFields = ['shopLogo', 'shopName', 'phone', 'address']
  return auditFields.some(field => form.value[field] !== originalData.value[field])
}

// 保存
const save = async () => {
  if (auditInfo.status === 1) {
    uni.showToast({ title: '审核中，暂不可修改', icon: 'none' })
    return
  }
  if (monthlyModifyCount.value >= 3 && hasAuditFieldChanged()) {
    uni.showToast({ title: '本月修改次数已用完', icon: 'none' })
    return
  }
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

    // 检查是否有需要审核的字段变更
    const needAudit = hasAuditFieldChanged()

    const res = await shopApi.updateInfo(data)
    if (res.code === 200) {
      if (needAudit) {
        uni.showModal({
          title: '提交成功',
          content: '店铺信息变更已提交审核，审核通过后生效。营业设置和配送设置已即时生效。',
          showCancel: false
        })
        // 刷新数据
        loadShopInfo()
      } else {
        // 更新本地状态
        merchantStore.updateMerchantInfo({
          shopName: form.value.shopName,
          shopLogo: form.value.shopLogo,
          shopStatus: form.value.shopStatus
        })
        uni.showToast({ title: '保存成功', icon: 'success' })
      }
    }
  } catch (e) {
    console.error('保存失败', e)
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
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

/* 审核状态提示条 */
.audit-banner {
  display: flex;
  align-items: flex-start;
  padding: 24rpx 30rpx;

  &.pending {
    background-color: #fff7e6;

    text {
      color: #fa8c16;
      font-size: 26rpx;
      margin-left: 12rpx;
    }
  }

  &.rejected {
    background-color: #fff2f0;

    .reject-content {
      margin-left: 12rpx;

      text {
        display: block;
        font-size: 26rpx;
        color: #ff4d4f;

        &.reason {
          font-size: 24rpx;
          margin-top: 8rpx;
          color: #999;
        }
      }
    }
  }
}

/* 修改次数提示 */
.modify-limit-tip {
  padding: 16rpx 30rpx;
  background-color: #e6f7ff;

  text {
    font-size: 24rpx;
    color: #1890ff;
  }
}

.form {
  padding: 20rpx;
}

.form-section {
  background-color: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .section-title {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
    }

    .section-tip {
      font-size: 22rpx;
      color: #fa8c16;
      padding: 4rpx 12rpx;
      background-color: #fff7e6;
      border-radius: 4rpx;

      &.instant {
        color: #52c41a;
        background-color: #f6ffed;
      }
    }
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

  .value-wrap {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    input {
      flex: 1;
      text-align: right;
    }

    .pending-tag {
      margin-left: 12rpx;
      font-size: 20rpx;
      color: #fa8c16;
      padding: 4rpx 8rpx;
      background-color: #fff7e6;
      border-radius: 4rpx;
    }
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

      .pending-badge {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(250, 140, 22, 0.7);
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        text {
          font-size: 24rpx;
          color: #fff;
        }
      }

      uni-icons {
        position: absolute;
        right: -10rpx;
        bottom: -10rpx;
        width: 44rpx;
        height: 44rpx;
        background-color: #1890ff;
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

    .label-row {
      display: flex;
      align-items: center;
      margin-bottom: 16rpx;

      .label {
        width: auto;
        margin-right: 12rpx;
      }

      .pending-tag {
        font-size: 20rpx;
        color: #fa8c16;
        padding: 4rpx 8rpx;
        background-color: #fff7e6;
        border-radius: 4rpx;
      }
    }

    .label {
      margin-bottom: 16rpx;
    }

    textarea {
      width: 100%;
      height: 160rpx;
      font-size: 28rpx;
      padding: 16rpx;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      box-sizing: border-box;
    }

    .address-input {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      box-sizing: border-box;

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
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  border-radius: 48rpx;
  margin-bottom: env(safe-area-inset-bottom);

  text {
    font-size: 32rpx;
    color: #fff;
  }

  &.disabled {
    background: #ccc;
  }
}
</style>
