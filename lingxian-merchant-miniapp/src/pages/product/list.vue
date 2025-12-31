<template>
  <view class="container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input">
        <uni-icons type="search" size="18" color="#999" />
        <input type="text" v-model="keyword" placeholder="搜索商品" @confirm="onSearch" />
      </view>
      <view class="add-btn" @click="goAdd">
        <uni-icons type="plusempty" size="20" color="#fff" />
      </view>
    </view>

    <!-- 分类筛选 -->
    <scroll-view class="category-scroll" scroll-x>
      <view class="category-list">
        <view
          class="category-item"
          :class="{ active: currentCategory === -1 }"
          @click="selectCategory(-1)"
        >
          全部
        </view>
        <view
          class="category-item"
          :class="{ active: currentCategory === item.id }"
          v-for="item in categories"
          :key="item.id"
          @click="selectCategory(item.id)"
        >
          {{ item.name }}
        </view>
      </view>
    </scroll-view>

    <!-- 商品列表 -->
    <scroll-view
      class="product-list"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="product-item" v-for="item in products" :key="item.id">
        <image :src="item.image" mode="aspectFill" @click="goEdit(item.id)" />
        <view class="info" @click="goEdit(item.id)">
          <text class="name">{{ item.name }}</text>
          <view class="meta">
            <text class="price">¥{{ item.price }}</text>
            <text class="stock">库存: {{ item.stock }}</text>
          </view>
          <view class="sales">
            <text>销量: {{ item.sales || 0 }}</text>
          </view>
        </view>
        <view class="actions">
          <switch
            :checked="item.status === 1"
            @change="toggleStatus(item)"
            color="#1890ff"
          />
          <text class="status-text">{{ item.status === 1 ? '上架' : '下架' }}</text>
          <view class="btn-group">
            <view class="btn" @click="editStock(item)">
              <uni-icons type="compose" size="18" color="#666" />
            </view>
            <view class="btn" @click="deleteProduct(item)">
              <uni-icons type="trash" size="18" color="#ff4d4f" />
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && products.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && products.length === 0">
        <image src="/static/images/empty-product.png" mode="aspectFit" />
        <text>暂无商品</text>
        <view class="add-first" @click="goAdd">添加商品</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { productApi, categoryApi } from '@/api'

const keyword = ref('')
const currentCategory = ref(-1)
const categories = ref([])
const products = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)

onLoad(() => {
  loadCategories()
})

onShow(() => {
  resetAndLoad()
})

// 加载分类
const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  products.value = []
  noMore.value = false
  loadProducts()
}

// 选择分类
const selectCategory = (id) => {
  if (currentCategory.value === id) return
  currentCategory.value = id
  resetAndLoad()
}

// 搜索
const onSearch = () => {
  resetAndLoad()
}

// 加载商品
const loadProducts = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      page: page.value,
      pageSize: 10,
      keyword: keyword.value
    }
    if (currentCategory.value !== -1) {
      params.categoryId = currentCategory.value
    }

    const res = await productApi.getList(params)
    if (res.code === 200) {
      const list = res.data.records || []
      products.value = page.value === 1 ? list : [...products.value, ...list]
      page.value++
      noMore.value = list.length < 10
    }
  } catch (e) {
    console.error('加载商品失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    loadProducts()
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  resetAndLoad()
}

// 切换上下架状态
const toggleStatus = async (item) => {
  const newStatus = item.status === 1 ? 0 : 1
  try {
    const res = await productApi.updateStatus(item.id, newStatus)
    if (res.code === 200) {
      item.status = newStatus
      uni.showToast({
        title: newStatus === 1 ? '已上架' : '已下架',
        icon: 'success'
      })
    }
  } catch (e) {
    console.error('更新状态失败', e)
  }
}

// 编辑库存
const editStock = async (item) => {
  try {
    const result = await uni.showModal({
      title: '修改库存',
      editable: true,
      placeholderText: '请输入库存数量',
      content: String(item.stock)
    })

    if (result.confirm && result.content) {
      const stock = parseInt(result.content)
      if (isNaN(stock) || stock < 0) {
        uni.showToast({ title: '请输入有效数量', icon: 'none' })
        return
      }

      const res = await productApi.updateStock(item.id, stock)
      if (res.code === 200) {
        item.stock = stock
        uni.showToast({ title: '库存已更新', icon: 'success' })
      }
    }
  } catch (e) {
    // 取消
  }
}

// 删除商品
const deleteProduct = async (item) => {
  try {
    await uni.showModal({
      title: '提示',
      content: `确定删除商品"${item.name}"吗？`
    })

    const res = await productApi.remove(item.id)
    if (res.code === 200) {
      uni.showToast({ title: '删除成功', icon: 'success' })
      resetAndLoad()
    }
  } catch (e) {
    // 取消
  }
}

// 跳转添加
const goAdd = () => {
  uni.navigateTo({ url: '/pages/product/edit' })
}

// 跳转编辑
const goEdit = (id) => {
  uni.navigateTo({ url: `/pages/product/edit?id=${id}` })
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
      margin-left: 16rpx;
      font-size: 28rpx;
    }
  }

  .add-btn {
    width: 72rpx;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: $primary-color;
    border-radius: 50%;
    margin-left: 20rpx;
  }
}

/* 分类滚动 */
.category-scroll {
  background-color: #fff;
  border-top: 1rpx solid #f0f0f0;
  white-space: nowrap;
}

.category-list {
  display: inline-flex;
  padding: 20rpx;

  .category-item {
    padding: 12rpx 32rpx;
    margin-right: 20rpx;
    font-size: 26rpx;
    color: #666;
    background-color: #f5f5f5;
    border-radius: 30rpx;

    &.active {
      color: #fff;
      background-color: $primary-color;
    }
  }
}

/* 商品列表 */
.product-list {
  flex: 1;
  padding: 20rpx;
}

.product-item {
  display: flex;
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
  overflow: hidden;

  image {
    width: 180rpx;
    height: 180rpx;
  }

  .info {
    flex: 1;
    padding: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .name {
      font-size: 28rpx;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .meta {
      display: flex;
      align-items: center;

      .price {
        font-size: 32rpx;
        color: $danger-color;
        font-weight: bold;
      }

      .stock {
        font-size: 24rpx;
        color: #999;
        margin-left: 20rpx;
      }
    }

    .sales {
      font-size: 24rpx;
      color: #999;
    }
  }

  .actions {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx;
    background-color: #fafafa;

    .status-text {
      font-size: 22rpx;
      color: #999;
      margin-top: -10rpx;
    }

    .btn-group {
      display: flex;

      .btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        border-radius: 50%;
        margin-left: 10rpx;
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

  .add-first {
    margin-top: 30rpx;
    padding: 20rpx 60rpx;
    background-color: $primary-color;
    color: #fff;
    font-size: 28rpx;
    border-radius: 40rpx;
  }
}
</style>
