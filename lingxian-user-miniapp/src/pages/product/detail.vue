<template>
  <view class="container">
    <!-- 商品轮播图 -->
    <swiper class="product-swiper" indicator-dots circular>
      <swiper-item v-for="(img, index) in images" :key="index">
        <image :src="img" mode="aspectFill" @click="previewImage(index)" />
      </swiper-item>
    </swiper>

    <!-- 商品信息 -->
    <view class="product-info">
      <view class="price-row">
        <text class="price">¥{{ product.price }}</text>
        <text class="original-price" v-if="product.originalPrice > product.price">
          ¥{{ product.originalPrice }}
        </text>
        <view class="group-tag" v-if="product.groupEnabled">
          <text>拼团价 ¥{{ product.groupPrice }}</text>
        </view>
      </view>
      <text class="name">{{ product.name }}</text>
      <text class="subtitle">{{ product.subtitle }}</text>
      <view class="tags">
        <text class="tag">{{ product.unit }}</text>
        <text class="tag" v-if="product.spec">{{ product.spec }}</text>
        <text class="tag">已售{{ product.sales }}</text>
      </view>
    </view>

    <!-- 配送信息 -->
    <view class="delivery-info">
      <view class="item">
        <text class="label">配送</text>
        <text class="value">由商家配送，预计30分钟内送达</text>
      </view>
      <view class="item" @click="goAddress">
        <text class="label">送至</text>
        <text class="value address">{{ address || '请选择收货地址' }}</text>
        <uni-icons type="right" size="14" color="#999" />
      </view>
    </view>

    <!-- 商品详情 -->
    <view class="detail-section">
      <view class="section-title">商品详情</view>
      <view class="description" v-if="product.description">
        <rich-text :nodes="product.description"></rich-text>
      </view>
      <view class="empty-desc" v-else>
        <text>暂无商品详情</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar safe-area-bottom">
      <view class="action-item" @click="goHome">
        <uni-icons type="home" size="24" color="#666" />
        <text>首页</text>
      </view>
      <view class="action-item" @click="goCart">
        <uni-icons type="cart" size="24" color="#666" />
        <text>购物车</text>
        <view class="badge" v-if="cartStore.totalCount > 0">
          {{ cartStore.totalCount > 99 ? '99+' : cartStore.totalCount }}
        </view>
      </view>
      <view class="btn-group">
        <view class="btn cart-btn" @click="addToCart">加入购物车</view>
        <view class="btn buy-btn" @click="buyNow">立即购买</view>
      </view>
    </view>

    <!-- 数量选择弹窗 -->
    <uni-popup ref="quantityPopup" type="bottom">
      <view class="quantity-popup">
        <view class="popup-header">
          <image :src="product.mainImage" mode="aspectFill" />
          <view class="info">
            <text class="price">¥{{ product.price }}</text>
            <text class="stock">库存：{{ product.stock }}</text>
          </view>
          <view class="close" @click="closePopup">
            <uni-icons type="closeempty" size="20" color="#999" />
          </view>
        </view>
        <view class="popup-body">
          <view class="quantity-row">
            <text class="label">数量</text>
            <view class="quantity-control">
              <view class="btn" @click="decreaseQuantity">
                <uni-icons type="minus" size="18" color="#666" />
              </view>
              <input type="number" v-model="quantity" class="input" />
              <view class="btn" @click="increaseQuantity">
                <uni-icons type="plus" size="18" color="#666" />
              </view>
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <view class="btn confirm-btn" @click="confirmAction">确定</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { productApi } from '@/api'
import { useCartStore } from '@/store/cart'

const cartStore = useCartStore()

const productId = ref('')
const product = ref({})
const quantity = ref(1)
const address = ref('')
const actionType = ref('cart') // cart | buy
const quantityPopup = ref(null)

const images = computed(() => {
  if (product.value.images) {
    try {
      return JSON.parse(product.value.images)
    } catch (e) {
      return [product.value.mainImage]
    }
  }
  return [product.value.mainImage]
})

onLoad((options) => {
  productId.value = options.id
  loadDetail()
  loadAddress()
})

// 加载商品详情
const loadDetail = async () => {
  try {
    const res = await productApi.getDetail(productId.value)
    if (res.code === 200) {
      product.value = res.data
    }
  } catch (e) {
    console.error('加载商品详情失败', e)
  }
}

// 加载默认地址
const loadAddress = () => {
  const addr = uni.getStorageSync('defaultAddress')
  if (addr) {
    address.value = addr.address
  }
}

// 预览图片
const previewImage = (index) => {
  uni.previewImage({
    urls: images.value,
    current: index
  })
}

// 选择地址
const goAddress = () => {
  uni.navigateTo({
    url: '/pages/address/list?select=1',
    events: {
      selectAddress: (addr) => {
        address.value = addr.address
      }
    }
  })
}

// 去首页
const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

// 去购物车
const goCart = () => {
  uni.switchTab({ url: '/pages/cart/index' })
}

// 显示数量弹窗
const showQuantityPopup = (type) => {
  actionType.value = type
  quantity.value = 1
  quantityPopup.value.open()
}

// 关闭弹窗
const closePopup = () => {
  quantityPopup.value.close()
}

// 减少数量
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

// 增加数量
const increaseQuantity = () => {
  if (quantity.value < product.value.stock) {
    quantity.value++
  }
}

// 加入购物车
const addToCart = () => {
  showQuantityPopup('cart')
}

// 立即购买
const buyNow = () => {
  showQuantityPopup('buy')
}

// 确认操作
const confirmAction = async () => {
  if (actionType.value === 'cart') {
    const success = await cartStore.add(product.value, quantity.value)
    if (success) {
      closePopup()
    }
  } else {
    closePopup()
    // 跳转确认订单
    uni.navigateTo({
      url: `/pages/order/confirm?productId=${productId.value}&quantity=${quantity.value}`
    })
  }
}
</script>

<style lang="scss" scoped>
.container {
  padding-bottom: 120rpx;
  background-color: #f5f5f5;
}

/* 轮播图 */
.product-swiper {
  height: 750rpx;
  background-color: #fff;

  image {
    width: 100%;
    height: 100%;
  }
}

/* 商品信息 */
.product-info {
  padding: 24rpx 30rpx;
  background-color: #fff;

  .price-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
  }

  .price {
    font-size: 48rpx;
    color: $danger-color;
    font-weight: bold;
  }

  .original-price {
    font-size: 28rpx;
    color: #999;
    text-decoration: line-through;
    margin-left: 16rpx;
  }

  .group-tag {
    margin-left: 16rpx;
    padding: 6rpx 16rpx;
    background-color: #fff0f0;
    border-radius: 8rpx;

    text {
      font-size: 24rpx;
      color: $danger-color;
    }
  }

  .name {
    display: block;
    font-size: 32rpx;
    color: #333;
    font-weight: bold;
    margin-top: 16rpx;
    line-height: 1.4;
  }

  .subtitle {
    display: block;
    font-size: 26rpx;
    color: #666;
    margin-top: 8rpx;
  }

  .tags {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16rpx;

    .tag {
      font-size: 22rpx;
      color: #999;
      padding: 4rpx 12rpx;
      background-color: #f5f5f5;
      border-radius: 4rpx;
      margin-right: 12rpx;
      margin-bottom: 8rpx;
    }
  }
}

/* 配送信息 */
.delivery-info {
  margin-top: 20rpx;
  padding: 24rpx 30rpx;
  background-color: #fff;

  .item {
    display: flex;
    align-items: center;
    padding: 12rpx 0;

    .label {
      font-size: 26rpx;
      color: #999;
      width: 80rpx;
    }

    .value {
      flex: 1;
      font-size: 26rpx;
      color: #333;

      &.address {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

/* 详情区域 */
.detail-section {
  margin-top: 20rpx;
  padding: 24rpx 30rpx;
  background-color: #fff;

  .section-title {
    font-size: 30rpx;
    color: #333;
    font-weight: bold;
    margin-bottom: 20rpx;
  }

  .description {
    font-size: 28rpx;
    color: #666;
    line-height: 1.6;
  }

  .empty-desc {
    text-align: center;
    padding: 60rpx 0;
    font-size: 26rpx;
    color: #999;
  }
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  height: 100rpx;
  padding: 0 20rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 24rpx;
    position: relative;

    text {
      font-size: 20rpx;
      color: #666;
      margin-top: 4rpx;
    }

    .badge {
      position: absolute;
      top: -8rpx;
      right: 8rpx;
      min-width: 32rpx;
      height: 32rpx;
      line-height: 32rpx;
      text-align: center;
      font-size: 20rpx;
      color: #fff;
      background-color: $danger-color;
      border-radius: 16rpx;
      padding: 0 8rpx;
    }
  }

  .btn-group {
    flex: 1;
    display: flex;
    margin-left: 20rpx;

    .btn {
      flex: 1;
      height: 72rpx;
      line-height: 72rpx;
      text-align: center;
      font-size: 28rpx;
      color: #fff;

      &.cart-btn {
        background-color: #ff9800;
        border-radius: 36rpx 0 0 36rpx;
      }

      &.buy-btn {
        background-color: $primary-color;
        border-radius: 0 36rpx 36rpx 0;
      }
    }
  }
}

/* 数量弹窗 */
.quantity-popup {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;

  .popup-header {
    display: flex;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    image {
      width: 180rpx;
      height: 180rpx;
      border-radius: $border-radius-base;
    }

    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      margin-left: 20rpx;

      .price {
        font-size: 40rpx;
        color: $danger-color;
        font-weight: bold;
      }

      .stock {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }

    .close {
      padding: 10rpx;
    }
  }

  .popup-body {
    padding: 30rpx;

    .quantity-row {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .label {
        font-size: 28rpx;
        color: #333;
      }
    }

    .quantity-control {
      display: flex;
      align-items: center;

      .btn {
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;
        border-radius: 8rpx;
      }

      .input {
        width: 80rpx;
        height: 56rpx;
        text-align: center;
        font-size: 28rpx;
      }
    }
  }

  .popup-footer {
    padding: 20rpx 30rpx 40rpx;

    .confirm-btn {
      height: 88rpx;
      line-height: 88rpx;
      text-align: center;
      background-color: $primary-color;
      color: #fff;
      font-size: 32rpx;
      border-radius: 44rpx;
    }
  }
}
</style>
