<template>
  <view class="container">
    <!-- 订单状态 -->
    <view class="status-card" :class="getStatusClass(order.status)">
      <view class="status-icon">
        <uni-icons :type="getStatusIcon(order.status)" size="50" color="#fff" />
      </view>
      <view class="status-info">
        <text class="status-text">{{ getStatusText(order.status) }}</text>
        <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
      </view>
    </view>

    <!-- 配送信息 -->
    <view class="section delivery-section" v-if="order.status === 3">
      <view class="delivery-info">
        <uni-icons type="car" size="24" color="#1890ff" />
        <view class="info">
          <text class="title">配送员正在赶来</text>
          <text class="desc">预计{{ order.estimateTime || '30分钟' }}内送达</text>
        </view>
      </view>
      <view class="delivery-actions">
        <view class="action-btn" @click="callDelivery">
          <uni-icons type="phone" size="20" color="#666" />
          <text>联系配送</text>
        </view>
      </view>
    </view>

    <!-- 收货地址 -->
    <view class="section address-section">
      <uni-icons type="location-filled" size="20" color="#1890ff" />
      <view class="address-info">
        <view class="receiver">
          <text class="name">{{ order.receiverName }}</text>
          <text class="phone">{{ order.receiverPhone }}</text>
        </view>
        <text class="address">{{ order.receiverAddress }}</text>
      </view>
    </view>

    <!-- 商品信息 -->
    <view class="section">
      <view class="section-header" @click="goShop">
        <image :src="order.shopLogo" mode="aspectFill" />
        <text class="shop-name">{{ order.shopName }}</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="product-list">
        <view class="product-item" v-for="(item, index) in orderItems" :key="index">
          <image :src="item.productImage" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.productName }}</text>
            <text class="spec">{{ item.skuName }}</text>
          </view>
          <view class="right">
            <text class="price">¥{{ item.price }}</text>
            <text class="quantity">x{{ item.quantity }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 价格明细 -->
    <view class="section">
      <view class="price-item">
        <text>商品金额</text>
        <text>¥{{ order.totalAmount }}</text>
      </view>
      <view class="price-item">
        <text>配送费</text>
        <text>¥{{ order.freightAmount || '0.00' }}</text>
      </view>
      <view class="price-item" v-if="order.discountAmount > 0">
        <text>优惠</text>
        <text class="discount">-¥{{ order.discountAmount }}</text>
      </view>
      <view class="price-item total">
        <text>实付金额</text>
        <text class="amount">¥{{ order.payAmount }}</text>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="section order-info">
      <view class="info-item">
        <text class="label">订单编号</text>
        <view class="value" @click="copyOrderNo">
          <text>{{ order.orderNo }}</text>
          <text class="copy">复制</text>
        </view>
      </view>
      <view class="info-item">
        <text class="label">下单时间</text>
        <text class="value">{{ order.createTime }}</text>
      </view>
      <view class="info-item" v-if="order.payTime">
        <text class="label">支付时间</text>
        <text class="value">{{ order.payTime }}</text>
      </view>
      <view class="info-item" v-if="order.deliveryTime">
        <text class="label">配送时间</text>
        <text class="value">{{ order.deliveryTime }}</text>
      </view>
      <view class="info-item" v-if="order.remark">
        <text class="label">订单备注</text>
        <text class="value">{{ order.remark }}</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer" v-if="showActions">
      <!-- 待付款 -->
      <template v-if="order.status === 1">
        <view class="btn default" @click="cancelOrder">取消订单</view>
        <view class="btn primary" @click="payOrder">立即支付</view>
      </template>
      <!-- 配送中 -->
      <template v-else-if="order.status === 3">
        <view class="btn default" @click="callShop">联系商家</view>
        <view class="btn primary" @click="confirmReceive">确认收货</view>
      </template>
      <!-- 待评价 -->
      <template v-else-if="order.status === 4">
        <view class="btn primary" @click="goComment">去评价</view>
      </template>
      <!-- 已完成 -->
      <template v-else-if="order.status === 5">
        <view class="btn default" @click="viewComment">查看评价</view>
        <view class="btn primary" @click="reorder">再来一单</view>
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
  items: []
})

// 获取订单商品列表（兼容 items 和 products 字段）
const orderItems = computed(() => {
  return order.value.items || order.value.products || []
})

const showActions = computed(() => {
  // 1-待付款 3-配送中 4-待评价 5-已完成
  return [1, 3, 4, 5].includes(order.value.status)
})

onLoad((options) => {
  orderId.value = options.id
  loadOrder()
})

// 加载订单
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

// 状态相关方法
const getStatusClass = (status) => {
  const map = {
    1: 'pending',    // 待付款
    2: 'wait',       // 待发货
    3: 'delivery',   // 配送中
    4: 'done',       // 待评价
    5: 'done',       // 已完成
    6: 'cancel',     // 已取消
    7: 'cancel'      // 已退款
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    1: '待支付',
    2: '待发货',
    3: '配送中',
    4: '待评价',
    5: '已完成',
    6: '已取消',
    7: '已退款'
  }
  return map[status] || '未知'
}

const getStatusDesc = (status) => {
  const map = {
    1: '请在15分钟内完成支付',
    2: '商家正在备货中',
    3: '配送员正在配送中',
    4: '请对本次购物进行评价',
    5: '感谢您的惠顾',
    6: '订单已取消',
    7: '订单已退款'
  }
  return map[status] || ''
}

const getStatusIcon = (status) => {
  const map = {
    1: 'wallet',
    2: 'list',
    3: 'car',
    4: 'star',
    5: 'checkbox-filled',
    6: 'closeempty',
    7: 'redo'
  }
  return map[status] || 'info'
}

// 复制订单号
const copyOrderNo = () => {
  uni.setClipboardData({
    data: order.value.orderNo,
    success: () => uni.showToast({ title: '已复制', icon: 'success' })
  })
}

// 跳转店铺
const goShop = () => {
  uni.navigateTo({ url: `/pages/shop/index?id=${order.value.shopId}` })
}

// 取消订单
const cancelOrder = async () => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要取消订单吗？'
    })

    const res = await orderApi.cancel(orderId.value)
    if (res.code === 200) {
      uni.showToast({ title: '订单已取消', icon: 'success' })
      loadOrder()
    }
  } catch (e) {
    // 取消
  }
}

// 模拟支付（开发环境）
const mockPayment = () => {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title: '模拟支付',
      content: `支付金额：¥${order.value.payAmount}`,
      confirmText: '确认支付',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          resolve()
        } else {
          reject(new Error('用户取消支付'))
        }
      }
    })
  })
}

// 支付订单
const payOrder = async () => {
  // 开发环境：使用模拟支付
  const isDev = true // 正式上线时改为 false

  if (isDev) {
    try {
      await mockPayment()
      uni.showToast({ title: '支付成功', icon: 'success' })
      // 模拟更新订单状态
      order.value.status = 1 // 待接单
      setTimeout(() => {
        loadOrder()
      }, 1500)
    } catch (e) {
      uni.showToast({ title: '支付取消', icon: 'none' })
    }
    return
  }

  // 正式环境：调用微信支付
  try {
    const res = await orderApi.pay(orderId.value)
    if (res.code === 200) {
      await uni.requestPayment(res.data)
      uni.showToast({ title: '支付成功', icon: 'success' })
      loadOrder()
    }
  } catch (e) {
    uni.showToast({ title: '支付失败', icon: 'none' })
  }
}

// 确认收货
const confirmReceive = async () => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确认已收到商品？'
    })

    const res = await orderApi.confirm(orderId.value)
    if (res.code === 200) {
      uni.showToast({ title: '已确认收货', icon: 'success' })
      loadOrder()
    }
  } catch (e) {
    // 取消
  }
}

// 联系商家
const callShop = () => {
  uni.makePhoneCall({ phoneNumber: order.value.shopPhone })
}

// 联系配送
const callDelivery = () => {
  uni.makePhoneCall({ phoneNumber: order.value.deliveryPhone })
}

// 去评价
const goComment = () => {
  uni.navigateTo({ url: `/pages/order/comment?id=${orderId.value}` })
}

// 查看评价
const viewComment = () => {
  uni.navigateTo({ url: `/pages/order/comment-view?orderId=${orderId.value}` })
}

// 再来一单
const reorder = async () => {
  try {
    const res = await orderApi.reorder(orderId.value)
    if (res.code === 200) {
      uni.switchTab({ url: '/pages/cart/index' })
    }
  } catch (e) {
    uni.showToast({ title: '添加失败', icon: 'none' })
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

/* 状态卡片 */
.status-card {
  display: flex;
  align-items: center;
  padding: 40rpx 30rpx;
  background: linear-gradient(135deg, #22c55e, #16a34a);

  &.pending {
    background: linear-gradient(135deg, #f97316, #fb923c);
  }

  &.delivery {
    background: linear-gradient(135deg, #3b82f6, #60a5fa);
  }

  &.done {
    background: linear-gradient(135deg, #22c55e, #4ade80);
  }

  &.cancel {
    background: linear-gradient(135deg, #9ca3af, #d1d5db);
  }

  .status-icon {
    margin-right: 24rpx;
  }

  .status-info {
    .status-text {
      font-size: 36rpx;
      font-weight: bold;
      color: #fff;
      display: block;
    }

    .status-desc {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 8rpx;
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
}

/* 配送信息 */
.delivery-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;

  .delivery-info {
    display: flex;
    align-items: center;

    .info {
      margin-left: 16rpx;

      .title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
        display: block;
      }

      .desc {
        font-size: 24rpx;
        color: #999;
        margin-top: 6rpx;
        display: block;
      }
    }
  }

  .action-btn {
    display: flex;
    align-items: center;
    padding: 12rpx 24rpx;
    background-color: #f5f5f5;
    border-radius: 30rpx;

    text {
      font-size: 24rpx;
      color: #666;
      margin-left: 8rpx;
    }
  }
}

/* 收货地址 */
.address-section {
  display: flex;
  align-items: flex-start;
  padding: 24rpx;

  .address-info {
    flex: 1;
    margin-left: 16rpx;

    .receiver {
      .name {
        font-size: 30rpx;
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
      margin-top: 10rpx;
      display: block;
    }
  }
}

/* 店铺头部 */
.section-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  image {
    width: 48rpx;
    height: 48rpx;
    border-radius: 8rpx;
    margin-right: 16rpx;
  }

  .shop-name {
    flex: 1;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
  }
}

/* 商品列表 */
.product-list {
  padding: 0 24rpx;

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
        color: #333;
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

/* 价格明细 */
.price-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 24rpx;
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

    .amount {
      font-size: 36rpx;
      font-weight: bold;
      color: $danger-color;
    }
  }
}

/* 订单信息 */
.order-info {
  padding: 16rpx 24rpx;

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
      display: flex;
      align-items: center;

      .copy {
        font-size: 22rpx;
        color: $primary-color;
        margin-left: 16rpx;
        padding: 4rpx 12rpx;
        border: 1rpx solid $primary-color;
        border-radius: 20rpx;
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
      background-color: #22c55e;
      color: #fff;
    }
  }
}
</style>
