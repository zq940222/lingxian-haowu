<template>
  <view class="container">
    <!-- 头部：定位和搜索 -->
    <view class="header">
      <view class="location" @click="chooseLocation">
        <uni-icons type="location" size="18" color="#fff" />
        <text class="text">{{ location || '选择地址' }}</text>
        <uni-icons type="down" size="14" color="#fff" />
      </view>
      <view class="search" @click="goSearch">
        <uni-icons type="search" size="18" color="#999" />
        <text class="placeholder">搜索商品</text>
      </view>
    </view>

    <!-- 轮播图 -->
    <swiper class="banner" indicator-dots autoplay circular :interval="3000">
      <swiper-item v-for="item in banners" :key="item.id" @click="onBannerTap(item)">
        <image class="banner-image" :src="item.imageUrl" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 分类入口 -->
    <view class="category-nav">
      <view
        class="category-item"
        v-for="item in categories"
        :key="item.id"
        @click="goCategory(item)"
      >
        <image class="icon" :src="item.icon" mode="aspectFit" />
        <text class="name">{{ item.name }}</text>
      </view>
    </view>

    <!-- 拼团专区 -->
    <view class="section" v-if="groupActivities.length > 0">
      <view class="section-header">
        <text class="title">限时拼团</text>
        <view class="more" @click="goGroupList">
          <text>更多</text>
          <uni-icons type="right" size="14" color="#999" />
        </view>
      </view>
      <scroll-view class="group-list" scroll-x>
        <view
          class="group-item"
          v-for="item in groupActivities"
          :key="item.id"
          @click="goGroupDetail(item.id)"
        >
          <image class="product-image" :src="item.productImage" mode="aspectFill" />
          <view class="info">
            <text class="name text-ellipsis">{{ item.productName }}</text>
            <view class="price-row">
              <text class="group-price">¥{{ item.groupPrice }}</text>
              <text class="original-price">¥{{ item.originalPrice }}</text>
            </view>
            <view class="group-tag">
              <text>{{ item.groupSize }}人团</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 推荐商品 -->
    <view class="section">
      <view class="section-header">
        <text class="title">为你推荐</text>
      </view>
      <view class="product-grid">
        <view
          class="product-item"
          v-for="item in products"
          :key="item.id"
          @click="goProductDetail(item.id)"
        >
          <image class="product-image" :src="item.image" mode="aspectFill" />
          <view class="info">
            <text class="name text-ellipsis-2">{{ item.name }}</text>
            <text class="subtitle text-ellipsis">{{ item.description }}</text>
            <view class="price-row">
              <text class="price">¥{{ item.price }}</text>
              <text class="original-price" v-if="item.originalPrice > item.price">
                ¥{{ item.originalPrice }}
              </text>
            </view>
            <view class="action">
              <text class="sales">已售{{ item.salesCount || 0 }}</text>
              <view class="add-cart" @click.stop="addToCart(item)">
                <uni-icons type="cart" size="20" color="#fff" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>
    <view class="no-more" v-if="noMore && products.length > 0">
      <text>没有更多了</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { homeApi } from '@/api'
import { useCartStore } from '@/store/cart'

const cartStore = useCartStore()

const location = ref('')
const banners = ref([])
const categories = ref([])
const groupActivities = ref([])
const products = ref([])
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const noMore = ref(false)

onMounted(() => {
  loadHomeData()
  loadProducts()
})

onPullDownRefresh(async () => {
  page.value = 1
  products.value = []
  noMore.value = false
  await Promise.all([loadHomeData(), loadProducts()])
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (!noMore.value && !loading.value) {
    loadProducts()
  }
})

// 加载首页数据
const loadHomeData = async () => {
  try {
    const res = await homeApi.getHomeData()
    if (res.code === 200) {
      banners.value = res.data.banners || []
      categories.value = res.data.categories || []
      groupActivities.value = res.data.groupActivities || []
    }
  } catch (e) {
    console.error('加载首页数据失败', e)
  }
}

// 加载商品列表
const loadProducts = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await homeApi.getRecommendProducts({
      page: page.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      const list = res.data.records || []
      products.value = [...products.value, ...list]
      page.value++
      noMore.value = list.length < pageSize.value
    }
  } catch (e) {
    console.error('加载商品失败', e)
  } finally {
    loading.value = false
  }
}

// 选择位置
const chooseLocation = () => {
  uni.chooseLocation({
    success: (res) => {
      location.value = res.name || res.address
      // 重新加载数据
      page.value = 1
      products.value = []
      noMore.value = false
      loadHomeData()
      loadProducts()
    }
  })
}

// 跳转搜索
const goSearch = () => {
  uni.navigateTo({ url: '/pages/search/index' })
}

// 轮播图点击
const onBannerTap = (item) => {
  switch (item.linkType) {
    case 1:
      uni.navigateTo({ url: `/pages/product/detail?id=${item.linkId}` })
      break
    case 2:
      uni.navigateTo({ url: `/pages/merchant/detail?id=${item.linkId}` })
      break
    case 3:
      uni.navigateTo({ url: `/pages/group/detail?id=${item.linkId}` })
      break
  }
}

// 跳转分类
const goCategory = (item) => {
  uni.switchTab({ url: '/pages/category/index' })
}

// 跳转拼团列表
const goGroupList = () => {
  uni.navigateTo({ url: '/pages/group/list' })
}

// 跳转拼团详情
const goGroupDetail = (id) => {
  uni.navigateTo({ url: `/pages/group/detail?id=${id}` })
}

// 跳转商品详情
const goProductDetail = (id) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

// 加入购物车
const addToCart = async (item) => {
  await cartStore.add(item)
}
</script>

<style lang="scss" scoped>
.container {
  padding-bottom: 20rpx;
  background-color: #f5f5f5;
}

/* 头部 */
.header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: $primary-color;
}

.location {
  display: flex;
  align-items: center;
  margin-right: 20rpx;

  .text {
    font-size: 28rpx;
    color: #fff;
    max-width: 150rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin: 0 8rpx;
  }
}

.search {
  flex: 1;
  display: flex;
  align-items: center;
  height: 64rpx;
  padding: 0 20rpx;
  background-color: #fff;
  border-radius: 32rpx;

  .placeholder {
    font-size: 28rpx;
    color: #999;
    margin-left: 12rpx;
  }
}

/* 轮播图 */
.banner {
  height: 320rpx;

  .banner-image {
    width: 100%;
    height: 100%;
  }
}

/* 分类导航 */
.category-nav {
  display: flex;
  flex-wrap: wrap;
  padding: 30rpx 20rpx;
  background-color: #fff;
}

.category-item {
  width: 20%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20rpx;

  .icon {
    width: 80rpx;
    height: 80rpx;
  }

  .name {
    font-size: 24rpx;
    color: #333;
    margin-top: 10rpx;
  }
}

/* 区块 */
.section {
  margin-top: 20rpx;
  background-color: #fff;
  padding: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  .title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .more {
    display: flex;
    align-items: center;
    font-size: 24rpx;
    color: #999;
  }
}

/* 拼团列表 */
.group-list {
  white-space: nowrap;
}

.group-item {
  display: inline-block;
  width: 240rpx;
  margin-right: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;
  box-shadow: $box-shadow;

  .product-image {
    width: 240rpx;
    height: 240rpx;
  }

  .info {
    padding: 16rpx;
  }

  .name {
    font-size: 26rpx;
    color: #333;
  }

  .price-row {
    margin-top: 10rpx;
  }

  .group-price {
    font-size: 32rpx;
    color: $danger-color;
    font-weight: bold;
  }

  .original-price {
    font-size: 22rpx;
    color: #999;
    text-decoration: line-through;
    margin-left: 10rpx;
  }

  .group-tag {
    display: inline-block;
    margin-top: 10rpx;
    padding: 6rpx 12rpx;
    background-color: #fff0f0;
    border-radius: 8rpx;

    text {
      font-size: 22rpx;
      color: $danger-color;
    }
  }
}

/* 商品网格 */
.product-grid {
  display: flex;
  flex-wrap: wrap;
  margin: -10rpx;
}

.product-item {
  width: calc(50% - 20rpx);
  margin: 10rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;
  box-shadow: $box-shadow;

  .product-image {
    width: 100%;
    height: 320rpx;
  }

  .info {
    padding: 16rpx;
  }

  .name {
    font-size: 28rpx;
    color: #333;
    line-height: 1.4;
  }

  .subtitle {
    font-size: 22rpx;
    color: #999;
    margin-top: 8rpx;
  }

  .price-row {
    margin-top: 10rpx;
  }

  .price {
    font-size: 36rpx;
    color: $danger-color;
    font-weight: bold;
  }

  .original-price {
    font-size: 24rpx;
    color: #999;
    text-decoration: line-through;
    margin-left: 10rpx;
  }

  .action {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10rpx;
  }

  .sales {
    font-size: 22rpx;
    color: #999;
  }

  .add-cart {
    width: 48rpx;
    height: 48rpx;
    background-color: $primary-color;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

/* 加载状态 */
.loading,
.no-more {
  text-align: center;
  padding: 30rpx;
  font-size: 26rpx;
  color: #999;
}
</style>
