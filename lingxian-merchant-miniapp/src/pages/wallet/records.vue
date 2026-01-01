<template>
  <view class="container">
    <!-- 提现记录列表 -->
    <view class="record-list">
      <view class="record-item" v-for="item in records" :key="item.id" @click="goDetail(item)">
        <view class="left">
          <view class="icon-wrap" :class="item.accountType">
            <text v-if="item.accountType === 'wechat'" class="icon-text">微</text>
            <text v-else-if="item.accountType === 'alipay'" class="icon-text">支</text>
            <text v-else class="icon-text">银</text>
          </view>
          <view class="info">
            <text class="account">{{ item.accountName }}</text>
            <text class="time">{{ item.createTime }}</text>
          </view>
        </view>
        <view class="right">
          <text class="amount">-¥{{ item.amount }}</text>
          <text class="status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</text>
        </view>
      </view>

      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && records.length > 0">没有更多了</view>
      <view class="empty" v-if="!loading && records.length === 0">
        <uni-icons type="wallet" size="60" color="#ccc" />
        <text>暂无提现记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { walletApi } from '@/api'

const records = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

onLoad(() => {
  uni.setNavigationBarTitle({ title: '提现记录' })
  loadRecords()
})

onReachBottom(() => {
  if (!loading.value && !noMore.value) {
    loadRecords()
  }
})

// 加载记录
const loadRecords = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const res = await walletApi.getWithdrawRecords({
      page: page.value,
      pageSize: 20
    })
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

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '处理中',
    2: '已到账',
    3: '已拒绝',
    4: '已取消'
  }
  return map[status] || '未知'
}

// 获取状态样式
const getStatusClass = (status) => {
  const map = {
    0: 'pending',
    1: 'processing',
    2: 'success',
    3: 'failed',
    4: 'cancelled'
  }
  return map[status] || ''
}

// 查看详情
const goDetail = (item) => {
  uni.navigateTo({
    url: `/pages/wallet/withdraw-detail?id=${item.id}`
  })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.record-list {
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .left {
    display: flex;
    align-items: center;

    .icon-wrap {
      width: 64rpx;
      height: 64rpx;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;

      &.wechat { background-color: #07c160; }
      &.alipay { background-color: #1677ff; }
      &.bank { background-color: #1890ff; }

      .icon-text {
        font-size: 28rpx;
        font-weight: bold;
        color: #fff;
      }
    }

    .info {
      .account {
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
  }

  .right {
    text-align: right;

    .amount {
      display: block;
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 8rpx;
    }

    .status {
      font-size: 24rpx;

      &.pending { color: #faad14; }
      &.processing { color: #1890ff; }
      &.success { color: #52c41a; }
      &.failed { color: #ff4d4f; }
      &.cancelled { color: #999; }
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
  padding: 80rpx 0;

  text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }
}
</style>
