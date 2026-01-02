<template>
  <view class="container">
    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="balance-info">
        <text class="label">可提现余额(元)</text>
        <text class="amount">{{ balance }}</text>
      </view>
      <view class="actions">
        <view class="btn-withdraw" @click="goWithdraw">立即提现</view>
      </view>
      <view class="extra-info">
        <view class="item">
          <text class="value">{{ totalIncome }}</text>
          <text class="label">累计收入</text>
        </view>
        <view class="item">
          <text class="value">{{ totalWithdraw }}</text>
          <text class="label">累计提现</text>
        </view>
        <view class="item">
          <text class="value">{{ pendingSettle }}</text>
          <text class="label">待结算</text>
        </view>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-entry">
      <view class="entry-item" @click="goWithdrawAccount">
        <uni-icons type="creditcard" size="24" color="#1890ff" />
        <text>提现账户</text>
      </view>
      <view class="entry-item" @click="goWithdrawRecords">
        <uni-icons type="list" size="24" color="#52c41a" />
        <text>提现记录</text>
      </view>
    </view>

    <!-- 收支明细 -->
    <view class="records-section">
      <view class="section-header">
        <text class="title">收支明细</text>
        <view class="filter" @click="showFilter = true">
          <text>{{ currentFilterText }}</text>
          <uni-icons type="down" size="14" color="#999" />
        </view>
      </view>

      <view class="record-list">
        <view class="record-item" v-for="item in records" :key="item.id">
          <view class="info">
            <text class="title">{{ item.title }}</text>
            <text class="time">{{ item.createTime }}</text>
          </view>
          <text class="amount" :class="{ income: item.type === 1 }">
            {{ item.type === 1 ? '+' : '-' }}{{ item.amount }}
          </text>
        </view>

        <view class="loading" v-if="loading">加载中...</view>
        <view class="no-more" v-if="noMore && records.length > 0">没有更多了</view>
        <view class="empty" v-if="!loading && records.length === 0">
          <uni-icons type="list" size="64" color="#ccc" />
          <text>暂无记录</text>
        </view>
      </view>
    </view>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom" v-if="showFilter">
      <view class="filter-popup">
        <view class="popup-header">
          <text>筛选</text>
          <uni-icons type="closeempty" size="20" color="#999" @click="showFilter = false" />
        </view>
        <view class="filter-options">
          <view
            class="option"
            :class="{ active: filterType === '' }"
            @click="selectFilter('')"
          >全部</view>
          <view
            class="option"
            :class="{ active: filterType === 'income' }"
            @click="selectFilter('income')"
          >收入</view>
          <view
            class="option"
            :class="{ active: filterType === 'withdraw' }"
            @click="selectFilter('withdraw')"
          >提现</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onReachBottom } from '@dcloudio/uni-app'
import { walletApi } from '@/api'

const balance = ref('0.00')
const totalIncome = ref('0.00')
const totalWithdraw = ref('0.00')
const pendingSettle = ref('0.00')

const records = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

const showFilter = ref(false)
const filterType = ref('')
const currentFilterText = ref('全部')

onLoad(() => {
  uni.setNavigationBarTitle({ title: '我的钱包' })
})

onShow(() => {
  loadBalance()
  resetAndLoad()
})

onReachBottom(() => {
  if (!loading.value && !noMore.value) {
    loadRecords()
  }
})

// 加载余额信息
const loadBalance = async () => {
  try {
    const res = await walletApi.getBalance()
    if (res.code === 200) {
      balance.value = res.data.balance || '0.00'
      totalIncome.value = res.data.totalIncome || '0.00'
      totalWithdraw.value = res.data.totalWithdraw || '0.00'
      pendingSettle.value = res.data.pendingSettle || '0.00'
    }
  } catch (e) {
    console.error('获取余额失败', e)
  }
}

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  records.value = []
  noMore.value = false
  loadRecords()
}

// 加载收支明细
const loadRecords = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      page: page.value,
      pageSize: 20,
      type: filterType.value
    }
    const res = await walletApi.getRecords(params)
    if (res.code === 200) {
      const list = res.data.records || []
      records.value = page.value === 1 ? list : [...records.value, ...list]
      page.value++
      noMore.value = list.length < 20
    }
  } catch (e) {
    console.error('加载记录失败', e)
  } finally {
    loading.value = false
  }
}

// 选择筛选
const selectFilter = (type) => {
  filterType.value = type
  currentFilterText.value = type === '' ? '全部' : (type === 'income' ? '收入' : '提现')
  showFilter.value = false
  resetAndLoad()
}

// 跳转提现
const goWithdraw = () => {
  uni.navigateTo({ url: '/pages/wallet/withdraw' })
}

// 跳转提现账户
const goWithdrawAccount = () => {
  uni.navigateTo({ url: '/pages/wallet/account' })
}

// 跳转提现记录
const goWithdrawRecords = () => {
  uni.navigateTo({ url: '/pages/wallet/records' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 余额卡片 */
.balance-card {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  padding: 40rpx 30rpx;
  margin: 20rpx;
  border-radius: $border-radius-lg;

  .balance-info {
    text-align: center;
    margin-bottom: 30rpx;

    .label {
      display: block;
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-bottom: 16rpx;
    }

    .amount {
      font-size: 72rpx;
      font-weight: bold;
      color: #fff;
    }
  }

  .actions {
    display: flex;
    justify-content: center;
    margin-bottom: 30rpx;

    .btn-withdraw {
      padding: 16rpx 60rpx;
      background-color: #fff;
      color: #1890ff;
      border-radius: 40rpx;
      font-size: 28rpx;
      font-weight: bold;
    }
  }

  .extra-info {
    display: flex;
    justify-content: space-around;
    padding-top: 30rpx;
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);

    .item {
      text-align: center;

      .value {
        display: block;
        font-size: 32rpx;
        font-weight: bold;
        color: #fff;
        margin-bottom: 8rpx;
      }

      .label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }
}

/* 快捷入口 */
.quick-entry {
  display: flex;
  background-color: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: $border-radius-lg;
  padding: 30rpx 0;

  .entry-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    text {
      font-size: 26rpx;
      color: #333;
      margin-top: 12rpx;
    }
  }
}

/* 收支明细 */
.records-section {
  background-color: #fff;
  margin: 0 20rpx;
  border-radius: $border-radius-lg;
  padding: 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .title {
      font-size: 30rpx;
      font-weight: bold;
      color: #333;
    }

    .filter {
      display: flex;
      align-items: center;

      text {
        font-size: 26rpx;
        color: #999;
        margin-right: 8rpx;
      }
    }
  }
}

.record-list {
  .record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .info {
      .title {
        display: block;
        font-size: 28rpx;
        color: #333;
        margin-bottom: 8rpx;
      }

      .time {
        font-size: 24rpx;
        color: #999;
      }
    }

    .amount {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;

      &.income {
        color: #52c41a;
      }
    }
  }
}

.loading, .no-more, .empty {
  text-align: center;
  padding: 30rpx;
  font-size: 26rpx;
  color: #999;
}

/* 筛选弹窗 */
.filter-popup {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .filter-options {
    display: flex;
    flex-wrap: wrap;

    .option {
      padding: 20rpx 40rpx;
      margin: 10rpx;
      background-color: #f5f5f5;
      border-radius: 8rpx;
      font-size: 28rpx;
      color: #666;

      &.active {
        background-color: #e6f7ff;
        color: #1890ff;
      }
    }
  }
}
</style>
