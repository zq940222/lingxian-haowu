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
      <text class="subtitle">{{ product.description }}</text>
      <view class="tags">
        <text class="tag" v-if="product.unit">{{ product.unit }}</text>
        <text class="tag">已售{{ product.sales || 0 }}</text>
      </view>
    </view>

    <!-- 商户信息 -->
    <view class="merchant-card" v-if="product.merchant" @click="goMerchant">
      <image class="merchant-logo" :src="product.merchant.logo || '/static/images/default-shop.png'" mode="aspectFill" />
      <view class="merchant-info">
        <view class="merchant-name-row">
          <text class="merchant-name">{{ product.merchant.name }}</text>
          <view class="merchant-rating" v-if="product.merchant.rating">
            <text class="rating-icon">★</text>
            <text class="rating-value">{{ product.merchant.rating }}</text>
          </view>
        </view>
        <text class="merchant-desc">{{ product.merchant.description || '暂无简介' }}</text>
      </view>
      <view class="enter-shop">
        <text>进店</text>
        <text class="arrow">›</text>
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
        <text class="arrow">›</text>
      </view>
    </view>

    <!-- 用户评价 -->
    <view class="comment-section">
      <view class="section-header" @click="goCommentList">
        <view class="left">
          <text class="title">用户评价</text>
          <text class="count">({{ product.commentStats?.total || 0 }})</text>
        </view>
        <view class="right">
          <text class="rate">好评率 {{ product.commentStats?.goodRate || 100 }}%</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 评价统计 -->
      <view class="comment-stats" v-if="product.commentStats?.total > 0">
        <view class="stat-item">
          <text class="stat-value">{{ product.commentStats?.avgRating || '5.0' }}</text>
          <text class="stat-label">综合评分</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ product.commentStats?.goodRate || 100 }}%</text>
          <text class="stat-label">好评率</text>
        </view>
      </view>

      <!-- 评价列表 -->
      <view class="comment-list" v-if="product.comments && product.comments.length > 0">
        <view class="comment-item" v-for="comment in product.comments" :key="comment.id">
          <view class="comment-header">
            <image class="user-avatar" :src="comment.userAvatar || '/static/images/default-avatar.png'" mode="aspectFill" />
            <view class="user-info">
              <text class="user-name">{{ comment.userName }}</text>
              <view class="rating-stars">
                <text v-for="i in 5" :key="i" :class="['star', i <= comment.rating ? 'active' : '']">★</text>
              </view>
            </view>
            <text class="comment-time">{{ comment.createTime }}</text>
          </view>
          <text class="comment-content">{{ comment.content }}</text>
          <!-- 评价图片 -->
          <view class="comment-images" v-if="comment.images && comment.images.length > 0">
            <image
              v-for="(img, idx) in comment.images"
              :key="idx"
              :src="img"
              mode="aspectFill"
              @click="previewCommentImage(comment.images, idx)"
            />
          </view>
          <!-- 商家回复 -->
          <view class="merchant-reply" v-if="comment.replyContent">
            <text class="reply-label">商家回复：</text>
            <text class="reply-content">{{ comment.replyContent }}</text>
          </view>
        </view>

        <!-- 查看更多评价 -->
        <view class="view-more" @click="goCommentList" v-if="product.commentStats?.total > 3">
          <text>查看全部{{ product.commentStats?.total }}条评价</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 暂无评价 -->
      <view class="no-comment" v-else>
        <text>暂无评价，快来抢沙发吧~</text>
      </view>
    </view>

    <!-- 商品详情 -->
    <view class="detail-section">
      <view class="section-title">商品详情</view>
      <view class="description" v-if="product.detail">
        <rich-text :nodes="product.detail"></rich-text>
      </view>
      <view class="empty-desc" v-else>
        <text>暂无商品详情</text>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar safe-area-bottom">
      <view class="action-item" @click="goHome">
        <text class="action-icon">🏠</text>
        <text>首页</text>
      </view>
      <view class="action-item" @click="goCart">
        <text class="action-icon">🛒</text>
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
          <image :src="product.mainImage || product.image" mode="aspectFill" />
          <view class="info">
            <text class="price">¥{{ product.price }}</text>
            <text class="stock">库存：{{ product.stock }}</text>
          </view>
          <view class="close" @click="closePopup">
            <text>×</text>
          </view>
        </view>
        <view class="popup-body">
          <view class="quantity-row">
            <text class="label">数量</text>
            <view class="quantity-control">
              <view class="btn" @click="decreaseQuantity">
                <text>－</text>
              </view>
              <input type="number" v-model="quantity" class="input" />
              <view class="btn" @click="increaseQuantity">
                <text>＋</text>
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
import { ref, computed } from 'vue'
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
      const imgs = typeof product.value.images === 'string'
        ? JSON.parse(product.value.images)
        : product.value.images
      return imgs.length > 0 ? imgs : [product.value.mainImage || product.value.image]
    } catch (e) {
      return [product.value.mainImage || product.value.image]
    }
  }
  return [product.value.mainImage || product.value.image]
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

// 预览商品图片
const previewImage = (index) => {
  uni.previewImage({
    urls: images.value,
    current: index
  })
}

// 预览评价图片
const previewCommentImage = (imgs, index) => {
  uni.previewImage({
    urls: imgs,
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

// 去商户详情
const goMerchant = () => {
  if (product.value.merchant?.id) {
    uni.navigateTo({ url: `/pages/merchant/detail?id=${product.value.merchant.id}` })
  }
}

// 去购物车
const goCart = () => {
  uni.switchTab({ url: '/pages/cart/index' })
}

// 去评价列表
const goCommentList = () => {
  uni.navigateTo({ url: `/pages/product/comments?id=${productId.value}` })
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
  padding-bottom: calc(120rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
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
    color: #e53935;
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
      color: #e53935;
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

/* 商户卡片 */
.merchant-card {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  padding: 24rpx 30rpx;
  background-color: #fff;

  .merchant-logo {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
    flex-shrink: 0;
  }

  .merchant-info {
    flex: 1;
    margin-left: 20rpx;
    overflow: hidden;

    .merchant-name-row {
      display: flex;
      align-items: center;
    }

    .merchant-name {
      font-size: 30rpx;
      color: #333;
      font-weight: 500;
    }

    .merchant-rating {
      display: flex;
      align-items: center;
      margin-left: 16rpx;
      padding: 2rpx 10rpx;
      background-color: #fff7e6;
      border-radius: 8rpx;

      .rating-icon {
        font-size: 22rpx;
        color: #ff9800;
      }

      .rating-value {
        font-size: 22rpx;
        color: #ff9800;
        margin-left: 4rpx;
      }
    }

    .merchant-desc {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .enter-shop {
    display: flex;
    align-items: center;
    padding: 12rpx 24rpx;
    background-color: $primary-color;
    border-radius: 30rpx;
    flex-shrink: 0;

    text {
      font-size: 24rpx;
      color: #fff;
    }

    .arrow {
      margin-left: 4rpx;
      font-size: 28rpx;
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

    .arrow {
      font-size: 28rpx;
      color: #999;
    }
  }
}

/* 评价区域 */
.comment-section {
  margin-top: 20rpx;
  padding: 24rpx 30rpx;
  background-color: #fff;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 20rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .left {
      display: flex;
      align-items: center;

      .title {
        font-size: 30rpx;
        color: #333;
        font-weight: bold;
      }

      .count {
        font-size: 26rpx;
        color: #999;
        margin-left: 8rpx;
      }
    }

    .right {
      display: flex;
      align-items: center;

      .rate {
        font-size: 26rpx;
        color: #ff9800;
      }

      .arrow {
        font-size: 28rpx;
        color: #999;
        margin-left: 8rpx;
      }
    }
  }

  .comment-stats {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 30rpx 0;
    background-color: #fafafa;
    border-radius: 12rpx;
    margin-top: 20rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0 60rpx;

      .stat-value {
        font-size: 40rpx;
        color: #ff9800;
        font-weight: bold;
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }

    .stat-divider {
      width: 1rpx;
      height: 60rpx;
      background-color: #ddd;
    }
  }

  .comment-list {
    margin-top: 20rpx;
  }

  .comment-item {
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .comment-header {
      display: flex;
      align-items: center;

      .user-avatar {
        width: 64rpx;
        height: 64rpx;
        border-radius: 50%;
      }

      .user-info {
        flex: 1;
        margin-left: 16rpx;

        .user-name {
          font-size: 26rpx;
          color: #333;
        }

        .rating-stars {
          margin-top: 4rpx;

          .star {
            font-size: 22rpx;
            color: #ddd;

            &.active {
              color: #ff9800;
            }
          }
        }
      }

      .comment-time {
        font-size: 22rpx;
        color: #999;
      }
    }

    .comment-content {
      display: block;
      font-size: 28rpx;
      color: #333;
      line-height: 1.6;
      margin-top: 16rpx;
    }

    .comment-images {
      display: flex;
      flex-wrap: wrap;
      margin-top: 16rpx;

      image {
        width: 160rpx;
        height: 160rpx;
        border-radius: 8rpx;
        margin-right: 12rpx;
        margin-bottom: 12rpx;
      }
    }

    .merchant-reply {
      margin-top: 16rpx;
      padding: 16rpx;
      background-color: #f5f5f5;
      border-radius: 8rpx;

      .reply-label {
        font-size: 24rpx;
        color: #ff9800;
      }

      .reply-content {
        font-size: 24rpx;
        color: #666;
      }
    }
  }

  .view-more {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 24rpx 0;

    text {
      font-size: 26rpx;
      color: #666;
    }

    .arrow {
      font-size: 28rpx;
      color: #999;
      margin-left: 8rpx;
    }
  }

  .no-comment {
    text-align: center;
    padding: 60rpx 0;

    text {
      font-size: 26rpx;
      color: #999;
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

    .action-icon {
      font-size: 36rpx;
    }

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
      background-color: #e53935;
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
      border-radius: 12rpx;
    }

    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      margin-left: 20rpx;

      .price {
        font-size: 40rpx;
        color: #e53935;
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

      text {
        font-size: 40rpx;
        color: #999;
      }
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

        text {
          font-size: 32rpx;
          color: #666;
        }
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
    padding: 20rpx 30rpx;
    padding-bottom: calc(40rpx + constant(safe-area-inset-bottom));
    padding-bottom: calc(40rpx + env(safe-area-inset-bottom));

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
