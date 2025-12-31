<template>
  <view class="container">
    <!-- 时间筛选 -->
    <view class="filter-bar">
      <view
        class="filter-item"
        :class="{ active: dateRange === 'today' }"
        @click="selectDateRange('today')"
      >
        今日
      </view>
      <view
        class="filter-item"
        :class="{ active: dateRange === 'week' }"
        @click="selectDateRange('week')"
      >
        本周
      </view>
      <view
        class="filter-item"
        :class="{ active: dateRange === 'month' }"
        @click="selectDateRange('month')"
      >
        本月
      </view>
      <view class="filter-item custom" @click="showDatePicker = true">
        <uni-icons type="calendar" size="16" color="#666" />
        <text>自定义</text>
      </view>
    </view>

    <!-- 自定义日期显示 -->
    <view class="custom-date" v-if="dateRange === 'custom'">
      <text>{{ customStartDate }} 至 {{ customEndDate }}</text>
    </view>

    <!-- 销售概览 -->
    <view class="section">
      <view class="section-header">
        <text class="title">销售概览</text>
      </view>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="value">¥{{ salesData.totalAmount }}</text>
          <text class="label">销售额</text>
        </view>
        <view class="stat-item">
          <text class="value">{{ salesData.orderCount }}</text>
          <text class="label">订单数</text>
        </view>
        <view class="stat-item">
          <text class="value">¥{{ salesData.avgAmount }}</text>
          <text class="label">客单价</text>
        </view>
        <view class="stat-item">
          <text class="value">{{ salesData.productCount }}</text>
          <text class="label">售出商品</text>
        </view>
      </view>
    </view>

    <!-- 订单统计 -->
    <view class="section">
      <view class="section-header">
        <text class="title">订单统计</text>
      </view>
      <view class="order-stats">
        <view class="order-stat-item">
          <view class="icon-wrap pending">
            <uni-icons type="list" size="24" color="#fff" />
          </view>
          <view class="info">
            <text class="value">{{ orderData.pendingCount }}</text>
            <text class="label">待接单</text>
          </view>
        </view>
        <view class="order-stat-item">
          <view class="icon-wrap processing">
            <uni-icons type="car" size="24" color="#fff" />
          </view>
          <view class="info">
            <text class="value">{{ orderData.deliveryCount }}</text>
            <text class="label">待配送</text>
          </view>
        </view>
        <view class="order-stat-item">
          <view class="icon-wrap completed">
            <uni-icons type="checkbox-filled" size="24" color="#fff" />
          </view>
          <view class="info">
            <text class="value">{{ orderData.completedCount }}</text>
            <text class="label">已完成</text>
          </view>
        </view>
        <view class="order-stat-item">
          <view class="icon-wrap canceled">
            <uni-icons type="closeempty" size="24" color="#fff" />
          </view>
          <view class="info">
            <text class="value">{{ orderData.canceledCount }}</text>
            <text class="label">已取消</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 商品销量排行 -->
    <view class="section">
      <view class="section-header">
        <text class="title">商品销量TOP10</text>
      </view>
      <view class="product-rank" v-if="productRank.length > 0">
        <view class="rank-item" v-for="(item, index) in productRank" :key="item.id">
          <view class="rank-num" :class="{ top: index < 3 }">{{ index + 1 }}</view>
          <image :src="item.image" mode="aspectFill" />
          <view class="info">
            <text class="name">{{ item.name }}</text>
            <text class="sales">销量: {{ item.sales }}</text>
          </view>
          <text class="amount">¥{{ item.amount }}</text>
        </view>
      </view>
      <view class="empty-rank" v-else>
        <text>暂无数据</text>
      </view>
    </view>

    <!-- 销售趋势 -->
    <view class="section">
      <view class="section-header">
        <text class="title">销售趋势</text>
        <view class="toggle">
          <text :class="{ active: trendType === 'amount' }" @click="trendType = 'amount'">金额</text>
          <text :class="{ active: trendType === 'count' }" @click="trendType = 'count'">订单</text>
        </view>
      </view>
      <view class="chart-container">
        <view class="chart-bars">
          <view
            class="bar-item"
            v-for="(item, index) in trendData"
            :key="index"
          >
            <view class="bar" :style="{ height: getBarHeight(item) + '%' }">
              <text class="bar-value">{{ trendType === 'amount' ? item.amount : item.count }}</text>
            </view>
            <text class="bar-label">{{ item.label }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 日期选择弹窗 -->
    <uni-popup ref="datePopup" type="bottom" v-if="showDatePicker">
      <view class="date-picker-popup">
        <view class="popup-header">
          <text @click="closeDatePicker">取消</text>
          <text class="title">选择日期范围</text>
          <text class="confirm" @click="confirmDatePicker">确定</text>
        </view>
        <view class="date-inputs">
          <picker mode="date" :value="tempStartDate" @change="onStartDateChange">
            <view class="date-input">
              <text>开始日期</text>
              <text class="date-value">{{ tempStartDate || '请选择' }}</text>
            </view>
          </picker>
          <text class="separator">至</text>
          <picker mode="date" :value="tempEndDate" @change="onEndDateChange">
            <view class="date-input">
              <text>结束日期</text>
              <text class="date-value">{{ tempEndDate || '请选择' }}</text>
            </view>
          </picker>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { statisticsApi } from '@/api'

const dateRange = ref('today')
const showDatePicker = ref(false)
const customStartDate = ref('')
const customEndDate = ref('')
const tempStartDate = ref('')
const tempEndDate = ref('')

const trendType = ref('amount')

const salesData = ref({
  totalAmount: '0.00',
  orderCount: 0,
  avgAmount: '0.00',
  productCount: 0
})

const orderData = ref({
  pendingCount: 0,
  deliveryCount: 0,
  completedCount: 0,
  canceledCount: 0
})

const productRank = ref([])
const trendData = ref([])

onShow(() => {
  loadData()
})

// 选择日期范围
const selectDateRange = (range) => {
  dateRange.value = range
  loadData()
}

// 获取日期参数
const getDateParams = () => {
  const now = new Date()
  let startDate, endDate

  if (dateRange.value === 'today') {
    startDate = formatDate(now)
    endDate = formatDate(now)
  } else if (dateRange.value === 'week') {
    const dayOfWeek = now.getDay()
    const monday = new Date(now)
    monday.setDate(now.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1))
    startDate = formatDate(monday)
    endDate = formatDate(now)
  } else if (dateRange.value === 'month') {
    const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
    startDate = formatDate(firstDay)
    endDate = formatDate(now)
  } else {
    startDate = customStartDate.value
    endDate = customEndDate.value
  }

  return { startDate, endDate }
}

// 格式化日期
const formatDate = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 加载数据
const loadData = async () => {
  const params = getDateParams()

  try {
    const [salesRes, ordersRes, productsRes] = await Promise.all([
      statisticsApi.getSales(params),
      statisticsApi.getOrders(params),
      statisticsApi.getProducts(params)
    ])

    if (salesRes.code === 200) {
      salesData.value = salesRes.data.summary || salesData.value
      trendData.value = salesRes.data.trend || []
    }

    if (ordersRes.code === 200) {
      orderData.value = ordersRes.data || orderData.value
    }

    if (productsRes.code === 200) {
      productRank.value = productsRes.data || []
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

// 计算柱状图高度
const getBarHeight = (item) => {
  const values = trendData.value.map(d => trendType.value === 'amount' ? d.amount : d.count)
  const max = Math.max(...values, 1)
  const value = trendType.value === 'amount' ? item.amount : item.count
  return Math.max((value / max) * 80, 5)
}

// 日期选择相关
const onStartDateChange = (e) => {
  tempStartDate.value = e.detail.value
}

const onEndDateChange = (e) => {
  tempEndDate.value = e.detail.value
}

const closeDatePicker = () => {
  showDatePicker.value = false
}

const confirmDatePicker = () => {
  if (tempStartDate.value && tempEndDate.value) {
    customStartDate.value = tempStartDate.value
    customEndDate.value = tempEndDate.value
    dateRange.value = 'custom'
    showDatePicker.value = false
    loadData()
  } else {
    uni.showToast({ title: '请选择日期', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  padding: 20rpx;
  background-color: #fff;

  .filter-item {
    flex: 1;
    height: 64rpx;
    line-height: 64rpx;
    text-align: center;
    font-size: 26rpx;
    color: #666;
    background-color: #f5f5f5;
    border-radius: 32rpx;
    margin-right: 16rpx;

    &:last-child {
      margin-right: 0;
    }

    &.active {
      color: #fff;
      background-color: $primary-color;
    }

    &.custom {
      display: flex;
      align-items: center;
      justify-content: center;

      text {
        margin-left: 8rpx;
      }
    }
  }
}

.custom-date {
  text-align: center;
  padding: 16rpx;
  background-color: #fff;
  font-size: 24rpx;
  color: #666;
  border-top: 1rpx solid #f0f0f0;
}

/* 通用区块 */
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
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
    }

    .toggle {
      display: flex;
      background-color: #f5f5f5;
      border-radius: 20rpx;
      padding: 4rpx;

      text {
        padding: 8rpx 24rpx;
        font-size: 24rpx;
        color: #666;
        border-radius: 16rpx;

        &.active {
          color: #fff;
          background-color: $primary-color;
        }
      }
    }
  }
}

/* 销售概览 */
.stats-grid {
  display: flex;
  padding: 30rpx 0;

  .stat-item {
    flex: 1;
    text-align: center;

    .value {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      display: block;
    }

    .label {
      font-size: 24rpx;
      color: #999;
      margin-top: 10rpx;
      display: block;
    }
  }
}

/* 订单统计 */
.order-stats {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx;

  .order-stat-item {
    width: 50%;
    display: flex;
    align-items: center;
    padding: 20rpx;

    .icon-wrap {
      width: 80rpx;
      height: 80rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;

      &.pending { background-color: #ff9800; }
      &.processing { background-color: #1890ff; }
      &.completed { background-color: #52c41a; }
      &.canceled { background-color: #999; }
    }

    .info {
      .value {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
        display: block;
      }

      .label {
        font-size: 24rpx;
        color: #999;
        display: block;
      }
    }
  }
}

/* 商品排行 */
.product-rank {
  padding: 0 30rpx;

  .rank-item {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .rank-num {
      width: 40rpx;
      height: 40rpx;
      line-height: 40rpx;
      text-align: center;
      font-size: 24rpx;
      color: #999;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      margin-right: 16rpx;

      &.top {
        color: #fff;
        background-color: $primary-color;
      }
    }

    image {
      width: 80rpx;
      height: 80rpx;
      border-radius: $border-radius-sm;
      margin-right: 16rpx;
    }

    .info {
      flex: 1;

      .name {
        font-size: 28rpx;
        color: #333;
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .sales {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
    }

    .amount {
      font-size: 28rpx;
      color: $danger-color;
      font-weight: bold;
    }
  }
}

.empty-rank {
  padding: 60rpx;
  text-align: center;

  text {
    font-size: 26rpx;
    color: #999;
  }
}

/* 图表 */
.chart-container {
  padding: 30rpx;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 300rpx;

  .bar-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .bar {
      width: 40rpx;
      background: linear-gradient(180deg, $primary-color, #36cfc9);
      border-radius: 4rpx 4rpx 0 0;
      position: relative;
      min-height: 10rpx;

      .bar-value {
        position: absolute;
        top: -40rpx;
        left: 50%;
        transform: translateX(-50%);
        font-size: 20rpx;
        color: #666;
        white-space: nowrap;
      }
    }

    .bar-label {
      font-size: 20rpx;
      color: #999;
      margin-top: 10rpx;
    }
  }
}

/* 日期选择弹窗 */
.date-picker-popup {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding-bottom: env(safe-area-inset-bottom);

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    text {
      font-size: 28rpx;
      color: #999;
    }

    .title {
      font-weight: bold;
      color: #333;
    }

    .confirm {
      color: $primary-color;
    }
  }

  .date-inputs {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40rpx;

    .date-input {
      text-align: center;

      text:first-child {
        font-size: 24rpx;
        color: #999;
        display: block;
        margin-bottom: 16rpx;
      }

      .date-value {
        font-size: 32rpx;
        color: #333;
        padding: 16rpx 30rpx;
        background-color: #f5f5f5;
        border-radius: 8rpx;
      }
    }

    .separator {
      margin: 0 30rpx;
      font-size: 28rpx;
      color: #666;
    }
  }
}
</style>
