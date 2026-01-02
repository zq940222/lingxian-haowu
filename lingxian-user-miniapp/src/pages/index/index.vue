<template>
  <view class="container">
    <!-- 头部：定位和搜索 -->
    <view class="header">
      <view class="location" @click="chooseLocation">
        <view class="location-dot"></view>
        <text class="text">{{ location || '选择地址' }}</text>
        <text class="arrow">∨</text>
      </view>
      <view class="search" @click="goSearch">
        <text class="search-icon">🔍</text>
        <text class="placeholder">搜索商品</text>
      </view>
    </view>

    <!-- 轮播图 -->
    <view class="banner-wrapper">
      <swiper class="banner" indicator-dots indicator-color="rgba(255,255,255,0.4)" indicator-active-color="#fff" autoplay circular :interval="3000">
        <swiper-item v-for="item in banners" :key="item.id" @click="onBannerTap(item)">
          <image class="banner-image" :src="item.imageUrl" mode="aspectFill" />
        </swiper-item>
      </swiper>
    </view>

    <!-- 分类入口 -->
    <view class="category-nav">
      <view
        class="category-item"
        v-for="item in categories"
        :key="item.id"
        @click="goCategory(item)"
      >
        <view class="icon-wrapper">
          <image class="icon" :src="item.icon" mode="aspectFit" />
        </view>
        <text class="name">{{ item.name }}</text>
      </view>
    </view>

    <!-- 限时抢购 -->
    <view class="section flash-sale-section" v-if="flashSaleProducts.length > 0">
      <view class="section-header">
        <view class="title-with-countdown">
          <text class="title">限时抢购</text>
          <view class="countdown">
            <text class="countdown-label">距结束</text>
            <view class="countdown-item">{{ countdown.hours }}</view>
            <text class="countdown-sep">:</text>
            <view class="countdown-item">{{ countdown.minutes }}</view>
            <text class="countdown-sep">:</text>
            <view class="countdown-item">{{ countdown.seconds }}</view>
          </view>
        </view>
        <view class="more" @click="goFlashSale">
          <text>更多</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <scroll-view class="flash-sale-list" scroll-x show-scrollbar="false">
        <view
          class="flash-sale-item"
          v-for="item in flashSaleProducts"
          :key="item.id"
          @click="goProductDetail(item.id)"
        >
          <image class="product-image" :src="item.image" mode="aspectFill" />
          <view class="info">
            <text class="flash-price">¥{{ item.salePrice }}</text>
            <text class="original-price">¥{{ item.originalPrice }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 拼团专区 -->
    <view class="section" v-if="groupActivities.length > 0">
      <view class="section-header">
        <text class="title">限时拼团</text>
        <view class="more" @click="goGroupList">
          <text>更多</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <scroll-view class="group-list" scroll-x show-scrollbar="false">
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
    <view class="section recommend-section">
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
          <view class="image-wrapper">
            <image class="product-image" :src="item.image" mode="aspectFill" />
          </view>
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
                <text class="add-icon">+</text>
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
      <text>— 没有更多了 —</text>
    </view>

    <!-- 小区选择弹窗 -->
    <uni-popup ref="communityPopup" type="bottom">
      <view class="community-popup">
        <view class="popup-header">
          <text class="popup-title">选择配送小区</text>
          <view class="popup-close" @click="closeCommunityPopup">
            <uni-icons type="closeempty" size="24" color="#666" />
          </view>
        </view>
        <view class="popup-tip">
          <uni-icons type="info" size="14" color="#1890ff" />
          <text>以下小区今日可配送</text>
        </view>
        <scroll-view scroll-y class="popup-body">
          <view v-if="availableCommunities.length === 0" class="empty-community">
            <uni-icons type="map-pin" size="48" color="#ccc" />
            <text>暂无可配送小区</text>
          </view>
          <view
            v-for="item in availableCommunities"
            :key="item.id"
            class="community-item"
            :class="{ active: selectedCommunity && selectedCommunity.id === item.id }"
            @click="selectCommunity(item)"
          >
            <view class="item-left">
              <uni-icons
                v-if="selectedCommunity && selectedCommunity.id === item.id"
                type="checkbox-filled"
                size="22"
                color="#22c55e"
              />
              <uni-icons v-else type="circle" size="22" color="#ddd" />
            </view>
            <view class="item-center">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-address">{{ item.fullAddress }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { homeApi, communityApi } from '@/api'
import { useCartStore } from '@/store/cart'

const cartStore = useCartStore()

const location = ref('')
const communityPopup = ref(null)
const availableCommunities = ref([])
const selectedCommunity = ref(null)
const banners = ref([])
const categories = ref([])
const groupActivities = ref([])
const flashSaleProducts = ref([])
const products = ref([])
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const noMore = ref(false)
const userLocation = ref(null) // 用户当前位置

// 限时抢购倒计时
const countdown = reactive({
  hours: '00',
  minutes: '00',
  seconds: '00'
})
let countdownTimer = null
let flashSaleEndTime = null

// 格式化数字为两位
const padZero = (num) => String(num).padStart(2, '0')

// 更新倒计时
const updateCountdown = () => {
  if (!flashSaleEndTime) return

  const now = Date.now()
  const diff = flashSaleEndTime - now

  if (diff <= 0) {
    countdown.hours = '00'
    countdown.minutes = '00'
    countdown.seconds = '00'
    clearInterval(countdownTimer)
    return
  }

  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)

  countdown.hours = padZero(hours)
  countdown.minutes = padZero(minutes)
  countdown.seconds = padZero(seconds)
}

// 启动倒计时
const startCountdown = () => {
  // 设置结束时间为今天23:59:59（模拟数据）
  const now = new Date()
  flashSaleEndTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59).getTime()

  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

onMounted(() => {
  // 先加载可配送小区列表
  loadAvailableCommunities()
  loadHomeData()
  loadProducts()
  startCountdown()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
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
    const params = {}
    if (selectedCommunity.value) {
      params.communityId = selectedCommunity.value.id
    }
    const res = await homeApi.getHomeData(params)
    if (res.code === 200) {
      banners.value = res.data.banners || []
      categories.value = res.data.categories || []
      groupActivities.value = res.data.groupActivities || []
      flashSaleProducts.value = res.data.flashSaleProducts || []
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
    const params = {
      page: page.value,
      pageSize: pageSize.value
    }
    // 如果选择了小区，传递小区ID
    if (selectedCommunity.value) {
      params.communityId = selectedCommunity.value.id
    }
    const res = await homeApi.getRecommendProducts(params)
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

// 计算两点之间的距离（使用Haversine公式）
const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const R = 6371 // 地球半径，单位：公里
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLng = (lng2 - lng1) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

// 获取用户位置
const getUserLocation = () => {
  return new Promise((resolve) => {
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        userLocation.value = {
          latitude: res.latitude,
          longitude: res.longitude
        }
        resolve(userLocation.value)
      },
      fail: (err) => {
        console.log('获取位置失败', err)
        resolve(null)
      }
    })
  })
}

// 根据定位找到最近的小区
const findNearestCommunity = (communities, userLoc) => {
  if (!userLoc || !communities || communities.length === 0) return null

  let nearest = null
  let minDistance = Infinity

  for (const community of communities) {
    if (community.latitude && community.longitude) {
      const distance = calculateDistance(
        userLoc.latitude, userLoc.longitude,
        community.latitude, community.longitude
      )
      if (distance < minDistance) {
        minDistance = distance
        nearest = community
      }
    }
  }

  return nearest
}

// 加载可配送小区
const loadAvailableCommunities = async () => {
  try {
    const res = await communityApi.getAvailable()
    if (res.code === 200) {
      availableCommunities.value = res.data || []

      // 恢复之前选择的小区
      const savedCommunity = uni.getStorageSync('selectedCommunity')
      if (savedCommunity) {
        const found = availableCommunities.value.find(c => c.id === savedCommunity.id)
        if (found) {
          selectedCommunity.value = found
          location.value = found.name
          return
        } else {
          // 之前选择的小区今天不可配送，清除选择
          uni.removeStorageSync('selectedCommunity')
        }
      }

      // 没有已选小区，尝试根据定位自动分配
      if (availableCommunities.value.length > 0) {
        const userLoc = await getUserLocation()
        if (userLoc) {
          const nearest = findNearestCommunity(availableCommunities.value, userLoc)
          if (nearest) {
            selectedCommunity.value = nearest
            location.value = nearest.name
            uni.setStorageSync('selectedCommunity', nearest)
            uni.showToast({
              title: `已自动选择：${nearest.name}`,
              icon: 'none',
              duration: 2000
            })
            return
          }
        }

        // 无法定位或没有找到最近小区，弹出选择弹窗
        setTimeout(() => {
          if (!selectedCommunity.value && communityPopup.value) {
            communityPopup.value.open()
          }
        }, 500)
      }
    }
  } catch (e) {
    console.error('加载可配送小区失败', e)
  }
}

// 选择位置 - 打开小区选择弹窗
const chooseLocation = () => {
  communityPopup.value.open()
}

// 关闭小区选择弹窗
const closeCommunityPopup = () => {
  communityPopup.value.close()
}

// 选择小区
const selectCommunity = (item) => {
  selectedCommunity.value = item
  location.value = item.name
  // 保存选择到本地
  uni.setStorageSync('selectedCommunity', item)
  closeCommunityPopup()
  // 重新加载数据
  page.value = 1
  products.value = []
  noMore.value = false
  loadHomeData()
  loadProducts()
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

// 跳转限时抢购
const goFlashSale = () => {
  uni.navigateTo({ url: '/pages/flash-sale/index' })
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
  uni.showToast({ title: '已加入购物车', icon: 'success' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  padding-bottom: 20rpx;
  background-color: #f5f5f5;
}

/* 头部 */
.header {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #fff;
}

.location {
  display: flex;
  align-items: center;
  margin-right: 20rpx;

  .location-dot {
    width: 16rpx;
    height: 16rpx;
    background-color: #22c55e;
    border-radius: 50%;
    margin-right: 8rpx;
  }

  .text {
    font-size: 28rpx;
    color: #1f2937;
    font-weight: 500;
    max-width: 150rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .arrow {
    font-size: 24rpx;
    color: #9ca3af;
    margin-left: 4rpx;
  }
}

.search {
  flex: 1;
  display: flex;
  align-items: center;
  height: 72rpx;
  padding: 0 24rpx;
  background-color: #f3f4f6;
  border-radius: 36rpx;

  .search-icon {
    font-size: 28rpx;
    margin-right: 12rpx;
  }

  .placeholder {
    font-size: 28rpx;
    color: #9ca3af;
  }
}

/* 轮播图 */
.banner-wrapper {
  padding: 20rpx 30rpx;
}

.banner {
  height: 320rpx;
  border-radius: 20rpx;
  overflow: hidden;

  .banner-image {
    width: 100%;
    height: 100%;
    border-radius: 20rpx;
  }
}

/* 分类导航 */
.category-nav {
  display: flex;
  flex-wrap: wrap;
  padding: 30rpx 20rpx;
  background-color: #fff;
  border-radius: 20rpx;
  margin: 0 30rpx 20rpx;
}

.category-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20rpx;

  .icon-wrapper {
    width: 96rpx;
    height: 96rpx;
    background-color: #f9fafb;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 12rpx;
  }

  .icon {
    width: 56rpx;
    height: 56rpx;
  }

  .name {
    font-size: 24rpx;
    color: #4b5563;
  }
}

/* 区块 */
.section {
  margin: 0 30rpx 20rpx;
  background-color: #fff;
  padding: 30rpx;
  border-radius: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;

  .title {
    font-size: 34rpx;
    font-weight: bold;
    color: #1f2937;
  }

  .title-with-countdown {
    display: flex;
    align-items: center;

    .title {
      margin-right: 20rpx;
    }

    .countdown {
      display: flex;
      align-items: center;

      .countdown-label {
        font-size: 22rpx;
        color: #6b7280;
        margin-right: 10rpx;
      }

      .countdown-item {
        min-width: 40rpx;
        height: 40rpx;
        line-height: 40rpx;
        text-align: center;
        background-color: #1f2937;
        color: #fff;
        font-size: 24rpx;
        font-weight: bold;
        border-radius: 8rpx;
      }

      .countdown-sep {
        font-size: 24rpx;
        font-weight: bold;
        color: #1f2937;
        margin: 0 6rpx;
      }
    }
  }

  .more {
    display: flex;
    align-items: center;
    font-size: 26rpx;
    color: #22c55e;

    .arrow {
      font-size: 28rpx;
      margin-left: 4rpx;
    }
  }
}

/* 限时抢购 */
.flash-sale-section {
  background-color: #fff;
}

.flash-sale-list {
  white-space: nowrap;
  margin: 0 -10rpx;
}

.flash-sale-item {
  display: inline-block;
  width: 200rpx;
  margin: 0 10rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;

  .product-image {
    width: 200rpx;
    height: 200rpx;
    background-color: #f9fafb;
    border-radius: 12rpx;
  }

  .info {
    padding: 12rpx 0;
    text-align: center;
  }

  .flash-price {
    font-size: 32rpx;
    color: #ef4444;
    font-weight: bold;
    display: block;
  }

  .original-price {
    font-size: 22rpx;
    color: #9ca3af;
    text-decoration: line-through;
    display: block;
    margin-top: 4rpx;
  }
}

/* 拼团列表 */
.group-list {
  white-space: nowrap;
  margin: 0 -10rpx;
}

.group-item {
  display: inline-block;
  width: 260rpx;
  margin: 0 10rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);

  .product-image {
    width: 260rpx;
    height: 260rpx;
    background-color: #f9fafb;
  }

  .info {
    padding: 16rpx;
  }

  .name {
    font-size: 26rpx;
    color: #1f2937;
  }

  .price-row {
    margin-top: 12rpx;
  }

  .group-price {
    font-size: 34rpx;
    color: #22c55e;
    font-weight: bold;
  }

  .original-price {
    font-size: 22rpx;
    color: #9ca3af;
    text-decoration: line-through;
    margin-left: 10rpx;
  }

  .group-tag {
    display: inline-block;
    margin-top: 12rpx;
    padding: 6rpx 16rpx;
    background-color: #f0fdf4;
    border-radius: 20rpx;

    text {
      font-size: 22rpx;
      color: #22c55e;
    }
  }
}

/* 推荐商品 */
.recommend-section {
  background-color: transparent;
  padding: 0 30rpx;
  margin: 0;
}

.product-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.product-item {
  width: calc(50% - 10rpx);
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;

  .image-wrapper {
    width: 100%;
    aspect-ratio: 1;
    background-color: #f9fafb;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20rpx;
  }

  .product-image {
    width: 100%;
    height: 100%;
  }

  .info {
    padding: 20rpx;
  }

  .name {
    font-size: 28rpx;
    color: #1f2937;
    font-weight: 500;
    line-height: 1.4;
  }

  .subtitle {
    font-size: 22rpx;
    color: #9ca3af;
    margin-top: 8rpx;
  }

  .price-row {
    margin-top: 12rpx;
  }

  .price {
    font-size: 36rpx;
    color: #22c55e;
    font-weight: bold;
  }

  .original-price {
    font-size: 24rpx;
    color: #9ca3af;
    text-decoration: line-through;
    margin-left: 10rpx;
  }

  .action {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12rpx;
  }

  .sales {
    font-size: 22rpx;
    color: #9ca3af;
  }

  .add-cart {
    width: 52rpx;
    height: 52rpx;
    background-color: #22c55e;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;

    .add-icon {
      font-size: 36rpx;
      color: #fff;
      line-height: 1;
    }
  }
}

/* 文本省略 */
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* 加载状态 */
.loading,
.no-more {
  text-align: center;
  padding: 40rpx;
  font-size: 26rpx;
  color: #9ca3af;
}

/* 小区选择弹窗 */
.community-popup {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 70vh;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.popup-close {
  padding: 10rpx;
}

.popup-tip {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #e6f7ff;
}

.popup-tip text {
  margin-left: 10rpx;
  font-size: 24rpx;
  color: #1890ff;
}

.popup-body {
  max-height: 50vh;
  padding: 0 30rpx;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.empty-community {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.empty-community text {
  margin-top: 20rpx;
  font-size: 28rpx;
  color: #999;
}

.community-list {
  padding-bottom: 20rpx;
}

.community-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.community-item:last-child {
  border-bottom: none;
}

.community-item.active {
  background-color: #f0fdf4;
  margin: 0 -30rpx;
  padding: 24rpx 30rpx;
  border-radius: 12rpx;
}

.item-left {
  flex-shrink: 0;
  width: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-center {
  flex: 1;
  min-width: 0;
  margin-left: 16rpx;
  margin-right: 16rpx;
  display: flex;
  flex-direction: column;
}

.item-name {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-address {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

</style>
