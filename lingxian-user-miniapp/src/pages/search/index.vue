<template>
  <view class="container">
    <!-- 搜索框 -->
    <view class="search-bar">
      <view class="search-input">
        <uni-icons type="search" size="18" color="#999" />
        <input
          type="text"
          v-model="keyword"
          placeholder="搜索商品"
          focus
          @confirm="doSearch"
        />
        <uni-icons
          v-if="keyword"
          type="closeempty"
          size="18"
          color="#999"
          @click="clearKeyword"
        />
      </view>
      <text class="cancel-btn" @click="goBack">取消</text>
    </view>

    <!-- 搜索历史 -->
    <view class="history-section" v-if="!keyword && searchHistory.length > 0">
      <view class="section-header">
        <text>搜索历史</text>
        <uni-icons type="trash" size="18" color="#999" @click="clearHistory" />
      </view>
      <view class="tag-list">
        <view
          class="tag"
          v-for="(item, index) in searchHistory"
          :key="index"
          @click="searchByHistory(item)"
        >
          {{ item }}
        </view>
      </view>
    </view>

    <!-- 热门搜索 -->
    <view class="hot-section" v-if="!keyword && hotKeywords.length > 0">
      <view class="section-header">
        <text>热门搜索</text>
      </view>
      <view class="tag-list">
        <view
          class="tag hot"
          v-for="(item, index) in hotKeywords"
          :key="index"
          @click="searchByHistory(item)"
        >
          <text class="rank" v-if="index < 3">{{ index + 1 }}</text>
          {{ item }}
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <scroll-view
      v-if="hasSearched"
      class="result-list"
      scroll-y
      @scrolltolower="loadMore"
    >
      <view class="product-item" v-for="item in products" :key="item.id" @click="goDetail(item.id)">
        <image :src="item.image" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ item.name }}</text>
          <text class="shop">{{ item.shopName }}</text>
          <view class="bottom">
            <text class="price">¥{{ item.price }}</text>
            <text class="sales">已售{{ item.sales || 0 }}</text>
          </view>
        </view>
      </view>

      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && products.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && products.length === 0">
        <image src="/static/images/empty-search.png" mode="aspectFit" />
        <text>未找到相关商品</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { homeApi, productApi } from '@/api'

const keyword = ref('')
const searchHistory = ref([])
const hotKeywords = ref([])
const products = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const hasSearched = ref(false)

onMounted(() => {
  loadHistory()
  loadHotKeywords()
})

// 加载搜索历史
const loadHistory = () => {
  const history = uni.getStorageSync('search_history') || []
  searchHistory.value = history
}

// 保存搜索历史
const saveHistory = (kw) => {
  let history = uni.getStorageSync('search_history') || []
  history = history.filter(item => item !== kw)
  history.unshift(kw)
  if (history.length > 10) {
    history = history.slice(0, 10)
  }
  uni.setStorageSync('search_history', history)
  searchHistory.value = history
}

// 清除历史
const clearHistory = async () => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要清除搜索历史吗？'
    })
    uni.removeStorageSync('search_history')
    searchHistory.value = []
  } catch (e) {
    // 取消
  }
}

// 加载热门搜索
const loadHotKeywords = async () => {
  try {
    const res = await homeApi.getHomeData()
    if (res.code === 200 && res.data.hotKeywords) {
      hotKeywords.value = res.data.hotKeywords
    }
  } catch (e) {
    // 使用默认热门词
    hotKeywords.value = ['新鲜蔬菜', '时令水果', '肉蛋禽', '海鲜水产', '米面粮油']
  }
}

// 清除关键词
const clearKeyword = () => {
  keyword.value = ''
  hasSearched.value = false
  products.value = []
}

// 搜索
const doSearch = () => {
  if (!keyword.value.trim()) return
  saveHistory(keyword.value)
  hasSearched.value = true
  page.value = 1
  products.value = []
  noMore.value = false
  loadProducts()
}

// 通过历史搜索
const searchByHistory = (kw) => {
  keyword.value = kw
  doSearch()
}

// 加载商品
const loadProducts = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const res = await productApi.getList({
      keyword: keyword.value,
      page: page.value,
      pageSize: 10
    })

    if (res.code === 200) {
      const list = res.data.records || []
      products.value = page.value === 1 ? list : [...products.value, ...list]
      page.value++
      noMore.value = list.length < 10
    }
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    loadProducts()
  }
}

// 跳转详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background-color: #fff;

  .search-input {
    flex: 1;
    display: flex;
    align-items: center;
    height: 72rpx;
    padding: 0 24rpx;
    background-color: #f5f5f5;
    border-radius: 36rpx;

    input {
      flex: 1;
      height: 100%;
      margin: 0 16rpx;
      font-size: 28rpx;
    }
  }

  .cancel-btn {
    font-size: 28rpx;
    color: #666;
    padding-left: 20rpx;
  }
}

/* 历史/热门区域 */
.history-section,
.hot-section {
  padding: 30rpx;
  background-color: #fff;
  margin-bottom: 20rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    text {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .tag-list {
    display: flex;
    flex-wrap: wrap;

    .tag {
      padding: 12rpx 24rpx;
      margin-right: 20rpx;
      margin-bottom: 20rpx;
      background-color: #f5f5f5;
      border-radius: 30rpx;
      font-size: 26rpx;
      color: #666;

      &.hot {
        display: flex;
        align-items: center;

        .rank {
          display: inline-block;
          width: 32rpx;
          height: 32rpx;
          line-height: 32rpx;
          text-align: center;
          font-size: 20rpx;
          color: #fff;
          background-color: $primary-color;
          border-radius: 4rpx;
          margin-right: 10rpx;
        }
      }
    }
  }
}

/* 结果列表 */
.result-list {
  height: calc(100vh - 112rpx);
  padding: 20rpx;
}

.product-item {
  display: flex;
  padding: 20rpx;
  margin-bottom: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  image {
    width: 180rpx;
    height: 180rpx;
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

    .shop {
      font-size: 24rpx;
      color: #999;
    }

    .bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price {
        font-size: 32rpx;
        color: $danger-color;
        font-weight: bold;
      }

      .sales {
        font-size: 24rpx;
        color: #999;
      }
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

  image {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 20rpx;
  }

  text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
