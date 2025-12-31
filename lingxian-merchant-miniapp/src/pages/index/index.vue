<template>
  <view class="container">
    <!-- 头部信息 -->
    <view class="header">
      <view class="shop-info">
        <image class="logo" :src="merchantStore.shopLogo" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ merchantStore.shopName }}</text>
          <view class="status" :class="{ open: shopStatus === 1 }">
            <text>{{ shopStatus === 1 ? '营业中' : '休息中' }}</text>
          </view>
        </view>
      </view>
      <view class="toggle-btn" @click="toggleShopStatus">
        <text>{{ shopStatus === 1 ? '暂停营业' : '开始营业' }}</text>
      </view>
    </view>

    <!-- 今日统计 -->
    <view class="stats-card">
      <view class="title">今日数据</view>
      <view class="stats-grid">
        <view class="stat-item" @click="goOrderList('all')">
          <text class="value">{{ todayStats.orderCount }}</text>
          <text class="label">订单数</text>
        </view>
        <view class="stat-item" @click="goStatistics">
          <text class="value">¥{{ todayStats.salesAmount }}</text>
          <text class="label">销售额</text>
        </view>
        <view class="stat-item" @click="goDelivery">
          <text class="value">{{ todayStats.deliveryCount }}</text>
          <text class="label">已配送</text>
        </view>
        <view class="stat-item" @click="goOrderList('1')">
          <text class="value highlight">{{ todayStats.pendingCount }}</text>
          <text class="label">待处理</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-entry">
      <view class="entry-item" @click="goOrderList('1')">
        <view class="icon-wrap pending">
          <uni-icons type="list" size="28" color="#fff" />
        </view>
        <text>待接单</text>
        <view class="badge" v-if="todayStats.pendingCount > 0">
          {{ todayStats.pendingCount > 99 ? '99+' : todayStats.pendingCount }}
        </view>
      </view>
      <view class="entry-item" @click="goDelivery">
        <view class="icon-wrap delivery">
          <uni-icons type="car" size="28" color="#fff" />
        </view>
        <text>待配送</text>
      </view>
      <view class="entry-item" @click="goProductList">
        <view class="icon-wrap product">
          <uni-icons type="shop" size="28" color="#fff" />
        </view>
        <text>商品管理</text>
      </view>
      <view class="entry-item" @click="goStatistics">
        <view class="icon-wrap stats">
          <uni-icons type="bars" size="28" color="#fff" />
        </view>
        <text>数据统计</text>
      </view>
    </view>

    <!-- 待处理订单 -->
    <view class="section" v-if="pendingOrders.length > 0">
      <view class="section-header">
        <text class="title">待处理订单</text>
        <view class="more" @click="goOrderList('1')">
          <text>全部</text>
          <uni-icons type="right" size="14" color="#999" />
        </view>
      </view>
      <view class="order-list">
        <view class="order-item" v-for="order in pendingOrders" :key="order.id">
          <view class="order-header" @click="goOrderDetail(order.id)">
            <text class="order-no">订单号: {{ order.orderNo }}</text>
            <text class="time">{{ order.createTime }}</text>
          </view>
          <view class="order-content" @click="goOrderDetail(order.id)">
            <view class="products">
              <text v-for="(p, i) in order.products" :key="i">
                {{ p.name }} x{{ p.quantity }}
              </text>
            </view>
            <view class="amount">¥{{ order.payAmount }}</view>
          </view>
          <view class="order-address" @click="goOrderDetail(order.id)">
            <uni-icons type="location" size="16" color="#999" />
            <text>{{ order.receiverAddress }}</text>
          </view>
          <view class="order-footer">
            <view class="customer">
              <text>{{ order.receiverName }} {{ order.receiverPhone }}</text>
            </view>
            <view class="actions">
              <view class="btn reject" @click="rejectOrder(order)">拒单</view>
              <view class="btn accept" @click="acceptOrder(order)">接单</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty" v-if="pendingOrders.length === 0">
      <image src="/static/images/empty-order.png" mode="aspectFit" />
      <text>暂无待处理订单</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useMerchantStore } from '@/store/merchant'
import { dashboardApi, orderApi, shopApi } from '@/api'

const merchantStore = useMerchantStore()

const shopStatus = ref(1)
const todayStats = ref({
  orderCount: 0,
  salesAmount: '0.00',
  deliveryCount: 0,
  pendingCount: 0
})
const pendingOrders = ref([])

onShow(() => {
  if (merchantStore.isLogin) {
    loadData()
  }
})

onPullDownRefresh(async () => {
  await loadData()
  uni.stopPullDownRefresh()
})

// 加载数据
const loadData = async () => {
  try {
    const [statsRes, ordersRes] = await Promise.all([
      dashboardApi.getTodayStats(),
      orderApi.getList({ status: 1, page: 1, pageSize: 5 })
    ])

    if (statsRes.code === 200) {
      todayStats.value = statsRes.data
    }

    if (ordersRes.code === 200) {
      pendingOrders.value = ordersRes.data.records || []
    }

    shopStatus.value = merchantStore.merchantInfo?.shopStatus || 0
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

// 切换营业状态
const toggleShopStatus = async () => {
  const newStatus = shopStatus.value === 1 ? 0 : 1
  try {
    await uni.showModal({
      title: '提示',
      content: `确定${newStatus === 1 ? '开始营业' : '暂停营业'}吗？`
    })

    const res = await shopApi.updateStatus(newStatus)
    if (res.code === 200) {
      shopStatus.value = newStatus
      merchantStore.updateMerchantInfo({ shopStatus: newStatus })
      uni.showToast({
        title: newStatus === 1 ? '已开始营业' : '已暂停营业',
        icon: 'success'
      })
    }
  } catch (e) {
    // 取消或失败
  }
}

// 接单
const acceptOrder = async (order) => {
  try {
    const res = await orderApi.accept(order.id)
    if (res.code === 200) {
      uni.showToast({ title: '接单成功', icon: 'success' })
      loadData()
    }
  } catch (e) {
    console.error('接单失败', e)
  }
}

// 拒单
const rejectOrder = async (order) => {
  try {
    const { content } = await uni.showModal({
      title: '拒单原因',
      editable: true,
      placeholderText: '请输入拒单原因'
    })

    if (content) {
      const res = await orderApi.reject(order.id, content)
      if (res.code === 200) {
        uni.showToast({ title: '已拒单', icon: 'success' })
        loadData()
      }
    }
  } catch (e) {
    // 取消
  }
}

// 跳转订单列表
const goOrderList = (status) => {
  uni.navigateTo({ url: `/pages/order/list?status=${status}` })
}

// 跳转订单详情
const goOrderDetail = (id) => {
  uni.navigateTo({ url: `/pages/order/detail?id=${id}` })
}

// 跳转配送
const goDelivery = () => {
  uni.switchTab({ url: '/pages/delivery/list' })
}

// 跳转商品管理
const goProductList = () => {
  uni.navigateTo({ url: '/pages/product/list' })
}

// 跳转统计
const goStatistics = () => {
  uni.navigateTo({ url: '/pages/statistics/index' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 头部 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background: linear-gradient(135deg, $primary-color, #36cfc9);
}

.shop-info {
  display: flex;
  align-items: center;

  .logo {
    width: 100rpx;
    height: 100rpx;
    border-radius: 16rpx;
    margin-right: 20rpx;
  }

  .info {
    .name {
      font-size: 32rpx;
      color: #fff;
      font-weight: bold;
      display: block;
    }

    .status {
      display: inline-block;
      padding: 4rpx 16rpx;
      border-radius: 20rpx;
      margin-top: 10rpx;
      font-size: 22rpx;
      background-color: rgba(255, 77, 79, 0.3);
      color: #fff;

      &.open {
        background-color: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

.toggle-btn {
  padding: 16rpx 32rpx;
  background-color: #fff;
  border-radius: 32rpx;
  font-size: 26rpx;
  color: $primary-color;
}

/* 统计卡片 */
.stats-card {
  margin: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  box-shadow: $box-shadow;

  .title {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
  }

  .stats-grid {
    display: flex;
  }

  .stat-item {
    flex: 1;
    text-align: center;

    .value {
      font-size: 40rpx;
      font-weight: bold;
      color: #333;
      display: block;

      &.highlight {
        color: $danger-color;
      }
    }

    .label {
      font-size: 24rpx;
      color: #999;
      margin-top: 10rpx;
      display: block;
    }
  }
}

/* 快捷入口 */
.quick-entry {
  display: flex;
  margin: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  .entry-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;

    .icon-wrap {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;

      &.pending { background-color: #ff9800; }
      &.delivery { background-color: #1890ff; }
      &.product { background-color: #52c41a; }
      &.stats { background-color: #722ed1; }
    }

    text {
      font-size: 24rpx;
      color: #333;
    }

    .badge {
      position: absolute;
      top: -10rpx;
      right: 20rpx;
      min-width: 32rpx;
      height: 32rpx;
      line-height: 32rpx;
      text-align: center;
      font-size: 20rpx;
      color: #fff;
      background-color: $danger-color;
      border-radius: 16rpx;
      padding: 0 8rpx;
    }
  }
}

/* 区块 */
.section {
  margin: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .title {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
    }

    .more {
      display: flex;
      align-items: center;
      font-size: 24rpx;
      color: #999;
    }
  }
}

/* 订单列表 */
.order-item {
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .order-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16rpx;

    .order-no {
      font-size: 24rpx;
      color: #666;
    }

    .time {
      font-size: 24rpx;
      color: #999;
    }
  }

  .order-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;

    .products {
      flex: 1;

      text {
        display: block;
        font-size: 28rpx;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .amount {
      font-size: 32rpx;
      color: $danger-color;
      font-weight: bold;
    }
  }

  .order-address {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;

    text {
      font-size: 26rpx;
      color: #666;
      margin-left: 8rpx;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .customer {
      font-size: 26rpx;
      color: #999;
    }

    .actions {
      display: flex;
    }

    .btn {
      padding: 12rpx 32rpx;
      border-radius: 32rpx;
      font-size: 26rpx;
      margin-left: 20rpx;

      &.reject {
        background-color: #f5f5f5;
        color: #666;
      }

      &.accept {
        background-color: $primary-color;
        color: #fff;
      }
    }
  }
}

/* 空状态 */
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
