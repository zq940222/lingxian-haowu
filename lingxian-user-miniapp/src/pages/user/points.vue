<template>
  <view class="container">
    <!-- 积分卡片 -->
    <view class="points-card">
      <view class="points-info">
        <text class="label">当前积分</text>
        <text class="value">{{ userStore.userInfo?.points || 0 }}</text>
      </view>
      <view class="sign-btn" :class="{ signed: signedToday }" @click="handleSignIn">
        <uni-icons :type="signedToday ? 'checkbox-filled' : 'calendar'" size="20" color="#fff" />
        <text>{{ signedToday ? '已签到' : '签到' }}</text>
      </view>
    </view>

    <!-- 签到日历 -->
    <view class="sign-calendar">
      <view class="calendar-header">
        <text>本月签到 {{ signDays }} 天</text>
      </view>
      <view class="calendar-week">
        <text v-for="day in weekDays" :key="day">{{ day }}</text>
      </view>
      <view class="calendar-days">
        <view
          class="day-item"
          :class="{
            'other-month': !isCurrentMonth(day),
            'signed': isSignedDay(day),
            'today': isToday(day)
          }"
          v-for="day in calendarDays"
          :key="day.date"
        >
          <text>{{ day.day }}</text>
          <view class="dot" v-if="isSignedDay(day)"></view>
        </view>
      </view>
    </view>

    <!-- 积分规则 -->
    <view class="section">
      <view class="section-header">
        <text>积分规则</text>
      </view>
      <view class="rule-list">
        <view class="rule-item">
          <uni-icons type="checkbox-filled" size="18" color="#52c41a" />
          <text>每日签到 +{{ signPoints }} 积分</text>
        </view>
        <view class="rule-item">
          <uni-icons type="checkbox-filled" size="18" color="#52c41a" />
          <text>购物消费 每1元 +1 积分</text>
        </view>
        <view class="rule-item">
          <uni-icons type="checkbox-filled" size="18" color="#52c41a" />
          <text>评价订单 +10 积分</text>
        </view>
        <view class="rule-item">
          <uni-icons type="checkbox-filled" size="18" color="#52c41a" />
          <text>邀请好友 +50 积分</text>
        </view>
      </view>
    </view>

    <!-- 积分记录 -->
    <view class="section">
      <view class="section-header">
        <text>积分记录</text>
      </view>
      <view class="record-list">
        <view class="record-item" v-for="item in records" :key="item.id">
          <view class="info">
            <text class="title">{{ item.title }}</text>
            <text class="time">{{ item.createTime }}</text>
          </view>
          <text class="points" :class="{ minus: item.points < 0 }">
            {{ item.points > 0 ? '+' : '' }}{{ item.points }}
          </text>
        </view>
      </view>
      <view class="load-more" v-if="hasMore" @click="loadMore">
        加载更多
      </view>
      <view class="no-more" v-if="!hasMore && records.length > 0">
        没有更多了
      </view>
      <view class="empty" v-if="records.length === 0">
        <uni-icons type="medal" size="64" color="#ccc" />
        <text>暂无积分记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { pointsApi } from '@/api'

const userStore = useUserStore()

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const signPoints = 10

const signedToday = ref(false)
const signDays = ref(0)
const signedDates = ref([])
const calendarDays = ref([])
const records = ref([])
const page = ref(1)
const hasMore = ref(true)

onMounted(() => {
  checkSignStatus()
  generateCalendar()
  loadRecords()
})

// 检查签到状态
const checkSignStatus = async () => {
  try {
    const res = await pointsApi.getSignStatus()
    if (res.code === 200) {
      signedToday.value = res.data.signedToday
      signDays.value = res.data.signDays || 0
      signedDates.value = res.data.signedDates || []
    }
  } catch (e) {
    console.error('获取签到状态失败', e)
  }
}

// 签到
const handleSignIn = async () => {
  if (signedToday.value) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }

  try {
    const res = await pointsApi.signIn()
    if (res.code === 200) {
      signedToday.value = true
      signDays.value++
      const today = formatDate(new Date())
      signedDates.value.push(today)
      userStore.updateUserInfo({
        points: (userStore.userInfo?.points || 0) + signPoints
      })
      uni.showToast({ title: `签到成功 +${signPoints}积分`, icon: 'success' })
      loadRecords()
    }
  } catch (e) {
    uni.showToast({ title: '签到失败', icon: 'none' })
  }
}

// 生成日历
const generateCalendar = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()

  // 本月第一天
  const firstDay = new Date(year, month, 1)
  const firstDayWeek = firstDay.getDay()

  // 本月最后一天
  const lastDay = new Date(year, month + 1, 0)
  const daysInMonth = lastDay.getDate()

  const days = []

  // 上个月的日期
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  for (let i = firstDayWeek - 1; i >= 0; i--) {
    days.push({
      date: `${year}-${month}-${prevMonthLastDay - i}`,
      day: prevMonthLastDay - i,
      month: month - 1
    })
  }

  // 本月日期
  for (let i = 1; i <= daysInMonth; i++) {
    days.push({
      date: `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`,
      day: i,
      month: month
    })
  }

  // 下个月的日期补齐
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    days.push({
      date: `${year}-${month + 2}-${i}`,
      day: i,
      month: month + 1
    })
  }

  calendarDays.value = days
}

// 格式化日期
const formatDate = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

// 是否当前月
const isCurrentMonth = (day) => {
  const now = new Date()
  return day.month === now.getMonth()
}

// 是否今天
const isToday = (day) => {
  const today = formatDate(new Date())
  return day.date === today
}

// 是否已签到日期
const isSignedDay = (day) => {
  return signedDates.value.includes(day.date)
}

// 加载积分记录
const loadRecords = async () => {
  try {
    const res = await pointsApi.getRecords({
      page: page.value,
      pageSize: 10
    })
    if (res.code === 200) {
      const list = res.data.records || []
      if (page.value === 1) {
        records.value = list
      } else {
        records.value = [...records.value, ...list]
      }
      hasMore.value = list.length >= 10
    }
  } catch (e) {
    console.error('加载积分记录失败', e)
  }
}

// 加载更多
const loadMore = () => {
  page.value++
  loadRecords()
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 积分卡片 */
.points-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx;
  background: linear-gradient(135deg, $primary-color, #36cfc9);
  margin: 20rpx;
  border-radius: $border-radius-lg;

  .points-info {
    .label {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.8);
      display: block;
    }

    .value {
      font-size: 60rpx;
      font-weight: bold;
      color: #fff;
      margin-top: 10rpx;
      display: block;
    }
  }

  .sign-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 40rpx;
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 16rpx;

    text {
      font-size: 24rpx;
      color: #fff;
      margin-top: 8rpx;
    }

    &.signed {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }
}

/* 签到日历 */
.sign-calendar {
  margin: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  .calendar-header {
    margin-bottom: 20rpx;

    text {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .calendar-week {
    display: flex;

    text {
      flex: 1;
      text-align: center;
      font-size: 24rpx;
      color: #999;
      padding: 16rpx 0;
    }
  }

  .calendar-days {
    display: flex;
    flex-wrap: wrap;

    .day-item {
      width: calc(100% / 7);
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16rpx 0;

      text {
        width: 48rpx;
        height: 48rpx;
        line-height: 48rpx;
        text-align: center;
        font-size: 26rpx;
        color: #333;
        border-radius: 50%;
      }

      .dot {
        width: 8rpx;
        height: 8rpx;
        background-color: $primary-color;
        border-radius: 50%;
        margin-top: 4rpx;
      }

      &.other-month text {
        color: #ccc;
      }

      &.today text {
        background-color: #f5f5f5;
      }

      &.signed text {
        background-color: $primary-color;
        color: #fff;
      }
    }
  }
}

/* 通用区块 */
.section {
  margin: 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .section-header {
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    text {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
    }
  }
}

/* 积分规则 */
.rule-list {
  padding: 20rpx 30rpx;

  .rule-item {
    display: flex;
    align-items: center;
    padding: 12rpx 0;

    text {
      font-size: 26rpx;
      color: #666;
      margin-left: 16rpx;
    }
  }
}

/* 积分记录 */
.record-list {
  .record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .info {
      .title {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .time {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
    }

    .points {
      font-size: 32rpx;
      font-weight: bold;
      color: $primary-color;

      &.minus {
        color: #999;
      }
    }
  }
}

.load-more, .no-more, .empty {
  text-align: center;
  padding: 30rpx;
  font-size: 26rpx;
  color: #999;
}

.load-more {
  color: $primary-color;
}
</style>
