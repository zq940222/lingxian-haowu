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
    <view class="filter-bar">
      <view class="filter-item" @click="showCategoryPicker = true">
        <text>{{ selectedCategoryText }}</text>
        <uni-icons type="bottom" size="14" color="#666" />
      </view>
      <view class="filter-item" :class="{ active: statusFilter !== '' }" @click="toggleStatusFilter">
        <text>{{ statusFilterText }}</text>
        <uni-icons type="bottom" size="14" color="#666" />
      </view>
    </view>

    <!-- 分类选择弹窗 -->
    <uni-popup ref="categoryPopup" type="bottom" :is-mask-click="true" @maskClick="showCategoryPicker = false">
      <view class="category-popup" v-if="showCategoryPicker">
        <view class="popup-header">
          <text class="cancel" @click="showCategoryPicker = false">取消</text>
          <text class="title">选择分类</text>
          <text class="confirm" @click="confirmCategory">确定</text>
        </view>
        <view class="category-content">
          <!-- 一级分类 -->
          <scroll-view class="category-column" scroll-y>
            <view
              class="category-option"
              :class="{ active: tempCategoryIndexes[0] === -1 }"
              @click="selectParentCategory(-1)"
            >
              全部分类
            </view>
            <view
              class="category-option"
              :class="{ active: tempCategoryIndexes[0] === index }"
              v-for="(item, index) in categoryTree"
              :key="item.id"
              @click="selectParentCategory(index)"
            >
              {{ item.name }}
            </view>
          </scroll-view>
          <!-- 二级分类 -->
          <scroll-view class="category-column" scroll-y v-if="tempCategoryIndexes[0] >= 0">
            <view
              class="category-option"
              :class="{ active: tempCategoryIndexes[1] === -1 }"
              @click="selectChildCategory(-1)"
            >
              全部{{ categoryTree[tempCategoryIndexes[0]]?.name }}
            </view>
            <view
              class="category-option"
              :class="{ active: tempCategoryIndexes[1] === index }"
              v-for="(item, index) in currentChildren"
              :key="item.id"
              @click="selectChildCategory(index)"
            >
              {{ item.name }}
            </view>
          </scroll-view>
        </view>
      </view>
    </uni-popup>

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
        <image :src="item.mainImage" mode="aspectFill" @click="goEdit(item.id)" />
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
import { ref, computed, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { productApi, categoryApi } from '@/api'

const keyword = ref('')
const products = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)

// 分类相关
const categoryTree = ref([])
const showCategoryPicker = ref(false)
const categoryPopup = ref(null)
// 选中的分类索引 [一级索引, 二级索引]，-1表示全部
const categoryIndexes = ref([-1, -1])
// 临时选择索引（弹窗中使用）
const tempCategoryIndexes = ref([-1, -1])

// 当前一级分类下的子分类
const currentChildren = computed(() => {
  const parentIdx = tempCategoryIndexes.value[0]
  if (parentIdx < 0 || !categoryTree.value[parentIdx]) return []
  return categoryTree.value[parentIdx].children || []
})

// 选中的分类文本
const selectedCategoryText = computed(() => {
  const [parentIdx, childIdx] = categoryIndexes.value
  if (parentIdx < 0) return '全部分类'
  const parent = categoryTree.value[parentIdx]
  if (!parent) return '全部分类'
  if (childIdx < 0) return parent.name
  const child = parent.children?.[childIdx]
  return child ? `${parent.name}/${child.name}` : parent.name
})

// 获取当前选中的分类ID
const currentCategoryId = computed(() => {
  const [parentIdx, childIdx] = categoryIndexes.value
  if (parentIdx < 0) return null
  const parent = categoryTree.value[parentIdx]
  if (!parent) return null
  if (childIdx >= 0 && parent.children?.[childIdx]) {
    return parent.children[childIdx].id
  }
  return parent.id
})

// 状态筛选
const statusFilter = ref('')
const statusFilterText = computed(() => {
  if (statusFilter.value === '') return '全部状态'
  return statusFilter.value === '1' ? '上架中' : '已下架'
})

// 切换状态筛选
const toggleStatusFilter = () => {
  if (statusFilter.value === '') {
    statusFilter.value = '1'
  } else if (statusFilter.value === '1') {
    statusFilter.value = '0'
  } else {
    statusFilter.value = ''
  }
  resetAndLoad()
}

// 监听弹窗显示
watch(showCategoryPicker, (val) => {
  if (val) {
    tempCategoryIndexes.value = [...categoryIndexes.value]
    categoryPopup.value?.open()
  } else {
    categoryPopup.value?.close()
  }
})

onLoad(() => {
  loadCategories()
})

onShow(() => {
  resetAndLoad()
})

// 加载分类（树形结构）
const loadCategories = async () => {
  try {
    const res = await categoryApi.getTree()
    if (res.code === 200) {
      categoryTree.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 选择一级分类
const selectParentCategory = (index) => {
  tempCategoryIndexes.value = [index, -1]
}

// 选择二级分类
const selectChildCategory = (index) => {
  tempCategoryIndexes.value[1] = index
}

// 确认分类选择
const confirmCategory = () => {
  categoryIndexes.value = [...tempCategoryIndexes.value]
  showCategoryPicker.value = false
  resetAndLoad()
}

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  products.value = []
  noMore.value = false
  loadProducts()
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
    if (currentCategoryId.value) {
      params.categoryId = currentCategoryId.value
    }
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
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

/* 筛选栏 */
.filter-bar {
  display: flex;
  background-color: #fff;
  border-top: 1rpx solid #f0f0f0;
  padding: 16rpx 20rpx;

  .filter-item {
    display: flex;
    align-items: center;
    padding: 12rpx 24rpx;
    margin-right: 20rpx;
    font-size: 26rpx;
    color: #666;
    background-color: #f5f5f5;
    border-radius: 30rpx;

    text {
      margin-right: 8rpx;
    }

    &.active {
      color: $primary-color;
      background-color: rgba(24, 144, 255, 0.1);
    }
  }
}

/* 分类选择弹窗 */
.category-popup {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 28rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .cancel {
      font-size: 28rpx;
      color: #999;
    }

    .confirm {
      font-size: 28rpx;
      color: $primary-color;
    }
  }

  .category-content {
    display: flex;
    height: 600rpx;

    .category-column {
      flex: 1;
      height: 100%;

      &:first-child {
        background-color: #f5f5f5;
      }

      .category-option {
        padding: 28rpx 30rpx;
        font-size: 28rpx;
        color: #333;

        &.active {
          color: $primary-color;
          background-color: #fff;
          font-weight: bold;
        }
      }
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
