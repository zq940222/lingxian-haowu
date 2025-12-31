<template>
  <view class="container">
    <!-- 配送状态 -->
    <view class="status-section" :class="getStatusClass(delivery.status)">
      <view class="status-info">
        <text class="status-text">{{ getStatusText(delivery.status) }}</text>
        <text class="status-desc">{{ getStatusDesc(delivery.status) }}</text>
      </view>
      <uni-icons :type="getStatusIcon(delivery.status)" size="60" color="#fff" />
    </view>

    <!-- 收货信息 -->
    <view class="section address-section">
      <view class="section-title">
        <uni-icons type="location" size="18" color="#1890ff" />
        <text>收货信息</text>
      </view>
      <view class="address-content">
        <view class="receiver">
          <text class="name">{{ delivery.receiverName }}</text>
          <text class="phone">{{ delivery.receiverPhone }}</text>
        </view>
        <text class="address">{{ delivery.receiverAddress }}</text>
        <view class="delivery-time" v-if="delivery.deliveryTime">
          <text>预约配送：{{ delivery.deliveryTime }}</text>
        </view>
      </view>
      <view class="action-btns" v-if="delivery.status === 'delivering'">
        <view class="nav-btn" @click="openNavigation">
          <uni-icons type="navigate" size="20" color="#1890ff" />
          <text>导航</text>
        </view>
        <view class="call-btn" @click="callCustomer">
          <uni-icons type="phone" size="20" color="#52c41a" />
          <text>电话</text>
        </view>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="section">
      <view class="section-title">
        <uni-icons type="shop" size="18" color="#1890ff" />
        <text>商品信息</text>
      </view>
      <view class="product-list">
        <view class="product-item" v-for="(item, index) in delivery.products" :key="index">
          <image :src="item.image" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.name }}</text>
            <text class="spec">{{ item.spec }}</text>
          </view>
          <view class="right">
            <text class="price">¥{{ item.price }}</text>
            <text class="quantity">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 金额信息 -->
    <view class="section">
      <view class="section-title">
        <uni-icons type="wallet" size="18" color="#1890ff" />
        <text>金额信息</text>
      </view>
      <view class="amount-list">
        <view class="amount-item">
          <text>商品金额</text>
          <text>¥{{ delivery.totalAmount }}</text>
        </view>
        <view class="amount-item">
          <text>配送费</text>
          <text>¥{{ delivery.deliveryFee || '0.00' }}</text>
        </view>
        <view class="amount-item total">
          <text>实付金额</text>
          <text class="pay-amount">¥{{ delivery.payAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 配送信息 -->
    <view class="section">
      <view class="section-title">
        <uni-icons type="info" size="18" color="#1890ff" />
        <text>配送信息</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">订单编号</text>
          <text class="value" @click="copyOrderNo">{{ delivery.orderNo }}</text>
        </view>
        <view class="info-item">
          <text class="label">下单时间</text>
          <text class="value">{{ delivery.createTime }}</text>
        </view>
        <view class="info-item" v-if="delivery.deliveryStartTime">
          <text class="label">开始配送</text>
          <text class="value">{{ delivery.deliveryStartTime }}</text>
        </view>
        <view class="info-item" v-if="delivery.completeTime">
          <text class="label">完成时间</text>
          <text class="value">{{ delivery.completeTime }}</text>
        </view>
        <view class="info-item" v-if="delivery.remark">
          <text class="label">订单备注</text>
          <text class="value remark">{{ delivery.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer" v-if="showActions">
      <!-- 待配送 -->
      <template v-if="delivery.status === 'pending'">
        <view class="btn primary" @click="startDelivery">开始配送</view>
      </template>
      <!-- 配送中 -->
      <template v-else-if="delivery.status === 'delivering'">
        <view class="btn primary" @click="completeDelivery">完成配送</view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { deliveryApi, orderApi } from '@/api'

const deliveryId = ref('')
const delivery = ref({
  products: []
})

const showActions = computed(() => {
  return ['pending', 'delivering'].includes(delivery.value.status)
})

onLoad((options) => {
  deliveryId.value = options.id
  loadDelivery()
})

// 加载配送详情
const loadDelivery = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await deliveryApi.getDetail(deliveryId.value)
    if (res.code === 200) {
      delivery.value = res.data
    }
  } catch (e) {
    console.error('加载配送详情失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 获取状态样式
const getStatusClass = (status) => {
  const classMap = {
    'pending': 'pending',
    'delivering': 'delivering',
    'completed': 'completed'
  }
  return classMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'pending': '待配送',
    'delivering': '配送中',
    'completed': '已完成'
  }
  return textMap[status] || '未知'
}

// 获取状态描述
const getStatusDesc = (status) => {
  const descMap = {
    'pending': '请尽快开始配送',
    'delivering': '正在配送中，请送达后确认',
    'completed': '配送已完成'
  }
  return descMap[status] || ''
}

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    'pending': 'list',
    'delivering': 'car',
    'completed': 'checkbox-filled'
  }
  return iconMap[status] || 'info'
}

// 复制订单号
const copyOrderNo = () => {
  uni.setClipboardData({
    data: delivery.value.orderNo,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// 打开导航
const openNavigation = () => {
  uni.openLocation({
    latitude: delivery.value.latitude,
    longitude: delivery.value.longitude,
    name: delivery.value.receiverAddress,
    address: delivery.value.receiverAddress
  })
}

// 联系顾客
const callCustomer = () => {
  uni.makePhoneCall({
    phoneNumber: delivery.value.receiverPhone
  })
}

// 开始配送
const startDelivery = async () => {
  try {
    const res = await orderApi.startDelivery(deliveryId.value)
    if (res.code === 200) {
      uni.showToast({ title: '开始配送', icon: 'success' })
      loadDelivery()
    }
  } catch (e) {
    console.error('开始配送失败', e)
  }
}

// 完成配送
const completeDelivery = async () => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确认已完成配送？'
    })

    const res = await orderApi.completeDelivery(deliveryId.value)
    if (res.code === 200) {
      uni.showToast({ title: '配送完成', icon: 'success' })
      loadDelivery()
    }
  } catch (e) {
    // 取消或失败
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

/* 状态区域 */
.status-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #1890ff, #36cfc9);

  &.pending {
    background: linear-gradient(135deg, #ff9800, #ffb74d);
  }

  &.delivering {
    background: linear-gradient(135deg, #1890ff, #36cfc9);
  }

  &.completed {
    background: linear-gradient(135deg, #52c41a, #95de64);
  }

  .status-info {
    .status-text {
      font-size: 40rpx;
      font-weight: bold;
      color: #fff;
      display: block;
    }

    .status-desc {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 10rpx;
      display: block;
    }
  }
}

/* 通用区块 */
.section {
  margin: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .section-title {
    display: flex;
    align-items: center;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;

    text {
      margin-left: 10rpx;
    }
  }
}

/* 收货地址 */
.address-section {
  position: relative;

  .address-content {
    padding: 24rpx 30rpx;

    .receiver {
      margin-bottom: 10rpx;

      .name {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-right: 20rpx;
      }

      .phone {
        font-size: 28rpx;
        color: #666;
      }
    }

    .address {
      font-size: 26rpx;
      color: #666;
      line-height: 1.5;
    }

    .delivery-time {
      margin-top: 16rpx;
      padding: 12rpx 16rpx;
      background-color: #fff7e6;
      border-radius: 8rpx;

      text {
        font-size: 24rpx;
        color: #fa8c16;
      }
    }
  }

  .action-btns {
    position: absolute;
    right: 30rpx;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    gap: 30rpx;

    .nav-btn, .call-btn {
      display: flex;
      flex-direction: column;
      align-items: center;

      text {
        font-size: 22rpx;
        margin-top: 4rpx;
      }
    }

    .nav-btn text {
      color: #1890ff;
    }

    .call-btn text {
      color: #52c41a;
    }
  }
}

/* 商品列表 */
.product-list {
  padding: 0 30rpx;

  .product-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    image {
      width: 120rpx;
      height: 120rpx;
      border-radius: $border-radius-sm;
      margin-right: 20rpx;
    }

    .info {
      flex: 1;

      .name {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .spec {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
    }

    .right {
      text-align: right;

      .price {
        font-size: 28rpx;
        color: $danger-color;
        display: block;
      }

      .quantity {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
    }
  }
}

/* 金额列表 */
.amount-list {
  padding: 16rpx 30rpx;

  .amount-item {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    font-size: 26rpx;
    color: #666;

    &.total {
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;
      margin-top: 10rpx;

      text:first-child {
        font-weight: bold;
        color: #333;
      }

      .pay-amount {
        font-size: 36rpx;
        font-weight: bold;
        color: $danger-color;
      }
    }
  }
}

/* 信息列表 */
.info-list {
  padding: 16rpx 30rpx;

  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    font-size: 26rpx;

    .label {
      color: #999;
    }

    .value {
      color: #333;

      &.remark {
        max-width: 400rpx;
        text-align: right;
      }
    }
  }
}

/* 底部操作 */
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));

  .btn {
    padding: 20rpx 50rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    margin-left: 20rpx;

    &.primary {
      background-color: $primary-color;
      color: #fff;
    }
  }
}
</style>
