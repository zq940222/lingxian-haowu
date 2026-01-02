<template>
  <view class="container">
    <!-- 头部Banner -->
    <view class="header-banner">
      <text class="title">拼团专区</text>
      <text class="desc">好物拼着买，价格更实惠</text>
    </view>

    <!-- 拼团列表 -->
    <scroll-view
      class="group-list"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="group-item" v-for="item in activities" :key="item.id" @click="goDetail(item.id)">
        <view class="product-info">
          <image :src="item.productImage" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.productName }}</text>
            <view class="price-row">
              <text class="group-price">¥{{ item.groupPrice }}</text>
              <text class="original-price">¥{{ item.originalPrice }}</text>
            </view>
            <view class="progress">
              <view class="progress-bar">
                <view class="progress-inner" :style="{ width: getProgress(item) + '%' }"></view>
              </view>
              <text>已拼{{ item.soldCount || 0 }}件</text>
            </view>
          </view>
        </view>
        <view class="group-info">
          <view class="people">
            <text class="num">{{ item.groupSize }}</text>
            <text>人团</text>
          </view>
          <view class="btn">去拼团</view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && activities.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && activities.length === 0">
        <view class="empty-icon">
          <uni-icons type="personadd" size="80" color="#ccc" />
        </view>
        <text>暂无拼团活动</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { groupApi } from '@/api'

const activities = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)

onShow(() => {
  resetAndLoad()
})

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  activities.value = []
  noMore.value = false
  loadActivities()
}

// 加载活动
const loadActivities = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const res = await groupApi.getActivities({
      page: page.value,
      pageSize: 10
    })

    if (res.code === 200) {
      const list = res.data.records || []
      activities.value = page.value === 1 ? list : [...activities.value, ...list]
      page.value++
      noMore.value = list.length < 10
    }
  } catch (e) {
    console.error('加载拼团活动失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    loadActivities()
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  resetAndLoad()
}

// 计算进度
const getProgress = (item) => {
  if (!item.totalCount || item.totalCount === 0) return 0
  return Math.min((item.soldCount / item.totalCount) * 100, 100)
}

// 跳转详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/group/detail?id=${id}` })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 头部Banner */
.header-banner {
  padding: 60rpx 40rpx;
  background: linear-gradient(135deg, #ff6b6b, #ffa06b);
  text-align: center;

  .title {
    font-size: 48rpx;
    font-weight: bold;
    color: #fff;
    display: block;
  }

  .desc {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 16rpx;
    display: block;
  }
}

/* 拼团列表 */
.group-list {
  height: calc(100vh - 200rpx);
  padding: 20rpx;
}

.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  margin-bottom: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  .product-info {
    display: flex;
    flex: 1;

    image {
      width: 160rpx;
      height: 160rpx;
      border-radius: $border-radius-sm;
      margin-right: 20rpx;
    }

    .info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .name {
        font-size: 28rpx;
        color: #333;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .price-row {
        display: flex;
        align-items: baseline;

        .group-price {
          font-size: 36rpx;
          font-weight: bold;
          color: $danger-color;
        }

        .original-price {
          font-size: 24rpx;
          color: #999;
          text-decoration: line-through;
          margin-left: 16rpx;
        }
      }

      .progress {
        display: flex;
        align-items: center;

        .progress-bar {
          flex: 1;
          height: 12rpx;
          background-color: #f5f5f5;
          border-radius: 6rpx;
          overflow: hidden;
          margin-right: 16rpx;

          .progress-inner {
            height: 100%;
            background: linear-gradient(90deg, #ff6b6b, #ffa06b);
            border-radius: 6rpx;
          }
        }

        text {
          font-size: 22rpx;
          color: #999;
          white-space: nowrap;
        }
      }
    }
  }

  .group-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-left: 20rpx;

    .people {
      text-align: center;
      margin-bottom: 16rpx;

      .num {
        font-size: 36rpx;
        font-weight: bold;
        color: $danger-color;
      }

      text {
        font-size: 22rpx;
        color: #666;
      }
    }

    .btn {
      padding: 16rpx 32rpx;
      background: linear-gradient(135deg, #ff6b6b, #ffa06b);
      color: #fff;
      font-size: 26rpx;
      border-radius: 30rpx;
    }
  }
}

.loading, .no-more {
  text-align: center;
  padding: 30rpx;
  font-size: 26rpx;
  color: #999;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  .empty-icon {
    margin-bottom: 20rpx;
  }

  text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
