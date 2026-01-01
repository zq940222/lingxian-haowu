<template>
  <view class="container">
    <!-- 商户头部信息 -->
    <view class="merchant-header">
      <image class="banner" :src="merchant.banner || '/static/images/default-banner.png'" mode="aspectFill" />
      <view class="header-content">
        <image class="logo" :src="merchant.logo || '/static/images/default-shop.png'" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ merchant.name }}</text>
          <view class="rating-row" v-if="merchant.rating">
            <text class="rating-icon">★</text>
            <text class="rating-value">{{ merchant.rating }}</text>
            <text class="rating-label">评分</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 商户简介 -->
    <view class="merchant-intro">
      <view class="intro-item">
        <text class="label">简介</text>
        <text class="value">{{ merchant.description || '暂无简介' }}</text>
      </view>
      <view class="intro-item" v-if="merchant.address">
        <text class="label">地址</text>
        <text class="value">{{ fullAddress }}</text>
      </view>
      <view class="intro-item" v-if="merchant.contactPhone">
        <text class="label">电话</text>
        <text class="value phone" @click="callPhone">{{ merchant.contactPhone }}</text>
      </view>
    </view>

    <!-- 商品分类标签 -->
    <view class="category-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view
          class="tab-item"
          :class="{ active: currentCategory === '' }"
          @click="changeCategory('')"
        >
          <text>全部</text>
        </view>
        <view
          class="tab-item"
          v-for="cat in categories"
          :key="cat.id"
          :class="{ active: currentCategory === cat.id }"
          @click="changeCategory(cat.id)"
        >
          <text>{{ cat.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 商品列表 -->
    <view class="product-list">
      <view class="product-item" v-for="item in products" :key="item.id" @click="goDetail(item.id)">
        <image class="product-image" :src="item.image" mode="aspectFill" />
        <view class="product-info">
          <text class="product-name text-ellipsis-2">{{ item.name }}</text>
          <text class="product-desc text-ellipsis">{{ item.description }}</text>
          <view class="product-bottom">
            <view class="price-row">
              <text class="price">¥{{ item.price }}</text>
              <text class="original-price" v-if="item.originalPrice > item.price">¥{{ item.originalPrice }}</text>
            </view>
            <view class="add-cart" @click.stop="addToCart(item)">
              <text>+</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty" v-if="!loading && products.length === 0">
        <text>暂无商品</text>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { merchantApi } from '@/api'
import { useCartStore } from '@/store/cart'

const cartStore = useCartStore()

const merchantId = ref('')
const merchant = ref({})
const products = ref([])
const categories = ref([])
const currentCategory = ref('')
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

const fullAddress = computed(() => {
  const m = merchant.value
  return (m.province || '') + (m.city || '') + (m.district || '') + (m.address || '')
})

onLoad((options) => {
  merchantId.value = options.id
  loadMerchant()
  loadProducts()
})

// 加载商户信息
const loadMerchant = async () => {
  try {
    const res = await merchantApi.getDetail(merchantId.value)
    if (res.code === 200) {
      merchant.value = res.data
      uni.setNavigationBarTitle({ title: res.data.name || '商户详情' })
    }
  } catch (e) {
    console.error('加载商户信息失败', e)
  }
}

// 加载商品列表
const loadProducts = async (reset = false) => {
  if (loading.value) return
  if (!reset && !hasMore.value) return

  if (reset) {
    page.value = 1
    products.value = []
    hasMore.value = true
  }

  loading.value = true
  try {
    const res = await merchantApi.getProducts(merchantId.value, {
      page: page.value,
      pageSize: pageSize.value,
      categoryId: currentCategory.value || undefined
    })
    if (res.code === 200) {
      const list = res.data?.records || res.data || []
      if (reset) {
        products.value = list
      } else {
        products.value = [...products.value, ...list]
      }
      hasMore.value = list.length >= pageSize.value
      page.value++
    }
  } catch (e) {
    console.error('加载商品列表失败', e)
  } finally {
    loading.value = false
  }
}

// 切换分类
const changeCategory = (categoryId) => {
  currentCategory.value = categoryId
  loadProducts(true)
}

// 拨打电话
const callPhone = () => {
  if (merchant.value.contactPhone) {
    uni.makePhoneCall({
      phoneNumber: merchant.value.contactPhone
    })
  }
}

// 去商品详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

// 加入购物车
const addToCart = async (item) => {
  await cartStore.add(item, 1)
}

// 触底加载更多
onReachBottom(() => {
  loadProducts()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 商户头部 */
.merchant-header {
  position: relative;
  background-color: #fff;

  .banner {
    width: 100%;
    height: 300rpx;
  }

  .header-content {
    display: flex;
    align-items: flex-end;
    padding: 0 30rpx 30rpx;
    margin-top: -60rpx;
    position: relative;
  }

  .logo {
    width: 120rpx;
    height: 120rpx;
    border-radius: 16rpx;
    border: 4rpx solid #fff;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  }

  .info {
    flex: 1;
    margin-left: 20rpx;
    padding-bottom: 10rpx;

    .name {
      font-size: 36rpx;
      color: #333;
      font-weight: bold;
    }

    .rating-row {
      display: flex;
      align-items: center;
      margin-top: 8rpx;

      .rating-icon {
        font-size: 24rpx;
        color: #ff9800;
      }

      .rating-value {
        font-size: 28rpx;
        color: #ff9800;
        font-weight: bold;
        margin-left: 4rpx;
      }

      .rating-label {
        font-size: 24rpx;
        color: #999;
        margin-left: 8rpx;
      }
    }
  }
}

/* 商户简介 */
.merchant-intro {
  margin-top: 20rpx;
  padding: 24rpx 30rpx;
  background-color: #fff;

  .intro-item {
    display: flex;
    padding: 12rpx 0;

    .label {
      font-size: 26rpx;
      color: #999;
      width: 80rpx;
      flex-shrink: 0;
    }

    .value {
      flex: 1;
      font-size: 26rpx;
      color: #333;
      line-height: 1.5;

      &.phone {
        color: $primary-color;
      }
    }
  }
}

/* 分类标签 */
.category-tabs {
  margin-top: 20rpx;
  background-color: #fff;
  padding: 20rpx 0;

  .tabs-scroll {
    white-space: nowrap;
    padding: 0 20rpx;
  }

  .tab-item {
    display: inline-block;
    padding: 12rpx 32rpx;
    margin-right: 20rpx;
    font-size: 28rpx;
    color: #666;
    background-color: #f5f5f5;
    border-radius: 30rpx;

    &.active {
      background-color: rgba(34, 197, 94, 0.1);
      color: $primary-color;
    }
  }
}

/* 商品列表 */
.product-list {
  padding: 20rpx;

  .product-item {
    display: flex;
    background-color: #fff;
    border-radius: 16rpx;
    padding: 20rpx;
    margin-bottom: 20rpx;

    .product-image {
      width: 180rpx;
      height: 180rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }

    .product-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      margin-left: 20rpx;

      .product-name {
        font-size: 28rpx;
        color: #333;
        line-height: 1.4;
      }

      .product-desc {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
      }

      .product-bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: auto;
        padding-top: 16rpx;

        .price-row {
          display: flex;
          align-items: baseline;
        }

        .price {
          font-size: 32rpx;
          color: #e53935;
          font-weight: bold;
        }

        .original-price {
          font-size: 24rpx;
          color: #999;
          text-decoration: line-through;
          margin-left: 10rpx;
        }

        .add-cart {
          width: 48rpx;
          height: 48rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: $primary-color;
          border-radius: 50%;

          text {
            font-size: 36rpx;
            color: #fff;
            line-height: 1;
          }
        }
      }
    }
  }
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 100rpx 0;

  text {
    font-size: 28rpx;
    color: #999;
  }
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 30rpx 0;

  text {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
