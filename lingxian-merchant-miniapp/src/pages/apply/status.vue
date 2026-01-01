<template>
  <view class="container">
    <!-- 状态卡片 -->
    <view class="status-card" :class="statusClass">
      <!-- 图标 -->
      <view class="status-icon">
        <uni-icons
          :type="statusIcon"
          size="60"
          :color="statusColor"
        />
      </view>

      <!-- 状态文本 -->
      <text class="status-text">{{ statusText }}</text>
      <text class="status-desc">{{ statusDesc }}</text>

      <!-- 拒绝原因 -->
      <view v-if="verifyStatus === 3 && verifyRemark" class="reject-reason">
        <text class="reason-title">拒绝原因:</text>
        <text class="reason-content">{{ verifyRemark }}</text>
      </view>
    </view>

    <!-- 店铺信息预览 -->
    <view v-if="merchantInfo" class="info-card">
      <view class="card-title">申请信息</view>

      <view class="info-item">
        <text class="label">店铺名称</text>
        <text class="value">{{ merchantInfo.shopName }}</text>
      </view>
      <view class="info-item">
        <text class="label">经营类目</text>
        <text class="value">{{ merchantInfo.category }}</text>
      </view>
      <view class="info-item">
        <text class="label">联系人</text>
        <text class="value">{{ merchantInfo.contactName }}</text>
      </view>
      <view class="info-item">
        <text class="label">联系电话</text>
        <text class="value">{{ merchantInfo.contactPhone }}</text>
      </view>
      <view class="info-item">
        <text class="label">店铺地址</text>
        <text class="value">{{ fullAddress }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-section">
      <!-- 未提交状态 -->
      <view v-if="verifyStatus === 0" class="btn-group">
        <button class="primary-btn" @click="goApply">
          立即申请入驻
        </button>
      </view>

      <!-- 待审核状态 -->
      <view v-else-if="verifyStatus === 1" class="btn-group">
        <button class="default-btn" @click="refreshStatus">
          刷新状态
        </button>
      </view>

      <!-- 审核通过 -->
      <view v-else-if="verifyStatus === 2" class="btn-group">
        <button class="primary-btn" @click="goWorkbench">
          进入工作台
        </button>
      </view>

      <!-- 审核拒绝 -->
      <view v-else-if="verifyStatus === 3" class="btn-group">
        <button class="primary-btn" @click="goEditApply">
          修改并重新提交
        </button>
        <button class="default-btn" @click="refreshStatus">
          刷新状态
        </button>
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="tips">
      <view class="tips-title">温馨提示</view>
      <view class="tips-content">
        <text>1. 审核时间一般为1-3个工作日</text>
        <text>2. 审核结果将通过微信服务通知告知</text>
        <text>3. 如有疑问请联系客服: 400-xxx-xxxx</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMerchantStore, VerifyStatus } from '@/store/merchant'

const merchantStore = useMerchantStore()

const verifyStatus = ref(0)
const verifyRemark = ref('')
const merchantInfo = ref(null)

const statusClass = computed(() => {
  switch (verifyStatus.value) {
    case VerifyStatus.NOT_SUBMITTED: return 'not-submitted'
    case VerifyStatus.PENDING: return 'pending'
    case VerifyStatus.APPROVED: return 'approved'
    case VerifyStatus.REJECTED: return 'rejected'
    default: return ''
  }
})

const statusIcon = computed(() => {
  switch (verifyStatus.value) {
    case VerifyStatus.NOT_SUBMITTED: return 'plusempty'
    case VerifyStatus.PENDING: return 'loop'
    case VerifyStatus.APPROVED: return 'checkmarkempty'
    case VerifyStatus.REJECTED: return 'closeempty'
    default: return 'info'
  }
})

const statusColor = computed(() => {
  switch (verifyStatus.value) {
    case VerifyStatus.NOT_SUBMITTED: return '#999'
    case VerifyStatus.PENDING: return '#faad14'
    case VerifyStatus.APPROVED: return '#52c41a'
    case VerifyStatus.REJECTED: return '#ff4d4f'
    default: return '#999'
  }
})

const statusText = computed(() => {
  switch (verifyStatus.value) {
    case VerifyStatus.NOT_SUBMITTED: return '未提交申请'
    case VerifyStatus.PENDING: return '审核中'
    case VerifyStatus.APPROVED: return '审核通过'
    case VerifyStatus.REJECTED: return '审核未通过'
    default: return '未知状态'
  }
})

const statusDesc = computed(() => {
  switch (verifyStatus.value) {
    case VerifyStatus.NOT_SUBMITTED: return '您还未提交入驻申请，点击下方按钮开始申请'
    case VerifyStatus.PENDING: return '您的申请正在审核中，请耐心等待'
    case VerifyStatus.APPROVED: return '恭喜！您的入驻申请已通过，可以开始营业了'
    case VerifyStatus.REJECTED: return '很抱歉，您的申请未通过审核，请查看原因并修改后重新提交'
    default: return ''
  }
})

const fullAddress = computed(() => {
  if (!merchantInfo.value) return ''
  const { province, city, district, address } = merchantInfo.value
  return `${province || ''}${city || ''}${district || ''}${address || ''}`
})

const fetchStatus = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const data = await merchantStore.fetchApplyStatus()
    verifyStatus.value = data.verifyStatus
    verifyRemark.value = data.verifyRemark || ''
    merchantInfo.value = data.merchantInfo || null
    uni.hideLoading()
  } catch (e) {
    uni.hideLoading()
    // 如果获取失败，使用store中的数据
    verifyStatus.value = merchantStore.verifyStatus
    merchantInfo.value = merchantStore.merchantInfo
  }
}

const refreshStatus = async () => {
  await fetchStatus()
  uni.showToast({ title: '已刷新', icon: 'success' })
}

const goApply = () => {
  uni.navigateTo({ url: '/pages/apply/index' })
}

const goEditApply = () => {
  uni.navigateTo({ url: '/pages/apply/edit' })
}

const goWorkbench = () => {
  uni.switchTab({ url: '/pages/index/index' })
}

onMounted(() => {
  // 检查登录状态
  if (!merchantStore.checkLoginStatus()) {
    return
  }
  fetchStatus()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 30rpx;
  padding-bottom: 60rpx;
}

.status-card {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx;
  text-align: center;
  margin-bottom: 30rpx;

  .status-icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 30rpx;
    background-color: #f5f5f5;
  }

  &.pending .status-icon {
    background-color: #fff7e6;
  }

  &.approved .status-icon {
    background-color: #f6ffed;
  }

  &.rejected .status-icon {
    background-color: #fff2f0;
  }

  .status-text {
    display: block;
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 16rpx;
  }

  .status-desc {
    display: block;
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }

  .reject-reason {
    margin-top: 30rpx;
    padding: 24rpx;
    background-color: #fff2f0;
    border-radius: 12rpx;
    text-align: left;

    .reason-title {
      display: block;
      font-size: 26rpx;
      color: #ff4d4f;
      margin-bottom: 8rpx;
    }

    .reason-content {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
    }
  }
}

.info-card {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .card-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
    padding-left: 20rpx;
    border-left: 6rpx solid $primary-color;
  }

  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      font-size: 28rpx;
      color: #999;
    }

    .value {
      font-size: 28rpx;
      color: #333;
      max-width: 60%;
      text-align: right;
    }
  }
}

.action-section {
  margin-bottom: 30rpx;

  .btn-group {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }

  .primary-btn {
    width: 100%;
    height: 96rpx;
    line-height: 96rpx;
    background: linear-gradient(135deg, $primary-color, #36cfc9);
    color: #fff;
    font-size: 32rpx;
    border-radius: 48rpx;
    border: none;
  }

  .default-btn {
    width: 100%;
    height: 96rpx;
    line-height: 96rpx;
    background-color: #fff;
    color: #666;
    font-size: 32rpx;
    border-radius: 48rpx;
    border: 2rpx solid #ddd;
  }
}

.tips {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;

  .tips-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .tips-content {
    text {
      display: block;
      font-size: 24rpx;
      color: #999;
      line-height: 2;
    }
  }
}
</style>
