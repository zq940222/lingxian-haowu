<template>
  <view class="container">
    <!-- 收货地址 -->
    <view class="address-section" @click="selectAddress">
      <template v-if="selectedAddress">
        <uni-icons type="location-filled" size="22" color="#1890ff" />
        <view class="address-info">
          <view class="receiver">
            <text class="name">{{ selectedAddress.name }}</text>
            <text class="phone">{{ selectedAddress.phone }}</text>
          </view>
          <text class="address">{{ selectedAddress.fullAddress }}</text>
        </view>
      </template>
      <template v-else>
        <uni-icons type="plusempty" size="22" color="#1890ff" />
        <text class="add-text">添加收货地址</text>
      </template>
      <uni-icons type="right" size="16" color="#999" />
    </view>

    <!-- 配送时间 -->
    <view class="section delivery-time" @click="showTimePicker = true">
      <text class="label">配送时间</text>
      <view class="time-value">
        <text>{{ deliveryTimeText }}</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="section">
      <view class="section-header">
        <image :src="shopInfo.logo" mode="aspectFill" />
        <text>{{ shopInfo.name }}</text>
      </view>
      <view class="product-list">
        <view class="product-item" v-for="item in cartItems" :key="item.id">
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

    <!-- 订单备注 -->
    <view class="section remark-section">
      <text class="label">订单备注</text>
      <input type="text" v-model="remark" placeholder="选填，可填写您的特殊要求" />
    </view>

    <!-- 金额明细 -->
    <view class="section amount-section">
      <view class="amount-item">
        <text>商品金额</text>
        <text>¥{{ totalAmount }}</text>
      </view>
      <view class="amount-item">
        <text>配送费</text>
        <text>¥{{ deliveryFee }}</text>
      </view>
      <view class="amount-item" v-if="discountAmount > 0">
        <text>优惠</text>
        <text class="discount">-¥{{ discountAmount }}</text>
      </view>
    </view>

    <!-- 底部结算 -->
    <view class="footer">
      <view class="total">
        <text>合计：</text>
        <text class="price">¥{{ payAmount }}</text>
      </view>
      <view class="submit-btn" :class="{ disabled: !canSubmit }" @click="submitOrder">
        提交订单
      </view>
    </view>

    <!-- 配送时间选择 -->
    <uni-popup ref="timePopup" type="bottom" v-if="showTimePicker">
      <view class="time-picker">
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
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store/cart'
import { orderApi, addressApi, merchantApi } from '@/api'

const cartStore = useCartStore()

const selectedAddress = ref(null)
const shopInfo = ref({ logo: '', name: '' })
const cartItems = ref([])
const remark = ref('')
const deliveryTime = ref('now')
const showTimePicker = ref(false)
const deliveryFee = ref('0.00')
const discountAmount = ref(0)

// 时间段
const timeSlots = ref([])

onLoad((options) => {
  if (options.shopId) {
    loadShopInfo(options.shopId)
  }
  generateTimeSlots()
})

onShow(() => {
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

// 加载店铺信息
const loadShopInfo = async (shopId) => {
  try {
    const res = await merchantApi.getDetail(shopId)
    if (res.code === 200) {
      shopInfo.value = {
        id: res.data.id,
        logo: res.data.shopLogo,
        name: res.data.shopName
      }
      deliveryFee.value = res.data.deliveryFee || '0.00'
    }
  } catch (e) {
    console.error('加载店铺信息失败', e)
  }
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

// 加载购物车商品
const loadCartItems = () => {
  cartItems.value = cartStore.selectedItems
}

// 计算属性
const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => {
    return sum + parseFloat(item.price) * item.quantity
  }, 0).toFixed(2)
})

const payAmount = computed(() => {
  const total = parseFloat(totalAmount.value) + parseFloat(deliveryFee.value) - discountAmount.value
  return total.toFixed(2)
})

const deliveryTimeText = computed(() => {
  if (deliveryTime.value === 'now') {
    return '立即配送'
  }
  return deliveryTime.value
})

const canSubmit = computed(() => {
  return selectedAddress.value && cartItems.value.length > 0
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

// 提交订单
const submitOrder = async () => {
  if (!canSubmit.value) {
    if (!selectedAddress.value) {
      uni.showToast({ title: '请选择收货地址', icon: 'none' })
    }
    return
  }

  try {
    uni.showLoading({ title: '提交中...' })

    const orderData = {
      addressId: selectedAddress.value.id,
      shopId: shopInfo.value.id,
      products: cartItems.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      })),
      deliveryTime: deliveryTime.value === 'now' ? null : deliveryTime.value,
      remark: remark.value
    }

    const res = await orderApi.create(orderData)
    uni.hideLoading()

    if (res.code === 200) {
      // 清空已下单的购物车商品
      cartStore.clearSelectedItems()

      // 调用支付
      try {
        await uni.requestPayment(res.data.payParams)
        uni.redirectTo({ url: `/pages/order/detail?id=${res.data.orderId}` })
      } catch (e) {
        // 支付取消或失败，跳转到订单列表
        uni.redirectTo({ url: '/pages/order/list?status=0' })
      }
    }
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '提交失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

/* 收货地址 */
.address-section {
  display: flex;
  align-items: center;
  padding: 30rpx;
  margin: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  .address-info {
    flex: 1;
    margin-left: 20rpx;

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
    margin-left: 16rpx;
  }
}

/* 通用区块 */
.section {
  margin: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
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
  }
}

/* 店铺头部 */
.section-header {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  image {
    width: 48rpx;
    height: 48rpx;
    border-radius: 8rpx;
    margin-right: 16rpx;
  }

  text {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
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

/* 订单备注 */
.remark-section {
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;

  .label {
    font-size: 28rpx;
    color: #333;
    margin-right: 20rpx;
  }

  input {
    flex: 1;
    font-size: 28rpx;
    text-align: right;
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
      color: $success-color;
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
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));

  .total {
    font-size: 28rpx;
    color: #333;

    .price {
      font-size: 40rpx;
      font-weight: bold;
      color: $danger-color;
    }
  }

  .submit-btn {
    padding: 24rpx 60rpx;
    background-color: $primary-color;
    color: #fff;
    font-size: 32rpx;
    border-radius: 44rpx;

    &.disabled {
      background-color: #ccc;
    }
  }
}

/* 时间选择器 */
.time-picker {
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
      color: $primary-color;
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
      border-radius: $border-radius-lg;

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
        border-color: $primary-color;
        background-color: rgba(24, 144, 255, 0.05);

        text {
          color: $primary-color;
        }
      }
    }
  }
}
</style>
