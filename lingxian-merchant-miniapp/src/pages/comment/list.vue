<template>
  <view class="container">
    <!-- 评价统计 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="value">{{ stats.totalCount || 0 }}</text>
        <text class="label">总评价</text>
      </view>
      <view class="stat-item">
        <text class="value highlight">{{ stats.avgRating || '5.0' }}</text>
        <text class="label">平均评分</text>
      </view>
      <view class="stat-item">
        <text class="value">{{ stats.goodRate || '100%' }}</text>
        <text class="label">好评率</text>
      </view>
      <view class="stat-item">
        <text class="value">{{ stats.unrepliedCount || 0 }}</text>
        <text class="label">待回复</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ active: currentTab === item.value }"
        v-for="item in tabs"
        :key="item.value"
        @click="switchTab(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <!-- 评价列表 -->
    <scroll-view
      class="comment-list"
      scroll-y
      @scrolltolower="loadMore"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="comment-item" v-for="comment in comments" :key="comment.id">
        <view class="user-info">
          <image :src="comment.userAvatar || '/static/images/default-avatar.png'" mode="aspectFill" />
          <view class="user-detail">
            <text class="name">{{ comment.isAnonymous ? '匿名用户' : comment.userName }}</text>
            <view class="rating">
              <uni-icons
                v-for="star in 5"
                :key="star"
                :type="comment.rating >= star ? 'star-filled' : 'star'"
                size="14"
                :color="comment.rating >= star ? '#ffb800' : '#ddd'"
              />
              <text class="time">{{ comment.createTime }}</text>
            </view>
          </view>
        </view>

        <view class="product-info" @click="goProductDetail(comment.productId)">
          <image :src="comment.productImage" mode="aspectFill" />
          <text class="name">{{ comment.productName }}</text>
        </view>

        <view class="content" v-if="comment.content">
          {{ comment.content }}
        </view>

        <view class="images" v-if="comment.imageList && comment.imageList.length > 0">
          <image
            v-for="(img, index) in comment.imageList"
            :key="index"
            :src="img"
            mode="aspectFill"
            @click="previewImage(comment.imageList, index)"
          />
        </view>

        <!-- 商家回复 -->
        <view class="reply-box" v-if="comment.replyContent">
          <text class="reply-label">商家回复：</text>
          <text class="reply-content">{{ comment.replyContent }}</text>
        </view>

        <!-- 操作按钮 -->
        <view class="actions" v-if="!comment.replyContent">
          <view class="btn-reply" @click="showReplyModal(comment)">
            <uni-icons type="chat" size="16" color="#1890ff" />
            <text>回复</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loading">加载中...</view>
      <view class="no-more" v-if="noMore && comments.length > 0">没有更多评价了</view>
      <view class="empty" v-if="!loading && comments.length === 0">
        <uni-icons type="chat" size="64" color="#ccc" />
        <text>暂无评价</text>
      </view>
    </scroll-view>

    <!-- 回复弹窗 -->
    <uni-popup ref="replyPopup" type="bottom">
      <view class="reply-modal">
        <view class="modal-header">
          <text class="title">回复评价</text>
          <uni-icons type="closeempty" size="24" color="#999" @click="closeReplyModal" />
        </view>
        <textarea
          class="reply-input"
          v-model="replyContent"
          placeholder="请输入回复内容..."
          :maxlength="200"
        />
        <view class="word-count">{{ replyContent.length }}/200</view>
        <view class="modal-footer">
          <view class="btn-cancel" @click="closeReplyModal">取消</view>
          <view class="btn-confirm" @click="submitReply">确定</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMerchantStore, VerifyStatus } from '@/store/merchant'
import { commentApi } from '@/api'

const merchantStore = useMerchantStore()

const tabs = [
  { label: '全部', value: -1 },
  { label: '好评', value: 1 },
  { label: '中评', value: 2 },
  { label: '差评', value: 3 },
  { label: '待回复', value: 4 }
]

const currentTab = ref(-1)
const comments = ref([])
const stats = ref({})
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)

// 回复相关
const replyPopup = ref(null)
const replyContent = ref('')
const currentComment = ref(null)
const pendingReplyId = ref(null)  // 待回复的评价ID（从URL参数获取）

// 处理URL参数
onLoad((options) => {
  if (options.replyId) {
    pendingReplyId.value = parseInt(options.replyId)
  }
})

onShow(async () => {
  // 检查登录状态
  if (!merchantStore.checkLoginStatus()) {
    return
  }

  // 检查审核状态
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

  loadStats()
  resetAndLoad()
})

// 加载统计
const loadStats = async () => {
  try {
    const res = await commentApi.getStats()
    if (res.code === 200) {
      stats.value = res.data || {}
    }
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

// 切换标签
const switchTab = (tab) => {
  if (currentTab.value === tab) return
  currentTab.value = tab
  resetAndLoad()
}

// 重置并加载
const resetAndLoad = () => {
  page.value = 1
  comments.value = []
  noMore.value = false
  loadComments()
}

// 加载评价列表
const loadComments = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      page: page.value,
      pageSize: 10
    }

    // 筛选条件
    if (currentTab.value === 1) {
      params.ratingType = 'good' // 好评 4-5星
    } else if (currentTab.value === 2) {
      params.ratingType = 'medium' // 中评 3星
    } else if (currentTab.value === 3) {
      params.ratingType = 'bad' // 差评 1-2星
    } else if (currentTab.value === 4) {
      params.unreplied = 1 // 待回复
    }

    const res = await commentApi.getList(params)
    if (res.code === 200) {
      const list = (res.data.records || []).map(item => ({
        ...item,
        imageList: item.images ? item.images.split(',').filter(img => img) : []
      }))
      comments.value = page.value === 1 ? list : [...comments.value, ...list]
      page.value++
      noMore.value = list.length < 10

      // 如果有待回复的评价ID，自动打开回复弹窗
      if (pendingReplyId.value && page.value === 2) {
        await handlePendingReply()
      }
    }
  } catch (e) {
    console.error('加载评价失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    loadComments()
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadStats()
  resetAndLoad()
}

// 处理待回复的评价（从URL参数）
const handlePendingReply = async () => {
  if (!pendingReplyId.value) return

  const replyId = pendingReplyId.value
  pendingReplyId.value = null  // 清除，避免重复处理

  // 先从列表中查找
  let targetComment = comments.value.find(c => c.id === replyId)

  // 如果列表中找不到，单独获取评价详情
  if (!targetComment) {
    try {
      const res = await commentApi.getDetail(replyId)
      if (res.code === 200 && res.data) {
        targetComment = {
          ...res.data,
          imageList: res.data.images ? res.data.images.split(',').filter(img => img) : []
        }
      }
    } catch (e) {
      console.error('获取评价详情失败', e)
    }
  }

  if (targetComment) {
    await nextTick()
    showReplyModal(targetComment)
  } else {
    uni.showToast({ title: '评价不存在', icon: 'none' })
  }
}

// 显示回复弹窗
const showReplyModal = (comment) => {
  currentComment.value = comment
  replyContent.value = ''
  if (replyPopup.value) {
    replyPopup.value.open()
  } else {
    console.error('回复弹窗组件未初始化')
    uni.showToast({ title: '页面加载中，请稍后重试', icon: 'none' })
  }
}

// 关闭回复弹窗
const closeReplyModal = () => {
  if (replyPopup.value) {
    replyPopup.value.close()
  }
  currentComment.value = null
  replyContent.value = ''
}

// 提交回复
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    uni.showToast({ title: '请输入回复内容', icon: 'none' })
    return
  }

  if (!currentComment.value || !currentComment.value.id) {
    uni.showToast({ title: '评价信息丢失，请重试', icon: 'none' })
    return
  }

  // 保存当前评价信息，防止关闭弹窗后丢失
  const commentId = currentComment.value.id
  const content = replyContent.value.trim()

  try {
    console.log('正在回复评价:', commentId, content)
    const res = await commentApi.reply(commentId, content)
    console.log('回复结果:', res)
    if (res.code === 200) {
      uni.showToast({ title: '回复成功', icon: 'success' })
      // 更新列表中的回复状态
      const index = comments.value.findIndex(c => c.id === commentId)
      if (index !== -1) {
        comments.value[index].replyContent = content
      }
      closeReplyModal()
      // 刷新统计
      loadStats()
    } else {
      uni.showToast({ title: res.message || '回复失败', icon: 'none' })
    }
  } catch (e) {
    console.error('回复失败', e)
    uni.showToast({ title: '回复失败', icon: 'none' })
  }
}

// 预览图片
const previewImage = (urls, current) => {
  uni.previewImage({
    urls,
    current: urls[current]
  })
}

// 跳转商品详情
const goProductDetail = (productId) => {
  uni.navigateTo({ url: `/pages/product/edit?id=${productId}` })
}
</script>

<style lang="scss" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

/* 统计卡片 */
.stats-card {
  display: flex;
  background-color: #fff;
  padding: 30rpx 20rpx;
  margin-bottom: 20rpx;

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .value {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;

      &.highlight {
        color: #ffb800;
      }
    }

    .label {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }
}

/* 标签页 */
.tabs {
  display: flex;
  background-color: #fff;
  padding: 0 20rpx;

  .tab-item {
    flex: 1;
    height: 80rpx;
    line-height: 80rpx;
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

/* 评价列表 */
.comment-list {
  flex: 1;
  padding: 20rpx;
}

.comment-item {
  background-color: #fff;
  border-radius: $border-radius-lg;
  padding: 24rpx;
  margin-bottom: 20rpx;

  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    image {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      margin-right: 16rpx;
    }

    .user-detail {
      flex: 1;

      .name {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .rating {
        display: flex;
        align-items: center;
        margin-top: 8rpx;

        .time {
          font-size: 24rpx;
          color: #999;
          margin-left: 16rpx;
        }
      }
    }
  }

  .product-info {
    display: flex;
    align-items: center;
    padding: 16rpx;
    background-color: #f8f8f8;
    border-radius: $border-radius-sm;
    margin-bottom: 16rpx;

    image {
      width: 80rpx;
      height: 80rpx;
      border-radius: $border-radius-sm;
      margin-right: 16rpx;
    }

    .name {
      flex: 1;
      font-size: 26rpx;
      color: #666;
    }
  }

  .content {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
    margin-bottom: 16rpx;
  }

  .images {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-bottom: 16rpx;

    image {
      width: 160rpx;
      height: 160rpx;
      border-radius: $border-radius-sm;
    }
  }

  .reply-box {
    background-color: #f0f9ff;
    padding: 16rpx;
    border-radius: $border-radius-sm;
    margin-bottom: 16rpx;

    .reply-label {
      font-size: 26rpx;
      color: $primary-color;
      font-weight: bold;
    }

    .reply-content {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
    }
  }

  .actions {
    display: flex;
    justify-content: flex-end;

    .btn-reply {
      display: flex;
      align-items: center;
      padding: 12rpx 24rpx;
      background-color: #f0f9ff;
      border-radius: 24rpx;

      text {
        font-size: 26rpx;
        color: $primary-color;
        margin-left: 8rpx;
      }
    }
  }
}

/* 回复弹窗 */
.reply-modal {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;
  padding-bottom: calc(30rpx + env(safe-area-inset-bottom));

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .reply-input {
    width: 100%;
    height: 200rpx;
    font-size: 28rpx;
    padding: 20rpx;
    background-color: #f8f8f8;
    border-radius: $border-radius-sm;
    box-sizing: border-box;
  }

  .word-count {
    text-align: right;
    font-size: 24rpx;
    color: #999;
    margin-top: 10rpx;
  }

  .modal-footer {
    display: flex;
    gap: 20rpx;
    margin-top: 30rpx;

    .btn-cancel, .btn-confirm {
      flex: 1;
      height: 80rpx;
      line-height: 80rpx;
      text-align: center;
      font-size: 28rpx;
      border-radius: 40rpx;
    }

    .btn-cancel {
      background-color: #f5f5f5;
      color: #666;
    }

    .btn-confirm {
      background-color: $primary-color;
      color: #fff;
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
