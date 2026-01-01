<template>
  <view class="container">
    <!-- 状态标签页 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ active: currentStatus === item.value }"
        v-for="item in statusTabs"
        :key="item.value"
        @click="switchTab(item.value)"
      >
        <text>{{ item.label }}</text>
        <view class="badge" v-if="item.count > 0">{{ item.count }}</view>
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
          <text class="order-no">{{ order.orderNo }}</text>
          <text class="status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </text>
        </view>

        <view class="order-content" @click="goDetail(order.id)">
          <view class="products">
            <view class="product" v-for="(p, i) in order.products" :key="i">
              <image :src="p.image" mode="aspectFill" />
              <view class="info">
                <text class="name text-ellipsis">{{ p.name }}</text>
                <text class="spec">{{ p.spec }}</text>
              </view>
              <view class="right">
                <text class="price">¥{{ p.price }}</text>
                <text class="quantity">x{{ p.quantity }}</text>
              </view>
            </view>
          </view>
          <view class="total">
            <text>共{{ order.totalQuantity }}件商品</text>
            <text class="amount">实付：¥{{ order.payAmount }}</text>
          </view>
        </view>

        <view class="order-address" @click="goDetail(order.id)">
          <uni-icons type="location" size="16" color="#1890ff" />
          <view class="address-detail">
            <view class="receiver">
              <text class="name">{{ order.receiverName }}</text>
              <text class="phone">{{ order.receiverPhone }}</text>
            </view>
            <text class="address">{{ order.receiverAddress }}</text>
          </view>
        </view>

        <view class="order-footer">
          <text class="time">{{ order.createTime }}</text>
          <view class="actions">
            <!-- 待发货 - 可接单/拒单，或直接开始配送 -->
            <template v-if="order.status === 2">
              <view class="btn default" @click="rejectOrder(order)">拒单</view>
              <view class="btn primary" @click="startDelivery(order)">开始配送</view>
            </template>
            <!-- 配送中 -->
            <template v-else-if="order.status === 3">
              <view class="btn default" @click="openNavigation(order)">导航</view>
              <view class="btn primary" @click="completeDelivery(order)">完成配送</view>
            </template>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && orders.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && orders.length === 0">
        <image src="/static/images/empty-order.png" mode="aspectFit" />
        <text>暂无订单</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMerchantStore, VerifyStatus } from '@/store/merchant'
import { orderApi } from '@/api'

const merchantStore = useMerchantStore()

// 简化流程：隐藏待配送状态，待发货(2)直接变为配送中(3)
const statusTabs = ref([
  { label: '全部', value: -1, count: 0 },
  { label: '待发货', value: 2, count: 0 },
  { label: '配送中', value: 3, count: 0 },
  { label: '已完成', value: 4, count: 0 }
])

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

onShow(async () => {
  // 检查登录状态
  if (!merchantStore.checkLoginStatus()) {
    return
  }

  // 从服务器获取最新的审核状态
  try {
    const statusData = await merchantStore.fetchApplyStatus()
    if (statusData.verifyStatus !== VerifyStatus.APPROVED) {
      uni.reLaunch({ url: '/pages/apply/status' })
      return
    }
  } catch (e) {
    if (merchantStore.verifyStatus !== VerifyStatus.APPROVED) {
      uni.reLaunch({ url: '/pages/apply/status' })
      return
    }
  }

  resetAndLoad()
})

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  orders.value = []
  noMore.value = false
  loadOrders()
}

// 切换标签
const switchTab = (status) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  resetAndLoad()
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
  const classMap = {
    1: 'wait',       // 待付款
    2: 'pending',    // 待发货
    3: 'delivering', // 配送中
    4: 'completed',  // 待评价
    5: 'completed',  // 已完成
    6: 'canceled',   // 已取消
    7: 'canceled'    // 已退款
  }
  return classMap[status] || ''
}

// 获取状态文本（简化流程）
const getStatusText = (status) => {
  const textMap = {
    1: '待付款',
    2: '待发货',
    3: '配送中',
    4: '待评价',
    5: '已完成',
    6: '已取消',
    7: '已退款'
  }
  return textMap[status] || '未知'
}

// 跳转详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

// 接单
const acceptOrder = async (order) => {
  try {
    const res = await orderApi.accept(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '接单成功', icon: 'success' })
      resetAndLoad()
    }
  } catch (e) {
    console.error('接单失败', e)
  }
}

// 拒单
const rejectOrder = async (order) => {
  try {
    const result = await uni.showModal({
      title: '拒单原因',
      editable: true,
      placeholderText: '请输入拒单原因'
    })

    if (result.confirm && result.content) {
      const res = await orderApi.reject(order.id, result.content)
      if (res.code === 200) {
        uni.showToast({ title: '已拒单', icon: 'success' })
        resetAndLoad()
      }
    }
  } catch (e) {
    // 取消
  }
}

// 开始配送
const startDelivery = async (order) => {
  try {
    const res = await orderApi.startDelivery(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '开始配送', icon: 'success' })
      resetAndLoad()
    }
  } catch (e) {
    console.error('开始配送失败', e)
  }
}

// 完成配送
const completeDelivery = async (order) => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确认已完成配送？'
    })

    const res = await orderApi.completeDelivery(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '配送完成', icon: 'success' })
      resetAndLoad()
    }
  } catch (e) {
    // 取消或失败
  }
}

// 打开导航
const openNavigation = (order) => {
  uni.openLocation({
    latitude: order.latitude,
    longitude: order.longitude,
    name: order.receiverAddress,
    address: order.receiverAddress
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

/* 标签页 */
.tabs {
  display: flex;
  background-color: #fff;
  padding: 0 10rpx;

  .tab-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 88rpx;
    position: relative;
    font-size: 28rpx;
    color: #666;

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

    .badge {
      min-width: 32rpx;
      height: 32rpx;
      line-height: 32rpx;
      text-align: center;
      font-size: 20rpx;
      color: #fff;
      background-color: $danger-color;
      border-radius: 16rpx;
      padding: 0 8rpx;
      margin-left: 8rpx;
    }
  }
}

/* 订单列表 */
.order-list {
  flex: 1;
  padding: 20rpx;
  box-sizing: border-box;
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

    .order-no {
      font-size: 26rpx;
      color: #666;
    }

    .status {
      font-size: 26rpx;
      font-weight: bold;

      &.pending { color: #ff9800; }
      &.delivering { color: $primary-color; }
      &.completed { color: $success-color; }
      &.canceled { color: #999; }
    }
  }

  .order-content {
    padding: 24rpx;

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
          color: $danger-color;
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

    .total {
      display: flex;
      justify-content: space-between;
      padding-top: 16rpx;
      border-top: 1rpx solid #f0f0f0;
      font-size: 26rpx;
      color: #666;

      .amount {
        color: $danger-color;
        font-weight: bold;
      }
    }
  }

  .order-address {
    display: flex;
    align-items: flex-start;
    padding: 20rpx 24rpx;
    background-color: #f8f8f8;
    font-size: 24rpx;
    color: #666;

    .address-detail {
      flex: 1;
      margin-left: 12rpx;
      overflow: hidden;

      .receiver {
        display: flex;
        align-items: center;
        margin-bottom: 8rpx;

        .name {
          font-size: 28rpx;
          font-weight: bold;
          color: #333;
          margin-right: 16rpx;
        }

        .phone {
          font-size: 26rpx;
          color: #666;
        }
      }

      .address {
        display: block;
        font-size: 26rpx;
        color: #666;
        line-height: 1.4;
        word-break: break-all;
      }
    }
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 24rpx;

    .time {
      font-size: 24rpx;
      color: #999;
    }

    .actions {
      display: flex;

      .btn {
        padding: 12rpx 28rpx;
        border-radius: 28rpx;
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
}
</style>
