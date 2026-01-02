<template>
  <view class="container">
    <!-- 未登录状态 -->
    <view class="not-login" v-if="!userStore.isLogin">
      <view class="empty-icon">
        <uni-icons type="cart" size="80" color="#ccc" />
      </view>
      <text class="tip">登录后可同步购物车</text>
      <button class="login-btn" open-type="getUserInfo" @click="handleLogin">
        微信授权登录
      </button>
    </view>

    <!-- 空购物车 -->
    <view class="empty" v-else-if="!loading && !cartStore.hasItems">
      <view class="empty-icon">
        <uni-icons type="cart" size="80" color="#ccc" />
      </view>
      <text>购物车空空如也</text>
      <view class="btn" @click="goHome">去逛逛</view>
    </view>

    <!-- 购物车列表 - 按商户分组 -->
    <view class="cart-list" v-else>
      <view class="merchant-group" v-for="group in cartStore.merchantGroups" :key="group.merchantId">
        <!-- 商户头部 -->
        <view class="merchant-header" @click="toggleMerchant(group)">
          <view class="checkbox" @click.stop="toggleMerchantSelect(group)">
            <view class="checkbox-icon" :class="{ checked: group.allSelected }">
              <text v-if="group.allSelected">✓</text>
            </view>
          </view>
          <image class="merchant-logo" :src="group.merchantLogo || '/static/images/default-shop.png'" mode="aspectFill" />
          <text class="merchant-name">{{ group.merchantName }}</text>
          <text class="merchant-arrow">›</text>
        </view>

        <!-- 商品列表 -->
        <view class="cart-item" v-for="item in group.items" :key="item.id">
          <view class="checkbox" @click="toggleItem(item)">
            <view class="checkbox-icon" :class="{ checked: item.selected }">
              <text v-if="item.selected">✓</text>
            </view>
          </view>
          <image
            class="product-image"
            :src="item.productImage"
            mode="aspectFill"
            @click="goDetail(item.productId)"
          />
          <view class="product-info">
            <text class="name text-ellipsis-2" @click="goDetail(item.productId)">{{ item.productName }}</text>
            <view class="bottom">
              <text class="price">¥{{ item.price }}</text>
              <view class="quantity-control">
                <view class="btn minus" @click.stop="decrease(item)">
                  <text>－</text>
                </view>
                <text class="num">{{ item.quantity }}</text>
                <view class="btn plus" @click.stop="increase(item)">
                  <text>＋</text>
                </view>
              </view>
            </view>
          </view>
          <view class="delete" @click="removeItem(item)">
            <text class="delete-icon">×</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部结算栏 -->
    <view class="bottom-bar safe-area-bottom" v-if="cartStore.hasItems">
      <view class="select-all" @click="toggleSelectAll">
        <view class="checkbox-icon" :class="{ checked: cartStore.isAllSelected }">
          <text v-if="cartStore.isAllSelected">✓</text>
        </view>
        <text>全选</text>
      </view>
      <view class="total">
        <text>合计：</text>
        <text class="amount">¥{{ cartStore.selectedAmount.toFixed(2) }}</text>
      </view>
      <view
        class="checkout-btn"
        :class="{ disabled: cartStore.selectedCount === 0 }"
        @click="checkout"
      >
        结算({{ cartStore.selectedCount }})
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import { showConfirm } from '@/utils'

const cartStore = useCartStore()
const userStore = useUserStore()
const loading = ref(false)

onShow(() => {
  // 检查登录状态
  userStore.checkLoginStatus()
  // 已登录则加载购物车
  if (userStore.isLogin) {
    loadCart()
  }
})

// 加载购物车
const loadCart = async () => {
  loading.value = true
  try {
    await cartStore.fetchList()
  } finally {
    loading.value = false
  }
}

// 微信登录
const handleLogin = async () => {
  uni.showLoading({ title: '登录中...' })
  try {
    await userStore.wxLogin()
    uni.hideLoading()
    uni.showToast({ title: '登录成功', icon: 'success' })
    // 登录成功后加载购物车
    loadCart()
  } catch (e) {
    uni.hideLoading()
    console.error('登录失败:', e)
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  }
}

// 切换单个商品选中
const toggleItem = (item) => {
  cartStore.select(item.id, !item.selected)
}

// 切换商户全选
const toggleMerchantSelect = (group) => {
  cartStore.selectMerchant(group.merchantId, !group.allSelected)
}

// 跳转到商户页面
const toggleMerchant = (group) => {
  uni.navigateTo({ url: `/pages/merchant/detail?id=${group.merchantId}` })
}

// 全选/取消全选
const toggleSelectAll = () => {
  cartStore.selectAll(!cartStore.isAllSelected)
}

// 增加数量
const increase = (item) => {
  cartStore.updateQuantity(item.id, item.quantity + 1)
}

// 减少数量
const decrease = (item) => {
  if (item.quantity <= 1) {
    removeItem(item)
  } else {
    cartStore.updateQuantity(item.id, item.quantity - 1)
  }
}

// 删除商品
const removeItem = async (item) => {
  try {
    await showConfirm('确定删除该商品吗？')
    await cartStore.remove(item.id)
  } catch (e) {
    // 取消删除
  }
}

// 去首页
const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

// 去商品详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

// 结算
const checkout = () => {
  if (cartStore.selectedCount === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  uni.navigateTo({ url: '/pages/order/confirm' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  /* 底部需要留出足够空间：结算栏100rpx + tabBar约100rpx + 安全区域 */
  padding-bottom: calc(220rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(220rpx + env(safe-area-inset-bottom));
}

/* 未登录状态 */
.not-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-icon {
    margin-bottom: 20rpx;
  }

  .tip {
    font-size: 28rpx;
    color: #999;
  }

  .login-btn {
    margin-top: 40rpx;
    padding: 0 60rpx;
    height: 80rpx;
    line-height: 80rpx;
    background-color: #22c55e;
    color: #fff;
    border-radius: 40rpx;
    font-size: 30rpx;
    border: none;

    &::after {
      border: none;
    }
  }
}

/* 空购物车 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-icon {
    margin-bottom: 20rpx;
  }

  text {
    font-size: 28rpx;
    color: #999;
  }

  .btn {
    margin-top: 40rpx;
    padding: 20rpx 80rpx;
    background-color: #22c55e;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
}

/* 购物车列表 */
.cart-list {
  padding: 20rpx;
}

/* 商户分组 */
.merchant-group {
  background-color: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

/* 商户头部 */
.merchant-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;

  .checkbox {
    margin-right: 16rpx;
  }

  .merchant-logo {
    width: 48rpx;
    height: 48rpx;
    border-radius: 8rpx;
    margin-right: 12rpx;
  }

  .merchant-name {
    flex: 1;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
  }

  .merchant-arrow {
    font-size: 32rpx;
    color: #999;
  }
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  .checkbox {
    margin-right: 16rpx;
  }

  .product-image {
    width: 160rpx;
    height: 160rpx;
    border-radius: 12rpx;
    margin-right: 20rpx;
    flex-shrink: 0;
  }

  .product-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-height: 140rpx;

    .name {
      font-size: 28rpx;
      color: #333;
      line-height: 1.4;
    }

    .bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16rpx;
    }

    .price {
      font-size: 32rpx;
      color: #e53935;
      font-weight: bold;
    }
  }

  .delete {
    padding: 20rpx;
    margin-left: 10rpx;
  }
}

/* 自定义复选框 */
.checkbox-icon {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #fff;

  &.checked {
    background-color: #22c55e;
    border-color: #22c55e;
  }
}

/* 删除按钮 */
.delete-icon {
  font-size: 40rpx;
  color: #999;
  font-weight: 300;
}

/* 数量控制 */
.quantity-control {
  display: flex;
  align-items: center;
  border: 1rpx solid #eee;
  border-radius: 8rpx;

  .btn {
    width: 52rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f8f8f8;

    text {
      font-size: 32rpx;
      color: #666;
      line-height: 1;
    }

    &.minus {
      border-radius: 8rpx 0 0 8rpx;
    }

    &.plus {
      border-radius: 0 8rpx 8rpx 0;
    }
  }

  .num {
    width: 70rpx;
    text-align: center;
    font-size: 28rpx;
    color: #333;
    background-color: #fff;
  }
}

/* 底部结算栏 - 需要在tabBar上方显示 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 100rpx; /* tabBar高度约100rpx，结算栏显示在tabBar上方 */
  display: flex;
  align-items: center;
  height: 100rpx;
  padding: 0 24rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 99;

  .select-all {
    display: flex;
    align-items: center;

    text {
      font-size: 28rpx;
      color: #333;
      margin-left: 8rpx;
    }
  }

  .total {
    flex: 1;
    text-align: right;
    margin-right: 20rpx;

    text {
      font-size: 28rpx;
      color: #333;
    }

    .amount {
      font-size: 36rpx;
      color: #e53935;
      font-weight: bold;
    }
  }

  .checkout-btn {
    padding: 0 40rpx;
    height: 72rpx;
    line-height: 72rpx;
    background-color: #22c55e;
    color: #fff;
    border-radius: 36rpx;
    font-size: 28rpx;

    &.disabled {
      background-color: #ccc;
    }
  }
}
</style>
