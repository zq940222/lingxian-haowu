<template>
  <view class="container">
    <!-- 订单状态标签 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ active: currentStatus === item.value }"
        v-for="item in statusTabs"
        :key="item.value"
        @click="switchTab(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <!-- 订单列表 -->
    <scroll-view
      class="order-list"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="order-item" v-for="order in orders" :key="order.id">
        <view class="order-header" @click="goDetail(order.id)">
          <view class="shop-info">
            <image :src="order.shopLogo" mode="aspectFill" />
            <text class="shop-name">{{ order.shopName }}</text>
          </view>
          <text class="status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </text>
        </view>

        <view class="order-products" @click="goDetail(order.id)">
          <view class="product" v-for="(item, i) in getOrderItems(order).slice(0, 2)" :key="i">
            <image :src="item.productImage" mode="aspectFill" />
            <view class="info">
              <text class="name">{{ item.productName }}</text>
              <text class="spec">{{ item.skuName }}</text>
            </view>
            <view class="right">
              <text class="price">¥{{ item.price }}</text>
              <text class="quantity">x{{ item.quantity }}</text>
            </view>
          </view>
          <view class="more-products" v-if="getOrderItems(order).length > 2">
            共{{ getOrderItems(order).length }}件商品
          </view>
        </view>

        <view class="order-footer">
          <view class="total">
            <text>共{{ getTotalQuantity(order) }}件</text>
            <text class="amount">实付 <text class="price">¥{{ order.payAmount }}</text></text>
          </view>
          <view class="actions">
            <!-- 待付款 -->
            <template v-if="order.status === 1">
              <view class="btn default" @click="cancelOrder(order)">取消订单</view>
              <view class="btn primary" @click="payOrder(order)">立即支付</view>
            </template>
            <!-- 配送中 -->
            <template v-else-if="order.status === 3">
              <view class="btn default" @click="goDetail(order.id)">查看详情</view>
              <view class="btn primary" @click="confirmReceive(order)">确认收货</view>
            </template>
            <!-- 待评价 -->
            <template v-else-if="order.status === 4">
              <view class="btn default" @click="goDetail(order.id)">查看详情</view>
              <view class="btn primary" @click="goComment(order)">去评价</view>
            </template>
            <!-- 已完成 -->
            <template v-else-if="order.status === 5">
              <view class="btn default" @click="viewComment(order)">查看评价</view>
              <view class="btn primary" @click="reorder(order)">再来一单</view>
            </template>
            <!-- 其他状态（待发货、已取消等） -->
            <template v-else>
              <view class="btn default" @click="goDetail(order.id)">查看详情</view>
            </template>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && orders.length > 0">没有更多订单了</view>
      <view class="empty" v-if="!loading && orders.length === 0">
        <image src="/static/images/empty-order.png" mode="aspectFit" />
        <text>暂无订单</text>
        <view class="go-shop" @click="goHome">去逛逛</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { orderApi } from '@/api'

// 订单状态：1-待付款 2-待发货 3-配送中 4-待评价 5-已完成 6-已取消 7-已退款
const statusTabs = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 1 },
  { label: '待发货', value: 2 },
  { label: '配送中', value: 3 },
  { label: '待评价', value: 4 },
  { label: '已完成', value: 5 }
]

const currentStatus = ref(-1)
const orders = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)

onLoad((options) => {
  if (options.status) {
    currentStatus.value = parseInt(options.status)
  }
})

onShow(() => {
  resetAndLoad()
})

// 切换状态
const switchTab = (status) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  resetAndLoad()
}

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  orders.value = []
  noMore.value = false
  loadOrders()
}

// 加载订单
const loadOrders = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      page: page.value,
      pageSize: 10
    }
    if (currentStatus.value !== -1) {
      params.status = currentStatus.value
    }

    const res = await orderApi.getList(params)
    if (res.code === 200) {
      const list = res.data.records || []
      orders.value = page.value === 1 ? list : [...orders.value, ...list]
      page.value++
      noMore.value = list.length < 10
    }
  } catch (e) {
    console.error('加载订单失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    loadOrders()
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  resetAndLoad()
}

// 获取状态样式
const getStatusClass = (status) => {
  const map = {
    1: 'pending',    // 待付款
    2: 'wait',       // 待发货
    3: 'delivery',   // 配送中
    4: 'done',       // 待评价
    5: 'done',       // 已完成
    6: 'cancel',     // 已取消
    7: 'cancel'      // 已退款
  }
  return map[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    1: '待支付',
    2: '待发货',
    3: '配送中',
    4: '待评价',
    5: '已完成',
    6: '已取消',
    7: '已退款'
  }
  return map[status] || '未知'
}

// 获取订单商品（兼容 items 和 products 字段）
const getOrderItems = (order) => {
  return order.items || order.products || []
}

// 计算订单总数量
const getTotalQuantity = (order) => {
  if (order.totalQuantity) return order.totalQuantity
  const items = getOrderItems(order)
  return items.reduce((sum, item) => sum + (item.quantity || 0), 0)
}

// 跳转详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

// 跳转评价
const goComment = (order) => {
  uni.navigateTo({ url: `/pages/order/comment?id=${order.id}` })
}

// 查看评价
const viewComment = (order) => {
  uni.navigateTo({ url: `/pages/order/comment-view?orderId=${order.id}` })
}

// 取消订单
const cancelOrder = async (order) => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要取消订单吗？'
    })

    const res = await orderApi.cancel(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '订单已取消', icon: 'success' })
      resetAndLoad()
    }
  } catch (e) {
    // 取消
  }
}

// 支付订单
const payOrder = async (order) => {
  try {
    const res = await orderApi.pay(order.id)
    if (res.code === 200) {
      // 调用微信支付
      await uni.requestPayment(res.data)
      uni.showToast({ title: '支付成功', icon: 'success' })
      resetAndLoad()
    }
  } catch (e) {
    uni.showToast({ title: '支付失败', icon: 'none' })
  }
}

// 确认收货
const confirmReceive = async (order) => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确认已收到商品？'
    })

    const res = await orderApi.confirm(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '已确认收货', icon: 'success' })
      resetAndLoad()
    }
  } catch (e) {
    // 取消
  }
}

// 联系商家
const callShop = (order) => {
  uni.makePhoneCall({ phoneNumber: order.shopPhone })
}

// 再来一单
const reorder = async (order) => {
  try {
    const res = await orderApi.reorder(order.id)
    if (res.code === 200) {
      uni.switchTab({ url: '/pages/cart/index' })
    }
  } catch (e) {
    uni.showToast({ title: '添加失败', icon: 'none' })
  }
}

// 去首页
const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 标签页 */
.tabs {
  display: flex;
  background-color: #fff;
  padding: 0 10rpx;

  .tab-item {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    font-size: 28rpx;
    color: #666;
    position: relative;

    &.active {
      color: $primary-color;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background-color: $primary-color;
        border-radius: 2rpx;
      }
    }
  }
}

/* 订单列表 */
.order-list {
  flex: 1;
  padding: 20rpx;
}

.order-item {
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
  overflow: hidden;

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .shop-info {
      display: flex;
      align-items: center;

      image {
        width: 48rpx;
        height: 48rpx;
        border-radius: 8rpx;
        margin-right: 16rpx;
      }

      .shop-name {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
      }
    }

    .status {
      font-size: 26rpx;

      &.pending { color: #ff9800; }
      &.wait { color: #1890ff; }
      &.delivery { color: $primary-color; }
      &.done { color: $success-color; }
      &.cancel { color: #999; }
    }
  }

  .order-products {
    padding: 20rpx 24rpx;

    .product {
      display: flex;
      align-items: center;
      margin-bottom: 16rpx;

      &:last-child {
        margin-bottom: 0;
      }

      image {
        width: 120rpx;
        height: 120rpx;
        border-radius: $border-radius-sm;
        margin-right: 16rpx;
      }

      .info {
        flex: 1;

        .name {
          font-size: 28rpx;
          color: #333;
          display: block;
        }

        .spec {
          font-size: 24rpx;
          color: #999;
          margin-top: 8rpx;
          display: block;
        }
      }

      .right {
        text-align: right;

        .price {
          font-size: 28rpx;
          color: #333;
          display: block;
        }

        .quantity {
          font-size: 24rpx;
          color: #999;
          margin-top: 8rpx;
          display: block;
        }
      }
    }

    .more-products {
      text-align: center;
      font-size: 24rpx;
      color: #999;
      padding-top: 16rpx;
      border-top: 1rpx dashed #f0f0f0;
    }
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 24rpx;
    border-top: 1rpx solid #f0f0f0;

    .total {
      font-size: 26rpx;
      color: #666;

      .amount {
        margin-left: 16rpx;

        .price {
          color: $danger-color;
          font-weight: bold;
        }
      }
    }

    .actions {
      display: flex;

      .btn {
        padding: 14rpx 32rpx;
        border-radius: 32rpx;
        font-size: 26rpx;
        margin-left: 16rpx;

        &.default {
          background-color: #f5f5f5;
          color: #666;
        }

        &.primary {
          background-color: $primary-color;
          color: #fff;
        }
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

  .go-shop {
    margin-top: 30rpx;
    padding: 16rpx 60rpx;
    background-color: $primary-color;
    color: #fff;
    font-size: 28rpx;
    border-radius: 40rpx;
  }
}
</style>
