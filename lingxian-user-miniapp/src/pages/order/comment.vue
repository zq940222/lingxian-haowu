<template>
  <view class="container">
    <!-- 订单商品列表 -->
    <view class="products-card">
      <view class="product-item" v-for="(item, index) in orderItems" :key="index">
        <image :src="item.productImage" mode="aspectFill" />
        <view class="info">
          <text class="name">{{ item.productName }}</text>
          <text class="spec">{{ item.skuName }}</text>
        </view>
        <view class="rating-box">
          <text class="label">评分</text>
          <view class="stars">
            <view
              class="star"
              v-for="star in 5"
              :key="star"
              @click="setRating(index, star)"
            >
              <uni-icons
                :type="item.rating >= star ? 'star-filled' : 'star'"
                size="24"
                :color="item.rating >= star ? '#ffb800' : '#ccc'"
              />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 评价内容 -->
    <view class="comment-card">
      <view class="card-title">评价内容</view>
      <textarea
        class="comment-input"
        v-model="content"
        placeholder="请分享您的购物体验，对商品的评价将帮助其他用户..."
        :maxlength="500"
      />
      <view class="word-count">{{ content.length }}/500</view>
    </view>

    <!-- 上传图片 -->
    <view class="image-card">
      <view class="card-title">上传图片（最多9张）</view>
      <view class="image-list">
        <view class="image-item" v-for="(img, index) in images" :key="index">
          <image :src="img" mode="aspectFill" />
          <view class="delete-btn" @click="removeImage(index)">
            <uni-icons type="close" size="14" color="#fff" />
          </view>
        </view>
        <view class="add-image" v-if="images.length < 9" @click="chooseImage">
          <uni-icons type="plusempty" size="40" color="#ccc" />
          <text>添加图片</text>
        </view>
      </view>
    </view>

    <!-- 匿名评价 -->
    <view class="anonymous-card">
      <text>匿名评价</text>
      <switch :checked="isAnonymous" @change="toggleAnonymous" color="#07c160" />
    </view>

    <!-- 提交按钮 -->
    <view class="submit-bar">
      <view class="btn-submit" :class="{ disabled: submitting }" @click="submitComment">
        {{ submitting ? '提交中...' : '提交评价' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi } from '@/api'

const orderId = ref(null)
const orderItems = ref([])
const content = ref('')
const images = ref([])
const isAnonymous = ref(false)
const submitting = ref(false)

onLoad(async (options) => {
  if (options.id) {
    orderId.value = options.id
    await loadOrderDetail()
  } else {
    uni.showToast({ title: '订单不存在', icon: 'none' })
    setTimeout(() => uni.navigateBack(), 1500)
  }
})

// 加载订单详情
const loadOrderDetail = async () => {
  try {
    const res = await orderApi.getDetail(orderId.value)
    if (res.code === 200) {
      const items = res.data.items || res.data.products || []
      // 为每个商品添加评分字段
      orderItems.value = items.map(item => ({
        ...item,
        rating: 5  // 默认5星
      }))
    }
  } catch (e) {
    console.error('加载订单详情失败', e)
  }
}

// 设置评分
const setRating = (index, rating) => {
  orderItems.value[index].rating = rating
}

// 选择图片
const chooseImage = async () => {
  try {
    const res = await uni.chooseImage({
      count: 9 - images.value.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })

    // 上传图片
    for (const tempPath of res.tempFilePaths) {
      await uploadImage(tempPath)
    }
  } catch (e) {
    // 取消选择
  }
}

// 上传图片
const uploadImage = async (tempPath) => {
  try {
    uni.showLoading({ title: '上传中...' })
    const uploadRes = await uni.uploadFile({
      url: '/api/user/upload',
      filePath: tempPath,
      name: 'file'
    })

    const data = JSON.parse(uploadRes.data)
    if (data.code === 200) {
      images.value.push(data.data.url)
    } else {
      uni.showToast({ title: '上传失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '上传失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 删除图片
const removeImage = (index) => {
  images.value.splice(index, 1)
}

// 切换匿名
const toggleAnonymous = (e) => {
  isAnonymous.value = e.detail.value
}

// 提交评价
const submitComment = async () => {
  if (submitting.value) return

  // 检查是否有商品
  if (orderItems.value.length === 0) {
    uni.showToast({ title: '订单商品为空', icon: 'none' })
    return
  }

  // 检查评分
  const hasNoRating = orderItems.value.some(item => !item.rating)
  if (hasNoRating) {
    uni.showToast({ title: '请为所有商品评分', icon: 'none' })
    return
  }

  submitting.value = true

  try {
    const data = {
      orderId: orderId.value,
      content: content.value,
      images: images.value,
      isAnonymous: isAnonymous.value ? 1 : 0,
      items: orderItems.value.map(item => ({
        orderItemId: item.id,
        productId: item.productId,
        rating: item.rating
      }))
    }

    const res = await orderApi.comment(orderId.value, data)
    if (res.code === 200) {
      uni.showToast({ title: '评价成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '评价失败', icon: 'none' })
    }
  } catch (e) {
    console.error('提交评价失败', e)
    uni.showToast({ title: '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 140rpx;
}

.products-card {
  background-color: #fff;
  border-radius: $border-radius-lg;
  padding: 24rpx;
  margin-bottom: 20rpx;

  .product-item {
    display: flex;
    align-items: flex-start;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    image {
      width: 120rpx;
      height: 120rpx;
      border-radius: $border-radius-sm;
      flex-shrink: 0;
    }

    .info {
      flex: 1;
      margin-left: 20rpx;

      .name {
        font-size: 28rpx;
        color: #333;
        display: block;
        line-height: 1.4;
      }

      .spec {
        font-size: 24rpx;
        color: #999;
        margin-top: 8rpx;
        display: block;
      }
    }

    .rating-box {
      display: flex;
      flex-direction: column;
      align-items: flex-end;

      .label {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 8rpx;
      }

      .stars {
        display: flex;

        .star {
          padding: 4rpx;
        }
      }
    }
  }
}

.comment-card {
  background-color: #fff;
  border-radius: $border-radius-lg;
  padding: 24rpx;
  margin-bottom: 20rpx;

  .card-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .comment-input {
    width: 100%;
    height: 200rpx;
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
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
}

.image-card {
  background-color: #fff;
  border-radius: $border-radius-lg;
  padding: 24rpx;
  margin-bottom: 20rpx;

  .card-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .image-item {
      position: relative;
      width: 200rpx;
      height: 200rpx;

      image {
        width: 100%;
        height: 100%;
        border-radius: $border-radius-sm;
      }

      .delete-btn {
        position: absolute;
        top: -12rpx;
        right: -12rpx;
        width: 40rpx;
        height: 40rpx;
        background-color: rgba(0, 0, 0, 0.6);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .add-image {
      width: 200rpx;
      height: 200rpx;
      background-color: #f8f8f8;
      border: 2rpx dashed #ddd;
      border-radius: $border-radius-sm;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      text {
        font-size: 24rpx;
        color: #999;
        margin-top: 10rpx;
      }
    }
  }
}

.anonymous-card {
  background-color: #fff;
  border-radius: $border-radius-lg;
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;

  text {
    font-size: 28rpx;
    color: #333;
  }
}

.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

  .btn-submit {
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    background-color: $primary-color;
    color: #fff;
    font-size: 32rpx;
    font-weight: bold;
    border-radius: 44rpx;

    &.disabled {
      opacity: 0.6;
    }
  }
}
</style>
