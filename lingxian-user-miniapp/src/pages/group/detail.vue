<template>
  <view class="container">
    <!-- 商品图片 -->
    <swiper class="product-swiper" indicator-dots autoplay circular>
      <swiper-item v-for="(img, index) in activity.productImages || [activity.productImage]" :key="index">
        <image :src="img" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 拼团信息 -->
    <view class="group-info">
      <view class="price-section">
        <view class="group-price">
          <text class="label">拼团价</text>
          <text class="price">¥{{ activity.groupPrice }}</text>
        </view>
        <view class="original-price">
          <text class="label">原价</text>
          <text class="price">¥{{ activity.originalPrice }}</text>
        </view>
      </view>
      <view class="group-rule">
        <view class="rule-item">
          <text class="num">{{ activity.groupSize }}</text>
          <text>人成团</text>
        </view>
        <view class="rule-item">
          <text class="num">{{ activity.timeLimit || 24 }}</text>
          <text>小时有效</text>
        </view>
        <view class="rule-item">
          <text class="num">{{ activity.soldCount || 0 }}</text>
          <text>人已拼</text>
        </view>
      </view>
    </view>

    <!-- 商品信息 -->
    <view class="product-info">
      <text class="name">{{ activity.productName }}</text>
      <text class="desc">{{ activity.productDesc }}</text>
    </view>

    <!-- 正在拼团 -->
    <view class="ongoing-section" v-if="ongoingGroups.length > 0">
      <view class="section-header">
        <text>正在拼团，可直接参与</text>
      </view>
      <view class="ongoing-list">
        <view class="ongoing-item" v-for="group in ongoingGroups" :key="group.id">
          <view class="user-info">
            <image :src="group.leaderAvatar" mode="aspectFill" />
            <text>{{ group.leaderNickname }}</text>
          </view>
          <view class="countdown">
            <text>还差{{ group.needCount }}人</text>
            <text class="time">{{ group.remainTime }}</text>
          </view>
          <view class="join-btn" @click="joinGroup(group)">去参团</view>
        </view>
      </view>
    </view>

    <!-- 拼团规则 -->
    <view class="rules-section">
      <view class="section-header">
        <text>拼团规则</text>
      </view>
      <view class="rules-content">
        <view class="rule-step">
          <view class="step-icon">1</view>
          <view class="step-info">
            <text class="title">开团/参团</text>
            <text class="desc">选择商品，发起拼团或参与他人的团</text>
          </view>
        </view>
        <view class="rule-step">
          <view class="step-icon">2</view>
          <view class="step-info">
            <text class="title">邀请好友</text>
            <text class="desc">分享给好友，邀请TA们一起拼团</text>
          </view>
        </view>
        <view class="rule-step">
          <view class="step-icon">3</view>
          <view class="step-info">
            <text class="title">拼团成功</text>
            <text class="desc">达到成团人数，享受拼团价</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer">
      <view class="btn single" @click="buySingle">¥{{ activity.originalPrice }} 单独购买</view>
      <view class="btn group" @click="startGroup">¥{{ activity.groupPrice }} 发起拼团</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { groupApi } from '@/api'

const activityId = ref('')
const activity = ref({})
const ongoingGroups = ref([])

onLoad((options) => {
  activityId.value = options.id
  loadActivity()
  loadOngoingGroups()
})

// 加载活动详情
const loadActivity = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await groupApi.getActivityDetail(activityId.value)
    if (res.code === 200) {
      activity.value = res.data
    }
  } catch (e) {
    console.error('加载活动失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 加载进行中的拼团
const loadOngoingGroups = async () => {
  try {
    const res = await groupApi.getOngoingGroups(activityId.value)
    if (res.code === 200) {
      ongoingGroups.value = res.data || []
    }
  } catch (e) {
    console.error('加载拼团列表失败', e)
  }
}

// 单独购买
const buySingle = () => {
  uni.navigateTo({
    url: `/pages/product/detail?id=${activity.value.productId}`
  })
}

// 发起拼团
const startGroup = async () => {
  try {
    uni.showLoading({ title: '发起拼团中...' })
    const res = await groupApi.start({
      activityId: activityId.value
    })
    uni.hideLoading()

    if (res.code === 200) {
      // 跳转到支付或拼团详情
      if (res.data.payParams) {
        await uni.requestPayment(res.data.payParams)
      }
      uni.redirectTo({ url: `/pages/group/result?id=${res.data.groupId}` })
    }
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '发起失败', icon: 'none' })
  }
}

// 参与拼团
const joinGroup = async (group) => {
  try {
    uni.showLoading({ title: '参与拼团中...' })
    const res = await groupApi.join({
      groupId: group.id
    })
    uni.hideLoading()

    if (res.code === 200) {
      // 跳转到支付
      if (res.data.payParams) {
        await uni.requestPayment(res.data.payParams)
      }
      uni.redirectTo({ url: `/pages/group/result?id=${group.id}` })
    }
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '参与失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 140rpx;
}

/* 商品轮播图 */
.product-swiper {
  height: 750rpx;

  image {
    width: 100%;
    height: 100%;
  }
}

/* 拼团信息 */
.group-info {
  background: linear-gradient(135deg, #ff6b6b, #ffa06b);
  padding: 30rpx;
  margin: -40rpx 20rpx 0;
  border-radius: $border-radius-lg;
  position: relative;
  z-index: 10;

  .price-section {
    display: flex;
    align-items: baseline;
    margin-bottom: 30rpx;

    .group-price {
      .label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
        margin-right: 10rpx;
      }

      .price {
        font-size: 48rpx;
        font-weight: bold;
        color: #fff;
      }
    }

    .original-price {
      margin-left: 30rpx;

      .label {
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.6);
        margin-right: 6rpx;
      }

      .price {
        font-size: 28rpx;
        color: rgba(255, 255, 255, 0.6);
        text-decoration: line-through;
      }
    }
  }

  .group-rule {
    display: flex;
    justify-content: space-around;
    padding-top: 20rpx;
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);

    .rule-item {
      text-align: center;

      .num {
        font-size: 36rpx;
        font-weight: bold;
        color: #fff;
      }

      text {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }
}

/* 商品信息 */
.product-info {
  margin: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  .name {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    display: block;
    margin-bottom: 16rpx;
  }

  .desc {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }
}

/* 正在拼团 */
.ongoing-section {
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

  .ongoing-list {
    .ongoing-item {
      display: flex;
      align-items: center;
      padding: 24rpx 30rpx;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .user-info {
        display: flex;
        align-items: center;

        image {
          width: 64rpx;
          height: 64rpx;
          border-radius: 50%;
          margin-right: 16rpx;
        }

        text {
          font-size: 26rpx;
          color: #333;
        }
      }

      .countdown {
        flex: 1;
        text-align: center;

        text {
          font-size: 24rpx;
          color: #666;
          display: block;
        }

        .time {
          color: $danger-color;
          margin-top: 6rpx;
        }
      }

      .join-btn {
        padding: 12rpx 32rpx;
        background: linear-gradient(135deg, #ff6b6b, #ffa06b);
        color: #fff;
        font-size: 26rpx;
        border-radius: 30rpx;
      }
    }
  }
}

/* 拼团规则 */
.rules-section {
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

  .rules-content {
    padding: 30rpx;

    .rule-step {
      display: flex;
      align-items: flex-start;
      margin-bottom: 30rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .step-icon {
        width: 48rpx;
        height: 48rpx;
        line-height: 48rpx;
        text-align: center;
        background: linear-gradient(135deg, #ff6b6b, #ffa06b);
        color: #fff;
        font-size: 24rpx;
        font-weight: bold;
        border-radius: 50%;
        margin-right: 20rpx;
      }

      .step-info {
        flex: 1;

        .title {
          font-size: 28rpx;
          font-weight: bold;
          color: #333;
          display: block;
        }

        .desc {
          font-size: 24rpx;
          color: #999;
          margin-top: 8rpx;
          display: block;
        }
      }
    }
  }
}

/* 底部操作 */
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));

  .btn {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    border-radius: 44rpx;
    font-size: 28rpx;

    &.single {
      background-color: #f5f5f5;
      color: #666;
      margin-right: 20rpx;
    }

    &.group {
      background: linear-gradient(135deg, #ff6b6b, #ffa06b);
      color: #fff;
    }
  }
}
</style>
