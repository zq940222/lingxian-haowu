<template>
  <view class="container">
    <!-- 状态标签页 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ active: currentTab === 'pending' }"
        @click="switchTab('pending')"
      >
        <text>待配送</text>
        <view class="badge" v-if="pendingCount > 0">{{ pendingCount }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'delivering' }"
        @click="switchTab('delivering')"
      >
        <text>配送中</text>
        <view class="badge" v-if="deliveringCount > 0">{{ deliveringCount }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentTab === 'completed' }"
        @click="switchTab('completed')"
      >
        <text>已完成</text>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view class="batch-bar" v-if="currentTab === 'pending' && orders.length > 0">
      <view class="select-all" @click="toggleSelectAll">
        <uni-icons
          :type="isAllSelected ? 'checkbox-filled' : 'circle'"
          :color="isAllSelected ? '#1890ff' : '#ccc'"
          size="20"
        />
        <text>全选</text>
      </view>
      <view class="batch-btn" :class="{ disabled: selectedIds.length === 0 }" @click="batchDelivery">
        <text>批量配送 ({{ selectedIds.length }})</text>
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
        <view class="order-main">
          <!-- 头部：订单号、时间、复选框 -->
          <view class="order-header">
            <view class="header-left">
              <!-- 选择框 -->
              <view class="checkbox" v-if="currentTab === 'pending'" @click.stop="toggleSelect(order.id)">
                <uni-icons
                  :type="selectedIds.includes(order.id) ? 'checkbox-filled' : 'circle'"
                  :color="selectedIds.includes(order.id) ? '#1890ff' : '#ccc'"
                  size="20"
                />
              </view>
              <text class="order-no">{{ order.orderNo }}</text>
            </view>
            <text class="time">{{ order.createTime }}</text>
          </view>

          <view class="order-body" @click="goDetail(order.id)">
            <view class="address-info">
              <uni-icons type="location" size="16" color="#1890ff" />
              <view class="address-detail">
                <view class="receiver">
                  <text class="name">{{ order.receiverName }}</text>
                  <text class="phone">{{ order.receiverPhone }}</text>
                </view>
                <text class="address">{{ order.receiverAddress }}</text>
              </view>
            </view>

            <view class="products-summary">
              <text>共{{ order.totalQuantity }}件商品</text>
              <text class="amount">¥{{ order.payAmount }}</text>
            </view>

            <view class="delivery-time" v-if="order.deliveryTime">
              <uni-icons type="calendar" size="14" color="#fa8c16" />
              <text>预约配送：{{ order.deliveryTime }}</text>
            </view>
          </view>

          <!-- 底部操作按钮 -->
          <view class="order-actions">
            <!-- 待配送 -->
            <template v-if="currentTab === 'pending'">
              <view class="action-btn primary" @click.stop="startDelivery(order)">
                <uni-icons type="car" size="18" color="#fff" />
                <text>开始配送</text>
              </view>
            </template>
            <!-- 配送中 -->
            <template v-else-if="currentTab === 'delivering'">
              <view class="action-btn default" @click.stop="openNavigation(order)">
                <uni-icons type="navigate" size="18" color="#666" />
                <text>导航</text>
              </view>
              <view class="action-btn default" @click.stop="callCustomer(order)">
                <uni-icons type="phone" size="18" color="#666" />
                <text>电话</text>
              </view>
              <view class="action-btn primary" @click.stop="completeDelivery(order)">
                <uni-icons type="checkbox" size="18" color="#fff" />
                <text>完成配送</text>
              </view>
            </template>
            <!-- 已完成 -->
            <template v-else-if="currentTab === 'completed'">
              <view class="status-tag completed">
                <uni-icons type="checkbox-filled" size="16" color="#52c41a" />
                <text>已完成</text>
              </view>
            </template>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && orders.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && orders.length === 0">
        <image src="/static/images/empty-delivery.png" mode="aspectFit" />
        <text>暂无配送订单</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useMerchantStore, VerifyStatus } from '@/store/merchant'
import { deliveryApi, orderApi } from '@/api'

const merchantStore = useMerchantStore()

const currentTab = ref('pending')
const orders = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)

const pendingCount = ref(0)
const deliveringCount = ref(0)

const selectedIds = ref([])

const isAllSelected = computed(() => {
  return orders.value.length > 0 && selectedIds.value.length === orders.value.length
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
  loadCounts()
})

// 加载数量
const loadCounts = async () => {
  try {
    const [pendingRes, deliveringRes] = await Promise.all([
      deliveryApi.getPending({ page: 1, pageSize: 1 }),
      deliveryApi.getDelivering({ page: 1, pageSize: 1 })
    ])
    if (pendingRes.code === 200) {
      pendingCount.value = pendingRes.data.total || 0
    }
    if (deliveringRes.code === 200) {
      deliveringCount.value = deliveringRes.data.total || 0
    }
  } catch (e) {
    console.error('加载数量失败', e)
  }
}

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  orders.value = []
  noMore.value = false
  selectedIds.value = []
  loadOrders()
}

// 切换标签
const switchTab = (tab) => {
  if (currentTab.value === tab) return
  currentTab.value = tab
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

    let res
    if (currentTab.value === 'pending') {
      res = await deliveryApi.getPending(params)
    } else if (currentTab.value === 'delivering') {
      res = await deliveryApi.getDelivering(params)
    } else {
      res = await deliveryApi.getCompleted(params)
    }

    if (res.code === 200) {
      const list = res.data.records || []
      orders.value = page.value === 1 ? list : [...orders.value, ...list]
      page.value++
      noMore.value = list.length < 10
    } else {
      uni.showToast({ title: res.message || '加载失败', icon: 'none' })
    }
  } catch (e) {
    console.error('加载配送列表失败', e)
    uni.showToast({ title: '加载失败，请重试', icon: 'none' })
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
  loadCounts()
}

// 切换选择
const toggleSelect = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = orders.value.map(o => o.id)
  }
}

// 批量配送
const batchDelivery = async () => {
  if (selectedIds.value.length === 0) return

  try {
    const modalRes = await uni.showModal({
      title: '提示',
      content: `确定开始配送选中的${selectedIds.value.length}个订单？`
    })

    if (!modalRes.confirm) return

    uni.showLoading({ title: '处理中...' })
    const res = await deliveryApi.batchDelivery(selectedIds.value)
    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({ title: '批量配送成功', icon: 'success' })
      selectedIds.value = []
      resetAndLoad()
      loadCounts()
    } else {
      uni.showToast({ title: res.message || '操作失败', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '操作失败，请重试', icon: 'none' })
  }
}

// 跳转详情
const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

// 开始配送
const startDelivery = async (order) => {
  try {
    const res = await orderApi.startDelivery(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '开始配送', icon: 'success' })
      // 更新本地状态
      orders.value = orders.value.filter(o => o.id !== order.id)
      pendingCount.value = Math.max(0, pendingCount.value - 1)
      deliveringCount.value += 1
    } else {
      uni.showToast({ title: res.message || '操作失败', icon: 'none' })
    }
  } catch (e) {
    console.error('开始配送失败', e)
    uni.showToast({ title: '操作失败，请重试', icon: 'none' })
  }
}

// 完成配送
const completeDelivery = async (order) => {
  try {
    const modalRes = await uni.showModal({
      title: '提示',
      content: '确认已完成配送？'
    })

    if (!modalRes.confirm) return

    const res = await orderApi.completeDelivery(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '配送完成', icon: 'success' })
      // 更新本地状态
      orders.value = orders.value.filter(o => o.id !== order.id)
      deliveringCount.value = Math.max(0, deliveringCount.value - 1)
    } else {
      uni.showToast({ title: res.message || '操作失败', icon: 'none' })
    }
  } catch (e) {
    console.error('完成配送失败', e)
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

// 联系顾客
const callCustomer = (order) => {
  uni.makePhoneCall({
    phoneNumber: order.receiverPhone
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
  padding: 0 20rpx;

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

/* 批量操作栏 */
.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 30rpx;
  background-color: #fff;
  border-top: 1rpx solid #f0f0f0;

  .select-all {
    display: flex;
    align-items: center;

    text {
      font-size: 26rpx;
      color: #666;
      margin-left: 10rpx;
    }
  }

  .batch-btn {
    padding: 16rpx 40rpx;
    background-color: $primary-color;
    border-radius: 32rpx;

    text {
      font-size: 26rpx;
      color: #fff;
    }

    &.disabled {
      background-color: #ccc;
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

  .order-main {
    padding: 24rpx;

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;
      padding-bottom: 16rpx;
      border-bottom: 1rpx solid #f0f0f0;

      .header-left {
        display: flex;
        align-items: center;

        .checkbox {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 48rpx;
          height: 48rpx;
          margin-right: 12rpx;
        }

        .order-no {
          font-size: 24rpx;
          color: #999;
        }
      }

      .time {
        font-size: 24rpx;
        color: #999;
      }
    }

    .order-body {
      .address-info {
        display: flex;
        align-items: flex-start;
        margin-bottom: 16rpx;

        .address-detail {
          flex: 1;
          margin-left: 10rpx;
          overflow: hidden;

          .receiver {
            display: flex;
            align-items: center;

            .name {
              font-size: 30rpx;
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
            font-size: 26rpx;
            color: #666;
            margin-top: 8rpx;
            display: block;
            line-height: 1.4;
            word-break: break-all;
          }
        }
      }

      .products-summary {
        display: flex;
        justify-content: space-between;
        font-size: 26rpx;
        color: #666;
        padding: 12rpx 0;
        border-top: 1rpx solid #f0f0f0;

        .amount {
          color: $danger-color;
          font-weight: bold;
        }
      }

      .delivery-time {
        display: flex;
        align-items: center;
        margin-top: 12rpx;
        padding: 10rpx 16rpx;
        background-color: #fff7e6;
        border-radius: 8rpx;

        text {
          font-size: 24rpx;
          color: #fa8c16;
          margin-left: 8rpx;
        }
      }
    }

    .order-actions {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      margin-top: 20rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;
      gap: 16rpx;

      .action-btn {
        display: flex;
        align-items: center;
        padding: 14rpx 28rpx;
        border-radius: 32rpx;

        text {
          font-size: 26rpx;
          margin-left: 8rpx;
        }

        &.default {
          background-color: #f5f5f5;
          border: 1rpx solid #e0e0e0;

          text {
            color: #666;
          }
        }

        &.primary {
          background-color: $primary-color;

          text {
            color: #fff;
          }
        }
      }

      .status-tag {
        display: flex;
        align-items: center;
        padding: 10rpx 20rpx;
        border-radius: 8rpx;

        &.completed {
          background-color: #f6ffed;

          text {
            font-size: 26rpx;
            color: #52c41a;
            margin-left: 8rpx;
          }
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
