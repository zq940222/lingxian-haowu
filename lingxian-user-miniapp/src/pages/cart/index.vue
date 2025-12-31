<template>
  <view class="container">
    <!-- 空购物车 -->
    <view class="empty" v-if="!loading && cartStore.items.length === 0">
      <image src="/static/images/empty-cart.png" mode="aspectFit" />
      <text>购物车空空如也</text>
      <view class="btn" @click="goHome">去逛逛</view>
    </view>

    <!-- 购物车列表 -->
    <view class="cart-list" v-else>
      <view
        class="merchant-group"
        v-for="group in cartStore.groupedByMerchant"
        :key="group.merchantId"
      >
        <view class="merchant-header">
          <view class="checkbox" @click="toggleMerchant(group)">
            <uni-icons
              :type="isMerchantSelected(group) ? 'checkbox-filled' : 'circle'"
              :color="isMerchantSelected(group) ? '#07c160' : '#ccc'"
              size="22"
            />
          </view>
          <text class="name">{{ group.merchantName }}</text>
        </view>

        <view class="cart-item" v-for="item in group.items" :key="item.id">
          <view class="checkbox" @click="toggleItem(item)">
            <uni-icons
              :type="item.selected ? 'checkbox-filled' : 'circle'"
              :color="item.selected ? '#07c160' : '#ccc'"
              size="22"
            />
          </view>
          <image
            class="product-image"
            :src="item.productImage"
            mode="aspectFill"
            @click="goDetail(item.productId)"
          />
          <view class="product-info" @click="goDetail(item.productId)">
            <text class="name text-ellipsis-2">{{ item.productName }}</text>
            <text class="spec">{{ item.productSpec }}</text>
            <view class="bottom">
              <text class="price">¥{{ item.price }}</text>
              <view class="quantity-control">
                <view class="btn minus" @click.stop="decrease(item)">
                  <uni-icons type="minus" size="16" color="#666" />
                </view>
                <text class="num">{{ item.quantity }}</text>
                <view class="btn plus" @click.stop="increase(item)">
                  <uni-icons type="plus" size="16" color="#666" />
                </view>
              </view>
            </view>
          </view>
          <view class="delete" @click="removeItem(item)">
            <uni-icons type="trash" size="20" color="#999" />
          </view>
        </view>
      </view>
    </view>

    <!-- 底部结算栏 -->
    <view class="bottom-bar safe-area-bottom" v-if="cartStore.items.length > 0">
      <view class="select-all" @click="toggleSelectAll">
        <uni-icons
          :type="cartStore.isAllSelected ? 'checkbox-filled' : 'circle'"
          :color="cartStore.isAllSelected ? '#07c160' : '#ccc'"
          size="22"
        />
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
import { onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store/cart'
import { showConfirm } from '@/utils'

const cartStore = useCartStore()
const loading = cartStore.loading

onShow(() => {
  cartStore.fetchList()
})

// 判断商户是否全选
const isMerchantSelected = (group) => {
  return group.items.every(item => item.selected)
}

// 切换商户选中
const toggleMerchant = (group) => {
  const selected = !isMerchantSelected(group)
  group.items.forEach(item => {
    cartStore.select(item.id, selected)
  })
}

// 切换单个商品选中
const toggleItem = (item) => {
  cartStore.select(item.id, !item.selected)
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
    cartStore.remove(item.id)
  } catch (e) {
    // 取消
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
  padding-bottom: 120rpx;
}

/* 空购物车 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  image {
    width: 240rpx;
    height: 240rpx;
  }

  text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }

  .btn {
    margin-top: 40rpx;
    padding: 20rpx 80rpx;
    background-color: $primary-color;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
}

/* 购物车列表 */
.cart-list {
  padding: 20rpx;
}

.merchant-group {
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.merchant-header {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .checkbox {
    margin-right: 16rpx;
  }

  .name {
    font-size: 28rpx;
    color: #333;
    font-weight: bold;
  }
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .checkbox {
    margin-right: 16rpx;
  }

  .product-image {
    width: 160rpx;
    height: 160rpx;
    border-radius: $border-radius-base;
    margin-right: 20rpx;
  }

  .product-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 160rpx;

    .name {
      font-size: 28rpx;
      color: #333;
      line-height: 1.4;
    }

    .spec {
      font-size: 24rpx;
      color: #999;
    }

    .bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .price {
      font-size: 32rpx;
      color: $danger-color;
      font-weight: bold;
    }
  }

  .delete {
    padding: 20rpx;
    margin-left: 10rpx;
  }
}

/* 数量控制 */
.quantity-control {
  display: flex;
  align-items: center;

  .btn {
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f5f5;
    border-radius: 8rpx;
  }

  .num {
    width: 60rpx;
    text-align: center;
    font-size: 28rpx;
    color: #333;
  }
}

/* 底部结算栏 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  height: 100rpx;
  padding: 0 24rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

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
      color: $danger-color;
      font-weight: bold;
    }
  }

  .checkout-btn {
    padding: 0 40rpx;
    height: 72rpx;
    line-height: 72rpx;
    background-color: $primary-color;
    color: #fff;
    border-radius: 36rpx;
    font-size: 28rpx;

    &.disabled {
      background-color: #ccc;
    }
  }
}
</style>
