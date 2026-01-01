<template>
  <view class="container">
    <!-- 左侧分类菜单 -->
    <scroll-view class="category-menu" scroll-y>
      <view
        class="menu-item"
        :class="{ active: currentIndex === index }"
        v-for="(item, index) in categories"
        :key="item.id"
        @click="switchCategory(index)"
      >
        <text>{{ item.name }}</text>
      </view>
    </scroll-view>

    <!-- 右侧商品列表 -->
    <scroll-view
      class="product-list"
      scroll-y
      @scrolltolower="loadMore"
      :scroll-top="scrollTop"
    >
      <!-- 子分类 -->
      <view class="sub-categories" v-if="currentCategory?.children?.length">
        <view
          class="sub-item"
          :class="{ active: currentSubIndex === index }"
          v-for="(item, index) in currentCategory.children"
          :key="item.id"
          @click="switchSubCategory(index)"
        >
          <image v-if="item.icon" :src="item.icon" mode="aspectFit" />
          <text>{{ item.name }}</text>
        </view>
      </view>

      <!-- 商品列表 -->
      <view class="products">
        <view
          class="product-item"
          v-for="item in products"
          :key="item.id"
          @click="goDetail(item.id)"
        >
          <image class="product-image" :src="item.image" mode="aspectFill" />
          <view class="product-info">
            <text class="name text-ellipsis-2">{{ item.name }}</text>
            <text class="spec">{{ item.unit }}</text>
            <view class="bottom">
              <view class="price-row">
                <text class="price">¥{{ item.price }}</text>
                <text class="original" v-if="item.originalPrice > item.price">
                  ¥{{ item.originalPrice }}
                </text>
              </view>
              <view class="add-btn" @click.stop="addToCart(item)">
                <uni-icons type="plusempty" size="20" color="#fff" />
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && products.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && products.length === 0">
        <text>暂无商品</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { categoryApi } from '@/api'
import { useCartStore } from '@/store/cart'

const cartStore = useCartStore()

const categories = ref([])
const currentIndex = ref(0)
const currentSubIndex = ref(-1)
const products = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const scrollTop = ref(0)

const currentCategory = computed(() => categories.value[currentIndex.value])

onMounted(() => {
  loadCategories()
})

// 加载分类
const loadCategories = async () => {
  try {
    const res = await categoryApi.getCategories()
    if (res.code === 200) {
      categories.value = res.data || []
      if (categories.value.length > 0) {
        loadProducts()
      }
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 切换一级分类
const switchCategory = (index) => {
  if (currentIndex.value === index) return
  currentIndex.value = index
  currentSubIndex.value = -1
  resetProducts()
  loadProducts()
  scrollTop.value = 0
}

// 切换二级分类
const switchSubCategory = (index) => {
  if (currentSubIndex.value === index) {
    currentSubIndex.value = -1
  } else {
    currentSubIndex.value = index
  }
  resetProducts()
  loadProducts()
}

// 重置商品列表
const resetProducts = () => {
  products.value = []
  page.value = 1
  noMore.value = false
}

// 加载商品
const loadProducts = async () => {
  if (loading.value || noMore.value) return

  const categoryId = currentSubIndex.value >= 0
    ? currentCategory.value.children[currentSubIndex.value].id
    : currentCategory.value?.id

  if (!categoryId) return

  loading.value = true
  try {
    const res = await categoryApi.getCategoryProducts(categoryId, {
      page: page.value,
      pageSize: 10
    })
    if (res.code === 200) {
      const list = res.data.records || []
      products.value = [...products.value, ...list]
      page.value++
      noMore.value = list.length < 10
    }
  } catch (e) {
    console.error('加载商品失败', e)
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

// 加入购物车
const addToCart = async (item) => {
  await cartStore.add(item)
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 左侧分类菜单 */
.category-menu {
  width: 180rpx;
  height: 100%;
  background-color: #f5f5f5;

  .menu-item {
    padding: 30rpx 20rpx;
    text-align: center;
    font-size: 26rpx;
    color: #666;
    position: relative;

    &.active {
      background-color: #fff;
      color: $primary-color;
      font-weight: bold;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 6rpx;
        height: 40rpx;
        background-color: $primary-color;
        border-radius: 0 3rpx 3rpx 0;
      }
    }
  }
}

/* 右侧商品列表 */
.product-list {
  flex: 1;
  height: 100%;
  background-color: #fff;
}

/* 子分类 */
.sub-categories {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .sub-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 25%;
    padding: 16rpx 0;

    image {
      width: 80rpx;
      height: 80rpx;
      margin-bottom: 8rpx;
    }

    text {
      font-size: 24rpx;
      color: #666;
    }

    &.active text {
      color: $primary-color;
    }
  }
}

/* 商品列表 */
.products {
  padding: 20rpx;
}

.product-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .product-image {
    width: 180rpx;
    height: 180rpx;
    border-radius: $border-radius-base;
    margin-right: 20rpx;
  }

  .product-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .name {
      font-size: 28rpx;
      color: #333;
      line-height: 1.4;
    }

    .spec {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }

    .bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .price-row {
      display: flex;
      align-items: baseline;
    }

    .price {
      font-size: 36rpx;
      color: $danger-color;
      font-weight: bold;
    }

    .original {
      font-size: 24rpx;
      color: #999;
      text-decoration: line-through;
      margin-left: 8rpx;
    }

    .add-btn {
      width: 48rpx;
      height: 48rpx;
      background-color: $primary-color;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
}

.loading,
.no-more,
.empty {
  text-align: center;
  padding: 40rpx;
  font-size: 26rpx;
  color: #999;
}
</style>
