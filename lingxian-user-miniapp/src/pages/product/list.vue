<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input" @click="goSearch">
        <uni-icons type="search" size="18" color="#999" />
        <text class="placeholder">搜索商品</text>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view
        class="filter-item"
        :class="{ active: sortType === 'default' }"
        @click="changeSort('default')"
      >
        <text>综合</text>
      </view>
      <view
        class="filter-item"
        :class="{ active: sortType === 'sales' }"
        @click="changeSort('sales')"
      >
        <text>销量</text>
      </view>
      <view
        class="filter-item"
        :class="{ active: sortType === 'price' }"
        @click="changeSort('price')"
      >
        <text>价格</text>
        <view class="sort-icon">
          <uni-icons
            :type="priceOrder === 'asc' ? 'top' : 'bottom'"
            size="12"
            :color="sortType === 'price' ? '#07c160' : '#999'"
          />
        </view>
      </view>
    </view>

    <!-- 商品列表 -->
    <scroll-view
      scroll-y
      class="product-scroll"
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="isRefreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="product-list">
        <view
          class="product-item"
          v-for="item in productList"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <image :src="item.mainImage" mode="aspectFill" class="product-image" />
          <view class="product-info">
            <text class="name">{{ item.name }}</text>
            <text class="subtitle">{{ item.subtitle }}</text>
            <view class="price-row">
              <text class="price">¥{{ item.price }}</text>
              <text class="original-price" v-if="item.originalPrice > item.price">
                ¥{{ item.originalPrice }}
              </text>
            </view>
            <view class="sales">已售{{ item.sales || 0 }}</view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-status">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty" v-if="!loading && productList.length === 0">
        <uni-icons type="shop" size="60" color="#ccc" />
        <text>暂无商品</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { productApi } from '@/api'

const categoryId = ref('')
const keyword = ref('')
const sortType = ref('default')
const priceOrder = ref('asc')
const productList = ref([])
const loading = ref(false)
const isRefreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 10

onLoad((options) => {
  if (options.categoryId) {
    categoryId.value = options.categoryId
  }
  if (options.keyword) {
    keyword.value = options.keyword
  }
  loadList()
})

// 加载商品列表
const loadList = async (refresh = false) => {
  if (loading.value) return

  if (refresh) {
    page.value = 1
    noMore.value = false
  }

  loading.value = true

  try {
    const params = {
      page: page.value,
      pageSize,
      categoryId: categoryId.value,
      keyword: keyword.value,
      sortType: sortType.value,
      priceOrder: sortType.value === 'price' ? priceOrder.value : ''
    }

    const res = await productApi.getList(params)
    if (res.code === 200) {
      const list = res.data.list || []
      if (refresh) {
        productList.value = list
      } else {
        productList.value = [...productList.value, ...list]
      }

      if (list.length < pageSize) {
        noMore.value = true
      }
    }
  } catch (e) {
    console.error('加载商品列表失败', e)
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  isRefreshing.value = true
  loadList(true)
}

// 加载更多
const loadMore = () => {
  if (noMore.value || loading.value) return
  page.value++
  loadList()
}

// 切换排序
const changeSort = (type) => {
  if (type === 'price' && sortType.value === 'price') {
    priceOrder.value = priceOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortType.value = type
    if (type === 'price') {
      priceOrder.value = 'asc'
    }
  }
  loadList(true)
}

// 去搜索页
const goSearch = () => {
  uni.navigateTo({
    url: '/pages/search/index'
  })
}

// 去商品详情
const goDetail = (id) => {
  uni.navigateTo({
    url: `/pages/product/detail?id=${id}`
  })
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 搜索栏 */
.search-bar {
  padding: 20rpx 30rpx;
  background-color: #fff;

  .search-input {
    display: flex;
    align-items: center;
    height: 72rpx;
    padding: 0 24rpx;
    background-color: #f5f5f5;
    border-radius: 36rpx;

    .placeholder {
      font-size: 28rpx;
      color: #999;
      margin-left: 12rpx;
    }
  }
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  height: 88rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .filter-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;

    text {
      font-size: 28rpx;
      color: #666;
    }

    &.active text {
      color: #07c160;
      font-weight: bold;
    }

    .sort-icon {
      margin-left: 4rpx;
    }
  }
}

/* 商品滚动区域 */
.product-scroll {
  flex: 1;
  overflow: hidden;
}

/* 商品列表 */
.product-list {
  padding: 20rpx;

  .product-item {
    display: flex;
    padding: 20rpx;
    background-color: #fff;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    .product-image {
      width: 200rpx;
      height: 200rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
    }

    .product-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      margin-left: 20rpx;
      overflow: hidden;

      .name {
        font-size: 28rpx;
        color: #333;
        font-weight: bold;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .subtitle {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .price-row {
        display: flex;
        align-items: baseline;
        margin-top: auto;

        .price {
          font-size: 36rpx;
          color: #ff4d4f;
          font-weight: bold;
        }

        .original-price {
          font-size: 24rpx;
          color: #999;
          text-decoration: line-through;
          margin-left: 12rpx;
        }
      }

      .sales {
        font-size: 22rpx;
        color: #999;
        margin-top: 8rpx;
      }
    }
  }
}

/* 加载状态 */
.loading-status {
  text-align: center;
  padding: 30rpx;

  text {
    font-size: 26rpx;
    color: #999;
  }
}

/* 空状态 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }
}
</style>
