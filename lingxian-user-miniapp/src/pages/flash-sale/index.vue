<template>
  <view class="container">
    <!-- 倒计时头部 -->
    <view class="countdown-header">
      <view class="countdown-info">
        <text class="label">限时抢购</text>
        <view class="countdown">
          <text class="countdown-label">距结束</text>
          <view class="countdown-item">{{ countdown.hours }}</view>
          <text class="countdown-sep">:</text>
          <view class="countdown-item">{{ countdown.minutes }}</view>
          <text class="countdown-sep">:</text>
          <view class="countdown-item">{{ countdown.seconds }}</view>
        </view>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="product-list">
      <view
        class="product-item"
        v-for="item in products"
        :key="item.id"
        @click="goDetail(item.id)"
      >
        <image class="product-image" :src="item.image" mode="aspectFill" />
        <view class="product-info">
          <text class="name text-ellipsis-2">{{ item.name }}</text>
          <text class="desc text-ellipsis">{{ item.description }}</text>
          <view class="price-row">
            <text class="sale-price">¥{{ item.salePrice }}</text>
            <text class="original-price">¥{{ item.originalPrice }}</text>
          </view>
          <view class="action-row">
            <view class="progress-wrapper">
              <view class="progress-bar">
                <view class="progress-fill" :style="{ width: item.soldPercent + '%' }"></view>
              </view>
              <text class="progress-text">已抢{{ item.soldPercent }}%</text>
            </view>
            <view class="buy-btn" @click.stop="handleBuy(item)">
              <text>抢购</text>
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
    <view class="empty" v-if="!loading && products.length === 0">
      <uni-icons type="fire" size="64" color="#ccc" />
      <text>暂无抢购活动</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { homeApi } from '@/api'

const products = ref([])
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const noMore = ref(false)

// 倒计时
const countdown = reactive({
  hours: '00',
  minutes: '00',
  seconds: '00'
})
let countdownTimer = null
let flashSaleEndTime = null

const padZero = (num) => String(num).padStart(2, '0')

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

const startCountdown = () => {
  const now = new Date()
  flashSaleEndTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59).getTime()
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

onMounted(() => {
  loadProducts()
  startCountdown()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

onReachBottom(() => {
  if (!noMore.value && !loading.value) {
    loadProducts()
  }
})

const loadProducts = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const res = await homeApi.getFlashSaleProducts({
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
    console.error('加载限时抢购商品失败', e)
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

const handleBuy = (item) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${item.id}` })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.countdown-header {
  background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
  padding: 30rpx;
}

.countdown-info {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .label {
    font-size: 36rpx;
    font-weight: bold;
    color: #fff;
  }

  .countdown {
    display: flex;
    align-items: center;

    .countdown-label {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-right: 12rpx;
    }

    .countdown-item {
      min-width: 48rpx;
      height: 48rpx;
      line-height: 48rpx;
      text-align: center;
      background-color: rgba(0, 0, 0, 0.3);
      color: #fff;
      font-size: 28rpx;
      font-weight: bold;
      border-radius: 8rpx;
    }

    .countdown-sep {
      font-size: 28rpx;
      font-weight: bold;
      color: #fff;
      margin: 0 8rpx;
    }
  }
}

.product-list {
  padding: 20rpx;
}

.product-item {
  display: flex;
  background-color: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;

  .product-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: 12rpx;
    background-color: #f9fafb;
    flex-shrink: 0;
  }

  .product-info {
    flex: 1;
    margin-left: 24rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .name {
    font-size: 28rpx;
    color: #1f2937;
    font-weight: 500;
    line-height: 1.4;
  }

  .desc {
    font-size: 22rpx;
    color: #9ca3af;
    margin-top: 8rpx;
  }

  .price-row {
    margin-top: 12rpx;
  }

  .sale-price {
    font-size: 36rpx;
    color: #ef4444;
    font-weight: bold;
  }

  .original-price {
    font-size: 24rpx;
    color: #9ca3af;
    text-decoration: line-through;
    margin-left: 12rpx;
  }

  .action-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;
  }

  .progress-wrapper {
    flex: 1;
    margin-right: 20rpx;

    .progress-bar {
      height: 12rpx;
      background-color: #fee2e2;
      border-radius: 6rpx;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #ef4444, #f97316);
        border-radius: 6rpx;
      }
    }

    .progress-text {
      font-size: 20rpx;
      color: #ef4444;
      margin-top: 6rpx;
      display: block;
    }
  }

  .buy-btn {
    padding: 12rpx 32rpx;
    background: linear-gradient(135deg, #ef4444 0%, #f97316 100%);
    border-radius: 30rpx;

    text {
      font-size: 26rpx;
      color: #fff;
      font-weight: 500;
    }
  }
}

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

.loading,
.no-more,
.empty {
  text-align: center;
  padding: 40rpx;
  font-size: 26rpx;
  color: #9ca3af;
}
</style>
