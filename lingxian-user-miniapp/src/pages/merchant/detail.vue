<template>
  <view class="container">
    <!-- 商户头部信息 -->
    <view class="merchant-header">
      <!-- 背景图 -->
      <view class="header-bg">
        <image
          class="banner"
          :src="merchant.banner || '/static/images/default-banner.png'"
          mode="aspectFill"
        />
        <view class="bg-mask"></view>
      </view>

      <!-- 商户信息卡片 -->
      <view class="merchant-card">
        <view class="card-top">
          <image class="logo" :src="merchant.logo || '/static/images/default-shop.png'" mode="aspectFill" />
          <view class="info">
            <view class="name-row">
              <text class="name">{{ merchant.name }}</text>
              <view class="status-tag" :class="{ closed: merchant.status !== 1 }">
                {{ merchant.status === 1 ? '营业中' : '休息中' }}
              </view>
            </view>
            <view class="desc" v-if="merchant.description">{{ merchant.description }}</view>
          </view>
        </view>

        <!-- 统计信息 -->
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-value">{{ merchant.rating || '5.0' }}</text>
            <text class="stat-label">评分</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ products.length || 0 }}+</text>
            <text class="stat-label">商品</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ categories.length || 0 }}</text>
            <text class="stat-label">分类</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="action-bar" v-if="merchant.contactPhone || merchant.address">
      <view class="action-item" @click="callPhone" v-if="merchant.contactPhone">
        <view class="action-icon phone-icon">
          <view class="icon-phone"></view>
        </view>
        <text class="action-text">联系商家</text>
      </view>
      <view class="action-item" @click="openLocation" v-if="merchant.address">
        <view class="action-icon location-icon">
          <view class="icon-location"></view>
        </view>
        <text class="action-text">店铺地址</text>
      </view>
    </view>

    <!-- 商品区域 -->
    <view class="product-section">
      <!-- 标题和分类 -->
      <view class="section-header">
        <text class="section-title">店铺商品</text>
        <text class="product-count">共{{ totalProducts }}件</text>
      </view>

      <!-- 分类标签 -->
      <view class="category-tabs" v-if="categories.length > 0">
        <scroll-view scroll-x class="tabs-scroll" :scroll-into-view="scrollIntoView">
          <view
            :id="'tab-all'"
            class="tab-item"
            :class="{ active: currentCategory === '' }"
            @click="changeCategory('')"
          >
            全部
          </view>
          <view
            v-for="cat in categories"
            :key="cat.id"
            :id="'tab-' + cat.id"
            class="tab-item"
            :class="{ active: currentCategory === cat.id }"
            @click="changeCategory(cat.id)"
          >
            {{ cat.name }}
          </view>
        </scroll-view>
      </view>

      <!-- 商品列表 -->
      <view class="product-list">
        <view
          class="product-item"
          v-for="item in products"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <view class="product-image-wrap">
            <image class="product-image" :src="item.image" mode="aspectFill" />
            <view class="sold-tag" v-if="item.salesCount > 0">
              已售{{ item.salesCount > 999 ? '999+' : item.salesCount }}
            </view>
          </view>
          <view class="product-info">
            <text class="product-name">{{ item.name }}</text>
            <text class="product-desc" v-if="item.description">{{ item.description }}</text>
            <view class="product-bottom">
              <view class="price-info">
                <text class="price-symbol">¥</text>
                <text class="price">{{ item.price }}</text>
                <text class="original-price" v-if="item.originalPrice > item.price">¥{{ item.originalPrice }}</text>
              </view>
              <view class="add-cart" @click.stop="addToCart(item)">
                <text class="add-icon">+</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-state" v-if="loading">
        <view class="loading-spinner"></view>
        <text>加载中...</text>
      </view>

      <!-- 加载完成 -->
      <view class="no-more" v-if="!loading && !hasMore && products.length > 0">
        <view class="line"></view>
        <text>已经到底了</text>
        <view class="line"></view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="!loading && products.length === 0">
        <image src="/static/images/empty-product.png" mode="aspectFit" />
        <text class="empty-text">暂无商品</text>
        <text class="empty-hint">店家正在努力上架中~</text>
      </view>
    </view>

    <!-- 底部安全区 -->
    <view class="safe-bottom"></view>
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
const totalProducts = ref(0)
const scrollIntoView = ref('')

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
      categories.value = res.data.categories || []
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
      totalProducts.value = res.data?.total || list.length
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
  scrollIntoView.value = categoryId ? `tab-${categoryId}` : 'tab-all'
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

// 打开地图或显示地址
const openLocation = () => {
  const lat = merchant.value.latitude
  const lng = merchant.value.longitude

  // 有经纬度则打开地图导航
  if (lat && lng && parseFloat(lat) !== 0 && parseFloat(lng) !== 0) {
    uni.openLocation({
      latitude: parseFloat(lat),
      longitude: parseFloat(lng),
      name: merchant.value.name,
      address: fullAddress.value
    })
  } else if (fullAddress.value) {
    // 无经纬度但有地址，显示地址并提供复制选项
    uni.showModal({
      title: '店铺地址',
      content: fullAddress.value,
      confirmText: '复制地址',
      success: (res) => {
        if (res.confirm) {
          uni.setClipboardData({
            data: fullAddress.value,
            success: () => {
              uni.showToast({ title: '地址已复制', icon: 'success' })
            }
          })
        }
      }
    })
  }
}

// 去商品详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

// 加入购物车
const addToCart = async (item) => {
  await cartStore.add({
    ...item,
    merchantId: merchantId.value,
    merchantName: merchant.value.name
  }, 1)
  uni.showToast({ title: '已加入购物车', icon: 'success' })
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
  padding-bottom: 30rpx;

  .header-bg {
    position: relative;
    height: 320rpx;

    .banner {
      width: 100%;
      height: 100%;
    }

    .bg-mask {
      position: absolute;
      left: 0;
      right: 0;
      bottom: 0;
      height: 160rpx;
      background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.3));
    }
  }

  .merchant-card {
    position: relative;
    margin: -80rpx 24rpx 0;
    padding: 30rpx;
    background: #fff;
    border-radius: 24rpx;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);

    .card-top {
      display: flex;

      .logo {
        width: 120rpx;
        height: 120rpx;
        border-radius: 20rpx;
        flex-shrink: 0;
        border: 4rpx solid #fff;
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
      }

      .info {
        flex: 1;
        margin-left: 24rpx;
        overflow: hidden;

        .name-row {
          display: flex;
          align-items: center;

          .name {
            font-size: 36rpx;
            font-weight: bold;
            color: #333;
            max-width: 360rpx;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .status-tag {
            margin-left: 16rpx;
            padding: 4rpx 16rpx;
            font-size: 22rpx;
            color: #fff;
            background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
            border-radius: 20rpx;

            &.closed {
              background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
            }
          }
        }

        .desc {
          margin-top: 12rpx;
          font-size: 26rpx;
          color: #666;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          line-height: 1.5;
        }
      }
    }

    .stats-row {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-top: 30rpx;
      padding-top: 30rpx;
      border-top: 1rpx solid #f0f0f0;

      .stat-item {
        flex: 1;
        text-align: center;

        .stat-value {
          display: block;
          font-size: 36rpx;
          font-weight: bold;
          color: #333;
        }

        .stat-label {
          display: block;
          margin-top: 8rpx;
          font-size: 24rpx;
          color: #999;
        }
      }

      .stat-divider {
        width: 1rpx;
        height: 60rpx;
        background-color: #f0f0f0;
      }
    }
  }
}

/* 功能入口 */
.action-bar {
  display: flex;
  margin: 0 24rpx 20rpx;
  padding: 30rpx 20rpx;
  background: #fff;
  border-radius: 20rpx;

  .action-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .action-icon {
      width: 72rpx;
      height: 72rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      margin-bottom: 12rpx;

      &.phone-icon {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);

        .icon-phone {
          width: 24rpx;
          height: 24rpx;
          border: 3rpx solid #fff;
          border-radius: 4rpx;
          position: relative;
          transform: rotate(-20deg);

          &::before {
            content: '';
            position: absolute;
            width: 8rpx;
            height: 8rpx;
            background: #fff;
            border-radius: 50%;
            bottom: 3rpx;
            left: 50%;
            transform: translateX(-50%);
          }
        }
      }

      &.location-icon {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);

        .icon-location {
          width: 20rpx;
          height: 20rpx;
          border: 3rpx solid #fff;
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          position: relative;

          &::before {
            content: '';
            position: absolute;
            width: 6rpx;
            height: 6rpx;
            background: #fff;
            border-radius: 50%;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
          }
        }
      }
    }

    .action-text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

/* 商品区域 */
.product-section {
  margin: 0 24rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx 20rpx 0 0;
  min-height: 400rpx;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      position: relative;
      padding-left: 20rpx;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 6rpx;
        height: 32rpx;
        background: $primary-color;
        border-radius: 3rpx;
      }
    }

    .product-count {
      font-size: 24rpx;
      color: #999;
    }
  }

  .category-tabs {
    margin-bottom: 24rpx;

    .tabs-scroll {
      white-space: nowrap;
    }

    .tab-item {
      display: inline-block;
      padding: 14rpx 32rpx;
      margin-right: 20rpx;
      font-size: 26rpx;
      color: #666;
      background-color: #f5f5f5;
      border-radius: 30rpx;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
        color: #fff;
        font-weight: 500;
      }
    }
  }
}

/* 商品列表 */
.product-list {
  .product-item {
    display: flex;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    .product-image-wrap {
      position: relative;
      width: 180rpx;
      height: 180rpx;
      flex-shrink: 0;

      .product-image {
        width: 100%;
        height: 100%;
        border-radius: 16rpx;
      }

      .sold-tag {
        position: absolute;
        left: 0;
        bottom: 0;
        padding: 4rpx 12rpx;
        font-size: 20rpx;
        color: #fff;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 0 16rpx 0 16rpx;
      }
    }

    .product-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      margin-left: 20rpx;
      overflow: hidden;

      .product-name {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        line-height: 1.4;
      }

      .product-desc {
        margin-top: 8rpx;
        font-size: 24rpx;
        color: #999;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: auto;
        padding-top: 16rpx;

        .price-info {
          display: flex;
          align-items: baseline;

          .price-symbol {
            font-size: 24rpx;
            color: #e53935;
            font-weight: bold;
          }

          .price {
            font-size: 36rpx;
            color: #e53935;
            font-weight: bold;
          }

          .original-price {
            margin-left: 12rpx;
            font-size: 24rpx;
            color: #bbb;
            text-decoration: line-through;
          }
        }

        .add-cart {
          width: 56rpx;
          height: 56rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
          border-radius: 50%;
          box-shadow: 0 4rpx 12rpx rgba(34, 197, 94, 0.3);

          .add-icon {
            font-size: 40rpx;
            color: #fff;
            font-weight: 300;
            line-height: 1;
          }
        }
      }
    }
  }
}

/* 加载状态 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 0;

  .loading-spinner {
    width: 32rpx;
    height: 32rpx;
    border: 4rpx solid #f0f0f0;
    border-top-color: $primary-color;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-right: 16rpx;
  }

  text {
    font-size: 26rpx;
    color: #999;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 加载完成 */
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx 0;

  .line {
    width: 80rpx;
    height: 1rpx;
    background-color: #e0e0e0;
  }

  text {
    margin: 0 20rpx;
    font-size: 24rpx;
    color: #bbb;
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;

  image {
    width: 240rpx;
    height: 240rpx;
    opacity: 0.6;
  }

  .empty-text {
    margin-top: 20rpx;
    font-size: 30rpx;
    color: #666;
  }

  .empty-hint {
    margin-top: 12rpx;
    font-size: 26rpx;
    color: #999;
  }
}

/* 底部安全区 */
.safe-bottom {
  height: env(safe-area-inset-bottom);
}
</style>
