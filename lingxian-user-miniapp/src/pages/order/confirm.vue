<template>
  <view class="container">
    <!-- 收货地址 -->
    <view class="address-section" @click="selectAddress">
      <template v-if="selectedAddress">
        <view class="location-icon">📍</view>
        <view class="address-info">
          <view class="receiver">
            <text class="name">{{ selectedAddress.name }}</text>
            <text class="phone">{{ selectedAddress.phone }}</text>
          </view>
          <text class="address">{{ selectedAddress.fullAddress }}</text>
        </view>
      </template>
      <template v-else>
        <view class="location-icon">➕</view>
        <text class="add-text">添加收货地址</text>
      </template>
      <text class="arrow">›</text>
    </view>

    <!-- 配送时间 -->
    <view class="section delivery-time" @click="showTimePicker = true">
      <text class="label">配送时间</text>
      <view class="time-value">
        <text>{{ deliveryTimeText }}</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 按商户分组的商品列表 -->
    <view class="merchant-orders">
      <view class="merchant-order" v-for="(merchantOrder, index) in merchantOrders" :key="merchantOrder.merchantId">
        <view class="order-header">
          <view class="order-badge">订单 {{ index + 1 }}</view>
          <view class="merchant-info">
            <image class="merchant-logo" :src="merchantOrder.merchantLogo || '/static/images/default-shop.png'" mode="aspectFill" />
            <text class="merchant-name">{{ merchantOrder.merchantName }}</text>
          </view>
        </view>

        <!-- 商品列表 -->
        <view class="product-list">
          <view class="product-item" v-for="item in merchantOrder.items" :key="item.id">
            <image :src="item.productImage" mode="aspectFill" />
            <view class="info">
              <text class="name text-ellipsis-2">{{ item.productName }}</text>
            </view>
            <view class="right">
              <text class="price">¥{{ item.price }}</text>
              <text class="quantity">x{{ item.quantity }}</text>
            </view>
          </view>
        </view>

        <!-- 订单备注 -->
        <view class="remark-row">
          <text class="label">订单备注</text>
          <input
            type="text"
            v-model="merchantOrder.remark"
            placeholder="选填，可填写您的特殊要求"
            @input="(e) => updateRemark(merchantOrder.merchantId, e.detail.value)"
          />
        </view>

        <!-- 商户订单小计 -->
        <view class="order-subtotal">
          <text>小计：</text>
          <text class="amount">¥{{ getMerchantTotal(merchantOrder).toFixed(2) }}</text>
        </view>
      </view>
    </view>

    <!-- 金额明细 -->
    <view class="section amount-section">
      <view class="amount-item">
        <text>商品金额</text>
        <text>¥{{ totalAmount.toFixed(2) }}</text>
      </view>
      <view class="amount-item">
        <text>配送费（{{ merchantOrders.length }}单）</text>
        <text>¥{{ totalDeliveryFee.toFixed(2) }}</text>
      </view>
      <view class="amount-item" v-if="discountAmount > 0">
        <text>优惠</text>
        <text class="discount">-¥{{ discountAmount.toFixed(2) }}</text>
      </view>
      <view class="amount-item total-row">
        <text>订单总数</text>
        <text class="order-count">共 {{ merchantOrders.length }} 单</text>
      </view>
    </view>

    <!-- 底部结算 -->
    <view class="footer safe-area-bottom">
      <view class="total">
        <text>合计：</text>
        <text class="price">¥{{ payAmount.toFixed(2) }}</text>
        <text class="order-info">（{{ merchantOrders.length }}个订单）</text>
      </view>
      <view class="submit-btn" :class="{ disabled: !canSubmit }" @click="submitOrder">
        提交订单
      </view>
    </view>

    <!-- 配送时间选择弹窗 -->
    <view class="time-picker-mask" v-if="showTimePicker" @click="showTimePicker = false">
      <view class="time-picker" @click.stop>
        <view class="picker-header">
          <text @click="showTimePicker = false">取消</text>
          <text class="title">选择配送时间</text>
          <text class="confirm" @click="confirmTime">确定</text>
        </view>
        <view class="picker-content">
          <view class="time-options">
            <view
              class="time-option"
              :class="{ active: deliveryTime === 'now' }"
              @click="deliveryTime = 'now'"
            >
              <text>立即配送</text>
              <text class="desc">预计30分钟内送达</text>
            </view>
            <view
              class="time-option"
              :class="{ active: deliveryTime === item }"
              v-for="item in timeSlots"
              :key="item"
              @click="deliveryTime = item"
            >
              <text>{{ item }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store/cart'
import { orderApi, addressApi } from '@/api'

const cartStore = useCartStore()

const selectedAddress = ref(null)
const merchantOrders = ref([]) // 按商户分组的订单
const remarks = ref({}) // 各商户订单的备注
const deliveryTime = ref('now')
const showTimePicker = ref(false)
const deliveryFeePerOrder = ref(5) // 每单配送费
const discountAmount = ref(0)
const selectedCommunity = ref(null) // 用户选择的配送小区

// 时间段
const timeSlots = ref([])

onLoad(() => {
  generateTimeSlots()
  // 获取用户选择的配送小区
  const community = uni.getStorageSync('selectedCommunity')
  if (community) {
    selectedCommunity.value = community
  }
})

onShow(() => {
  // 检查是否选择了小区
  const community = uni.getStorageSync('selectedCommunity')
  if (!community) {
    uni.showModal({
      title: '提示',
      content: '请先选择配送小区',
      showCancel: false,
      success: () => {
        uni.switchTab({ url: '/pages/index/index' })
      }
    })
    return
  }
  selectedCommunity.value = community
  loadDefaultAddress()
  loadCartItems()
})

// 生成配送时间段
const generateTimeSlots = () => {
  const slots = []
  const now = new Date()
  const startHour = now.getHours() + 1

  for (let h = Math.max(startHour, 8); h <= 21; h++) {
    slots.push(`${h}:00-${h + 1}:00`)
  }

  timeSlots.value = slots
}

// 加载默认地址
const loadDefaultAddress = async () => {
  try {
    const res = await addressApi.getDefault()
    if (res.code === 200 && res.data) {
      selectedAddress.value = res.data
    }
  } catch (e) {
    console.error('加载地址失败', e)
  }
}

// 加载购物车中选中的商品（按商户分组）
const loadCartItems = () => {
  const selectedByMerchant = cartStore.getSelectedItemsByMerchant()
  merchantOrders.value = selectedByMerchant.map(group => ({
    ...group,
    remark: remarks.value[group.merchantId] || ''
  }))
}

// 更新商户订单备注
const updateRemark = (merchantId, value) => {
  remarks.value[merchantId] = value
  const order = merchantOrders.value.find(o => o.merchantId === merchantId)
  if (order) {
    order.remark = value
  }
}

// 获取商户订单小计
const getMerchantTotal = (merchantOrder) => {
  return merchantOrder.items.reduce((sum, item) => {
    return sum + parseFloat(item.price) * item.quantity
  }, 0)
}

// 计算属性
const totalAmount = computed(() => {
  return merchantOrders.value.reduce((sum, order) => {
    return sum + getMerchantTotal(order)
  }, 0)
})

const totalDeliveryFee = computed(() => {
  return merchantOrders.value.length * deliveryFeePerOrder.value
})

const payAmount = computed(() => {
  return totalAmount.value + totalDeliveryFee.value - discountAmount.value
})

const deliveryTimeText = computed(() => {
  if (deliveryTime.value === 'now') {
    return '立即配送'
  }
  return deliveryTime.value
})

const canSubmit = computed(() => {
  return selectedAddress.value && merchantOrders.value.length > 0 && selectedCommunity.value
})

// 选择地址
const selectAddress = () => {
  uni.navigateTo({
    url: '/pages/address/list?select=1',
    events: {
      selectAddress: (address) => {
        selectedAddress.value = address
      }
    }
  })
}

// 确认时间选择
const confirmTime = () => {
  showTimePicker.value = false
}

// 模拟支付（开发环境）
const mockPayment = (totalPay) => {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title: '模拟支付',
      content: `支付金额：¥${totalPay.toFixed(2)}\n共 ${merchantOrders.value.length} 个订单`,
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

// 提交订单
const submitOrder = async () => {
  if (!canSubmit.value) {
    if (!selectedCommunity.value) {
      uni.showToast({ title: '请先选择配送小区', icon: 'none' })
      return
    }
    if (!selectedAddress.value) {
      uni.showToast({ title: '请选择收货地址', icon: 'none' })
    }
    return
  }

  // 开发环境：使用模拟支付
  const isDev = true // 正式上线时改为 false

  try {
    uni.showLoading({ title: '提交中...' })

    // 构建多商户订单数据，添加小区ID用于后端验证
    const ordersData = merchantOrders.value.map(order => ({
      merchantId: order.merchantId,
      addressId: selectedAddress.value.id,
      communityId: selectedCommunity.value.id,
      products: order.items.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      deliveryTime: deliveryTime.value === 'now' ? null : deliveryTime.value,
      remark: order.remark || ''
    }))

    // 调用后端批量创建订单
    let orderIds = []
    const res = await orderApi.create({ orders: ordersData })
    if (res.code === 200) {
      orderIds = res.data.orderIds || []
    } else {
      uni.hideLoading()
      uni.showToast({ title: res.msg || '创建订单失败', icon: 'none' })
      return
    }

    if (orderIds.length === 0) {
      uni.hideLoading()
      uni.showToast({ title: '创建订单失败', icon: 'none' })
      return
    }

    uni.hideLoading()

    // 清空已下单的购物车商品
    await cartStore.fetchList()

    if (isDev) {
      // 模拟支付
      try {
        await mockPayment(payAmount.value)

        // 模拟支付成功后，调用后端更新订单状态为已支付
        uni.showLoading({ title: '处理中...' })
        for (const orderId of orderIds) {
          try {
            await orderApi.pay(orderId)
          } catch (e) {
            console.error('更新订单支付状态失败', orderId, e)
          }
        }
        uni.hideLoading()

        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => {
          if (orderIds.length === 1) {
            uni.redirectTo({ url: `/pages/order/detail?id=${orderIds[0]}` })
          } else {
            uni.redirectTo({ url: '/pages/order/list' })
          }
        }, 1500)
      } catch (e) {
        // 支付取消，跳转到待付款订单
        uni.showToast({ title: '支付取消', icon: 'none' })
        setTimeout(() => {
          uni.redirectTo({ url: '/pages/order/list?status=0' })
        }, 1500)
      }
    } else {
      // 正式环境：调用微信支付（合并支付）
      try {
        const payRes = await orderApi.pay({ orderIds })
        await uni.requestPayment(payRes.data.payParams)
        uni.redirectTo({ url: '/pages/order/list' })
      } catch (e) {
        uni.redirectTo({ url: '/pages/order/list?status=0' })
      }
    }
  } catch (e) {
    uni.hideLoading()
    console.error('提交订单失败', e)
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
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

/* 收货地址 */
.address-section {
  display: flex;
  align-items: center;
  padding: 30rpx;
  margin: 20rpx;
  background-color: #fff;
  border-radius: 16rpx;

  .location-icon {
    font-size: 40rpx;
    margin-right: 16rpx;
  }

  .address-info {
    flex: 1;

    .receiver {
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
      margin-top: 10rpx;
      display: block;
    }
  }

  .add-text {
    flex: 1;
    font-size: 28rpx;
    color: #666;
  }

  .arrow {
    font-size: 32rpx;
    color: #999;
  }
}

/* 通用区块 */
.section {
  margin: 20rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

/* 配送时间 */
.delivery-time {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 30rpx;

  .label {
    font-size: 28rpx;
    color: #333;
  }

  .time-value {
    display: flex;
    align-items: center;

    text {
      font-size: 28rpx;
      color: #666;
    }

    .arrow {
      margin-left: 8rpx;
      font-size: 28rpx;
      color: #999;
    }
  }
}

/* 商户订单 */
.merchant-orders {
  margin: 20rpx;
}

.merchant-order {
  background-color: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;

  .order-header {
    padding: 24rpx;
    border-bottom: 1rpx solid #f0f0f0;
    display: flex;
    align-items: center;

    .order-badge {
      background-color: #22c55e;
      color: #fff;
      font-size: 22rpx;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      margin-right: 16rpx;
    }

    .merchant-info {
      display: flex;
      align-items: center;
      flex: 1;

      .merchant-logo {
        width: 40rpx;
        height: 40rpx;
        border-radius: 6rpx;
        margin-right: 12rpx;
      }

      .merchant-name {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
    }
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
      border-radius: 8rpx;
      margin-right: 20rpx;
    }

    .info {
      flex: 1;

      .name {
        font-size: 28rpx;
        color: #333;
        line-height: 1.4;
      }
    }

    .right {
      text-align: right;

      .price {
        font-size: 28rpx;
        color: #e53935;
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

/* 订单备注 */
.remark-row {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-top: 1rpx solid #f0f0f0;

  .label {
    font-size: 26rpx;
    color: #666;
    margin-right: 16rpx;
    white-space: nowrap;
  }

  input {
    flex: 1;
    font-size: 26rpx;
    text-align: right;
  }
}

/* 订单小计 */
.order-subtotal {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: #fafafa;
  font-size: 26rpx;
  color: #666;

  .amount {
    font-size: 30rpx;
    color: #e53935;
    font-weight: bold;
    margin-left: 8rpx;
  }
}

/* 金额明细 */
.amount-section {
  padding: 16rpx 30rpx;

  .amount-item {
    display: flex;
    justify-content: space-between;
    padding: 12rpx 0;
    font-size: 26rpx;
    color: #666;

    .discount {
      color: #22c55e;
    }

    &.total-row {
      border-top: 1rpx solid #f0f0f0;
      margin-top: 8rpx;
      padding-top: 16rpx;

      .order-count {
        color: #333;
        font-weight: 500;
      }
    }
  }
}

/* 底部结算 */
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

  .total {
    font-size: 28rpx;
    color: #333;

    .price {
      font-size: 40rpx;
      font-weight: bold;
      color: #e53935;
    }

    .order-info {
      font-size: 22rpx;
      color: #999;
      margin-left: 8rpx;
    }
  }

  .submit-btn {
    padding: 24rpx 60rpx;
    background-color: #22c55e;
    color: #fff;
    font-size: 32rpx;
    border-radius: 44rpx;

    &.disabled {
      background-color: #ccc;
    }
  }
}

/* 时间选择器遮罩 */
.time-picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

/* 时间选择器 */
.time-picker {
  width: 100%;
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding-bottom: env(safe-area-inset-bottom);

  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    text {
      font-size: 28rpx;
      color: #999;
    }

    .title {
      font-weight: bold;
      color: #333;
    }

    .confirm {
      color: #22c55e;
    }
  }

  .picker-content {
    max-height: 600rpx;
    overflow-y: auto;
  }

  .time-options {
    padding: 20rpx;

    .time-option {
      padding: 24rpx;
      margin-bottom: 16rpx;
      border: 2rpx solid #f0f0f0;
      border-radius: 16rpx;

      text {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .desc {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }

      &.active {
        border-color: #22c55e;
        background-color: rgba(34, 197, 94, 0.05);

        text {
          color: #22c55e;
        }
      }
    }
  }
}
</style>
