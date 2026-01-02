<template>
  <view class="container">
    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: currentType === 'all' }"
        @click="changeType('all')"
      >
        <text>全部</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentType === 'good' }"
        @click="changeType('good')"
      >
        <text>好评</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentType === 'medium' }"
        @click="changeType('medium')"
      >
        <text>中评</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentType === 'bad' }"
        @click="changeType('bad')"
      >
        <text>差评</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: currentType === 'image' }"
        @click="changeType('image')"
      >
        <text>有图</text>
      </view>
    </view>

    <!-- 评价列表 -->
    <view class="comment-list">
      <view class="comment-item" v-for="comment in comments" :key="comment.id">
        <view class="comment-header">
          <image class="user-avatar" :src="comment.userAvatar || '/static/images/default-avatar.png'" mode="aspectFill" />
          <view class="user-info">
            <text class="user-name">{{ comment.userName }}</text>
            <view class="rating-stars">
              <text v-for="i in 5" :key="i" :class="['star', i <= comment.rating ? 'active' : '']">★</text>
            </view>
          </view>
          <text class="comment-time">{{ comment.createTime }}</text>
        </view>
        <text class="comment-content">{{ comment.content }}</text>
        <!-- 评价图片 -->
        <view class="comment-images" v-if="comment.images && comment.images.length > 0">
          <image
            v-for="(img, idx) in comment.images"
            :key="idx"
            :src="img"
            mode="aspectFill"
            @click="previewImage(comment.images, idx)"
          />
        </view>
        <!-- 商家回复 -->
        <view class="merchant-reply" v-if="comment.replyContent">
          <text class="reply-label">商家回复：</text>
          <text class="reply-content">{{ comment.replyContent }}</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty" v-if="!loading && comments.length === 0">
        <uni-icons type="chat" size="64" color="#ccc" />
        <text>暂无评价</text>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="loading">
      <text>加载中...</text>
    </view>
    <view class="load-more" v-else-if="!hasMore && comments.length > 0">
      <text>没有更多了</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { productApi } from '@/api'

const productId = ref('')
const comments = ref([])
const currentType = ref('all')
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

onLoad((options) => {
  productId.value = options.id
  loadComments()
})

// 加载评价列表
const loadComments = async (reset = false) => {
  if (loading.value) return
  if (!reset && !hasMore.value) return

  if (reset) {
    page.value = 1
    comments.value = []
    hasMore.value = true
  }

  loading.value = true
  try {
    const res = await productApi.getComments(productId.value, {
      page: page.value,
      pageSize: pageSize.value,
      type: currentType.value
    })
    if (res.code === 200) {
      const list = res.data?.records || []
      if (reset) {
        comments.value = list
      } else {
        comments.value = [...comments.value, ...list]
      }
      hasMore.value = list.length >= pageSize.value
      page.value++
    }
  } catch (e) {
    console.error('加载评价失败', e)
  } finally {
    loading.value = false
  }
}

// 切换类型
const changeType = (type) => {
  if (currentType.value === type) return
  currentType.value = type
  loadComments(true)
}

// 预览图片
const previewImage = (imgs, index) => {
  uni.previewImage({
    urls: imgs,
    current: index
  })
}

// 触底加载更多
onReachBottom(() => {
  loadComments()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 筛选标签 */
.filter-tabs {
  display: flex;
  padding: 20rpx 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #f0f0f0;

  .tab-item {
    padding: 12rpx 32rpx;
    margin-right: 20rpx;
    font-size: 26rpx;
    color: #666;
    background-color: #f5f5f5;
    border-radius: 30rpx;

    &.active {
      background-color: rgba(34, 197, 94, 0.1);
      color: $primary-color;
    }
  }
}

/* 评价列表 */
.comment-list {
  padding: 20rpx;
}

.comment-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;

  .comment-header {
    display: flex;
    align-items: center;

    .user-avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
    }

    .user-info {
      flex: 1;
      margin-left: 16rpx;

      .user-name {
        font-size: 26rpx;
        color: #333;
      }

      .rating-stars {
        margin-top: 4rpx;

        .star {
          font-size: 22rpx;
          color: #ddd;

          &.active {
            color: #ff9800;
          }
        }
      }
    }

    .comment-time {
      font-size: 22rpx;
      color: #999;
    }
  }

  .comment-content {
    display: block;
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
    margin-top: 16rpx;
  }

  .comment-images {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16rpx;

    image {
      width: 160rpx;
      height: 160rpx;
      border-radius: 8rpx;
      margin-right: 12rpx;
      margin-bottom: 12rpx;
    }
  }

  .merchant-reply {
    margin-top: 16rpx;
    padding: 16rpx;
    background-color: #f5f5f5;
    border-radius: 8rpx;

    .reply-label {
      font-size: 24rpx;
      color: #ff9800;
    }

    .reply-content {
      font-size: 24rpx;
      color: #666;
    }
  }
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 100rpx 0;

  text {
    font-size: 28rpx;
    color: #999;
  }
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 30rpx 0;

  text {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
