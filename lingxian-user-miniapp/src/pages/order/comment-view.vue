<template>
  <view class="container">
    <!-- 评价内容 -->
    <view class="comment-card" v-if="comment">
      <!-- 用户信息 -->
      <view class="user-info">
        <image class="avatar" :src="comment.userAvatar || '/static/images/default-avatar.png'" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ comment.isAnonymous ? '匿名用户' : comment.userName }}</text>
          <text class="time">{{ comment.createTime }}</text>
        </view>
        <view class="rating">
          <uni-icons
            v-for="star in 5"
            :key="star"
            :type="comment.rating >= star ? 'star-filled' : 'star'"
            size="16"
            :color="comment.rating >= star ? '#ffb800' : '#ddd'"
          />
        </view>
      </view>

      <!-- 评价文字 -->
      <view class="content" v-if="comment.content">
        {{ comment.content }}
      </view>
      <view class="content empty" v-else>
        用户未填写评价内容
      </view>

      <!-- 评价图片 -->
      <view class="images" v-if="comment.imageList && comment.imageList.length > 0">
        <image
          v-for="(img, index) in comment.imageList"
          :key="index"
          :src="img"
          mode="aspectFill"
          @click="previewImage(comment.imageList, index)"
        />
      </view>

      <!-- 商品信息 -->
      <view class="product-info" @click="goProduct">
        <image :src="comment.productImage" mode="aspectFill" />
        <text class="name">{{ comment.productName }}</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>

      <!-- 商家回复 -->
      <view class="reply-box" v-if="comment.replyContent">
        <view class="reply-header">
          <text class="label">商家回复</text>
          <text class="time">{{ comment.replyTime }}</text>
        </view>
        <text class="reply-content">{{ comment.replyContent }}</text>
      </view>
    </view>

    <!-- 加载中 -->
    <view class="loading" v-if="loading">
      <text>加载中...</text>
    </view>

    <!-- 无评价 -->
    <view class="empty" v-if="!loading && !comment">
      <view class="empty-icon">
        <uni-icons type="chat" size="80" color="#ccc" />
      </view>
      <text>暂无评价</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { commentApi } from '@/api'

const orderId = ref(null)
const comment = ref(null)
const loading = ref(true)

onLoad(async (options) => {
  if (options.orderId) {
    orderId.value = options.orderId
    await loadComment()
  } else {
    uni.showToast({ title: '参数错误', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
  }
})

// 加载评价
const loadComment = async () => {
  loading.value = true
  try {
    const res = await commentApi.getOrderComment(orderId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      comment.value = {
        ...data,
        imageList: data.images ? data.images.split(',').filter(img => img) : []
      }
    }
  } catch (e) {
    console.error('加载评价失败', e)
  } finally {
    loading.value = false
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
const goProduct = () => {
  if (comment.value && comment.value.productId) {
    uni.navigateTo({ url: `/pages/product/detail?id=${comment.value.productId}` })
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.comment-card {
  background-color: #fff;
  border-radius: $border-radius-lg;
  padding: 24rpx;

  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
    }

    .info {
      flex: 1;
      margin-left: 16rpx;

      .name {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .time {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
        display: block;
      }
    }

    .rating {
      display: flex;
    }
  }

  .content {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
    margin-bottom: 20rpx;

    &.empty {
      color: #999;
      font-style: italic;
    }
  }

  .images {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-bottom: 20rpx;

    image {
      width: 200rpx;
      height: 200rpx;
      border-radius: $border-radius-sm;
    }
  }

  .product-info {
    display: flex;
    align-items: center;
    padding: 16rpx;
    background-color: #f8f8f8;
    border-radius: $border-radius-sm;
    margin-bottom: 20rpx;

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

  .reply-box {
    background-color: #f0f9ff;
    padding: 20rpx;
    border-radius: $border-radius-sm;
    border-left: 4rpx solid $primary-color;

    .reply-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;

      .label {
        font-size: 26rpx;
        color: $primary-color;
        font-weight: bold;
      }

      .time {
        font-size: 22rpx;
        color: #999;
      }
    }

    .reply-content {
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
    }
  }
}

.loading {
  text-align: center;
  padding: 100rpx 0;
  font-size: 28rpx;
  color: #999;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  .empty-icon {
    margin-bottom: 20rpx;
  }

  text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
