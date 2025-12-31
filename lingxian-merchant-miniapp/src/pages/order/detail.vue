<template>
  <view class="container">
    <!-- 订单状态 -->
    <view class="status-section" :class="getStatusClass(order.status)">
      <view class="status-info">
        <text class="status-text">{{ getStatusText(order.status) }}</text>
        <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
      </view>
      <uni-icons :type="getStatusIcon(order.status)" size="60" color="#fff" />
    </view>

    <!-- 收货信息 -->
    <view class="section address-section">
      <view class="section-title">
        <uni-icons type="location" size="18" color="#1890ff" />
        <text>收货信息</text>
      </view>
      <view class="address-content">
        <view class="receiver">
          <text class="name">{{ order.receiverName }}</text>
          <text class="phone">{{ order.receiverPhone }}</text>
        </view>
        <text class="address">{{ order.receiverAddress }}</text>
        <view class="delivery-time" v-if="order.deliveryTime">
          <text>预约配送：{{ order.deliveryTime }}</text>
        </view>
      </view>
      <view class="nav-btn" v-if="order.status === 3" @click="openNavigation">
        <uni-icons type="navigate" size="20" color="#1890ff" />
        <text>导航</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="section">
      <view class="section-title">
        <uni-icons type="shop" size="18" color="#1890ff" />
        <text>商品信息</text>
      </view>
      <view class="product-list">
        <view class="product-item" v-for="(item, index) in order.products" :key="index">
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
          <text>¥{{ order.totalAmount }}</text>
        </view>
        <view class="amount-item">
          <text>配送费</text>
          <text>¥{{ order.deliveryFee || '0.00' }}</text>
        </view>
        <view class="amount-item" v-if="order.discountAmount > 0">
          <text>优惠</text>
          <text class="discount">-¥{{ order.discountAmount }}</text>
        </view>
        <view class="amount-item total">
          <text>实付金额</text>
          <text class="pay-amount">¥{{ order.payAmount }}</text>
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="section">
      <view class="section-title">
        <uni-icons type="info" size="18" color="#1890ff" />
        <text>订单信息</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">订单编号</text>
          <text class="value" @click="copyOrderNo">{{ order.orderNo }}</text>
        </view>
        <view class="info-item">
          <text class="label">下单时间</text>
          <text class="value">{{ order.createTime }}</text>
        </view>
        <view class="info-item" v-if="order.payTime">
          <text class="label">支付时间</text>
          <text class="value">{{ order.payTime }}</text>
        </view>
        <view class="info-item" v-if="order.acceptTime">
          <text class="label">接单时间</text>
          <text class="value">{{ order.acceptTime }}</text>
        </view>
        <view class="info-item" v-if="order.deliveryStartTime">
          <text class="label">开始配送</text>
          <text class="value">{{ order.deliveryStartTime }}</text>
        </view>
        <view class="info-item" v-if="order.completeTime">
          <text class="label">完成时间</text>
          <text class="value">{{ order.completeTime }}</text>
        </view>
        <view class="info-item" v-if="order.remark">
          <text class="label">订单备注</text>
          <text class="value remark">{{ order.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 拒单原因 -->
    <view class="section" v-if="order.status === 5 && order.rejectReason">
      <view class="section-title">
        <uni-icons type="closeempty" size="18" color="#ff4d4f" />
        <text>拒单原因</text>
      </view>
      <view class="reject-reason">
        <text>{{ order.rejectReason }}</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer" v-if="showActions">
      <!-- 待接单 -->
      <template v-if="order.status === 1">
        <view class="btn default" @click="rejectOrder">拒单</view>
        <view class="btn primary" @click="acceptOrder">接单</view>
      </template>
      <!-- 待配送 -->
      <template v-else-if="order.status === 2">
        <view class="btn primary" @click="startDelivery">开始配送</view>
      </template>
      <!-- 配送中 -->
      <template v-else-if="order.status === 3">
        <view class="btn default" @click="callCustomer">联系顾客</view>
        <view class="btn primary" @click="completeDelivery">完成配送</view>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi } from '@/api'

const orderId = ref('')
const order = ref({
  products: []
})

const showActions = computed(() => {
  return [1, 2, 3].includes(order.value.status)
})

onLoad((options) => {
  orderId.value = options.id
  loadOrder()
})

// 加载订单详情
const loadOrder = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await orderApi.getDetail(orderId.value)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch (e) {
    console.error('加载订单失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 获取状态样式
const getStatusClass = (status) => {
  const classMap = {
    0: 'wait',
    1: 'pending',
    2: 'pending',
    3: 'delivering',
    4: 'completed',
    5: 'canceled'
  }
  return classMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待支付',
    1: '待接单',
    2: '待配送',
    3: '配送中',
    4: '已完成',
    5: '已取消'
  }
  return textMap[status] || '未知'
}

// 获取状态描述
const getStatusDesc = (status) => {
  const descMap = {
    0: '等待用户支付',
    1: '请尽快接单处理',
    2: '订单已接，请尽快配送',
    3: '正在配送中，请送达后确认',
    4: '订单已完成',
    5: '订单已取消'
  }
  return descMap[status] || ''
}

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    0: 'wallet',
    1: 'notification',
    2: 'list',
    3: 'car',
    4: 'checkbox-filled',
    5: 'closeempty'
  }
  return iconMap[status] || 'info'
}

// 复制订单号
const copyOrderNo = () => {
  uni.setClipboardData({
    data: order.value.orderNo,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// 打开导航
const openNavigation = () => {
  uni.openLocation({
    latitude: order.value.latitude,
    longitude: order.value.longitude,
    name: order.value.receiverAddress,
    address: order.value.receiverAddress
  })
}

// 联系顾客
const callCustomer = () => {
  uni.makePhoneCall({
    phoneNumber: order.value.receiverPhone
  })
}

// 接单
const acceptOrder = async () => {
  try {
    const res = await orderApi.accept(orderId.value)
    if (res.code === 200) {
      uni.showToast({ title: '接单成功', icon: 'success' })
      loadOrder()
    }
  } catch (e) {
    console.error('接单失败', e)
  }
}

// 拒单
const rejectOrder = async () => {
  try {
    const result = await uni.showModal({
      title: '拒单原因',
      editable: true,
      placeholderText: '请输入拒单原因'
    })

    if (result.confirm && result.content) {
      const res = await orderApi.reject(orderId.value, result.content)
      if (res.code === 200) {
        uni.showToast({ title: '已拒单', icon: 'success' })
        loadOrder()
      }
    }
  } catch (e) {
    // 取消
  }
}

// 开始配送
const startDelivery = async () => {
  try {
    const res = await orderApi.startDelivery(orderId.value)
    if (res.code === 200) {
      uni.showToast({ title: '开始配送', icon: 'success' })
      loadOrder()
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

    const res = await orderApi.completeDelivery(orderId.value)
    if (res.code === 200) {
      uni.showToast({ title: '配送完成', icon: 'success' })
      loadOrder()
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

  &.canceled {
    background: linear-gradient(135deg, #999, #bbb);
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

  .nav-btn {
    position: absolute;
    right: 30rpx;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;

    text {
      font-size: 22rpx;
      color: #1890ff;
      margin-top: 4rpx;
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

    .discount {
      color: $success-color;
    }

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

/* 拒单原因 */
.reject-reason {
  padding: 24rpx 30rpx;

  text {
    font-size: 26rpx;
    color: #ff4d4f;
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

    &.default {
      background-color: #f5f5f5;
      color: #666;
    }

    &.primary {
      background-color: $primary-color;
      color: #fff;
    }
  }
}
</style>
