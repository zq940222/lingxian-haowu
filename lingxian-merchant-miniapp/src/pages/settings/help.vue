<template>
  <view class="container">
    <!-- 搜索框 -->
    <view class="search-bar">
      <uni-icons type="search" size="18" color="#999" />
      <input type="text" v-model="searchKey" placeholder="搜索问题" @confirm="search" />
    </view>

    <!-- 常见问题分类 -->
    <view class="category-list">
      <view
        class="category-item"
        v-for="(category, index) in categories"
        :key="index"
        @click="goCategory(category)"
      >
        <view class="icon-wrap" :style="{ backgroundColor: category.color }">
          <uni-icons :type="category.icon" size="24" color="#fff" />
        </view>
        <text class="name">{{ category.name }}</text>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <!-- 热门问题 -->
    <view class="hot-section">
      <view class="section-title">热门问题</view>
      <view class="faq-list">
        <view
          class="faq-item"
          v-for="(faq, index) in hotFaqs"
          :key="index"
          @click="goDetail(faq)"
        >
          <view class="faq-header">
            <text class="question">{{ faq.question }}</text>
            <uni-icons :type="faq.expanded ? 'up' : 'down'" size="16" color="#999" />
          </view>
          <view class="faq-content" v-if="faq.expanded">
            <text>{{ faq.answer }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 联系客服 -->
    <view class="contact-section">
      <view class="section-title">联系我们</view>
      <view class="contact-list">
        <view class="contact-item" @click="callService">
          <uni-icons type="phone" size="24" color="#1890ff" />
          <view class="contact-info">
            <text class="label">客服热线</text>
            <text class="value">400-123-4567</text>
          </view>
          <view class="contact-btn">
            <text>拨打</text>
          </view>
        </view>
        <view class="contact-item" @click="openChat">
          <uni-icons type="chat" size="24" color="#52c41a" />
          <view class="contact-info">
            <text class="label">在线客服</text>
            <text class="value">工作时间 9:00-22:00</text>
          </view>
          <view class="contact-btn">
            <text>咨询</text>
          </view>
        </view>
        <view class="contact-item" @click="feedback">
          <uni-icons type="email" size="24" color="#fa8c16" />
          <view class="contact-info">
            <text class="label">意见反馈</text>
            <text class="value">我们会认真对待每一条建议</text>
          </view>
          <view class="contact-btn">
            <text>反馈</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const searchKey = ref('')

const categories = ref([
  { name: '订单问题', icon: 'list', color: '#1890ff' },
  { name: '配送问题', icon: 'car', color: '#52c41a' },
  { name: '商品管理', icon: 'shop', color: '#fa8c16' },
  { name: '账户问题', icon: 'person', color: '#722ed1' },
  { name: '支付结算', icon: 'wallet', color: '#eb2f96' },
  { name: '其他问题', icon: 'help', color: '#8c8c8c' }
])

const hotFaqs = ref([
  {
    id: 1,
    question: '如何接单？',
    answer: '在订单列表中，点击待接单订单，然后点击"接单"按钮即可完成接单。您也可以在设置中开启自动接单功能。',
    expanded: false
  },
  {
    id: 2,
    question: '如何设置配送范围？',
    answer: '进入"设置-配送设置"，可以设置配送范围、配送费、起送金额等配送相关参数。',
    expanded: false
  },
  {
    id: 3,
    question: '如何添加商品？',
    answer: '在"商品管理"页面，点击右上角的"添加"按钮，填写商品信息后保存即可上架商品。',
    expanded: false
  },
  {
    id: 4,
    question: '如何查看收入明细？',
    answer: '在"数据统计"页面，可以查看每日、每周、每月的订单数据和收入明细。',
    expanded: false
  },
  {
    id: 5,
    question: '如何联系用户？',
    answer: '在订单详情页面，点击用户电话旁边的"电话"图标，即可拨打用户电话。',
    expanded: false
  }
])

onLoad(() => {
  // 可以从服务器加载FAQ数据
})

// 搜索
const search = () => {
  if (!searchKey.value.trim()) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  uni.showToast({ title: '搜索功能开发中', icon: 'none' })
}

// 跳转分类
const goCategory = (category) => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 展开/收起问题
const goDetail = (faq) => {
  faq.expanded = !faq.expanded
}

// 拨打客服电话
const callService = () => {
  uni.makePhoneCall({
    phoneNumber: '4001234567'
  })
}

// 打开在线客服
const openChat = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 意见反馈
const feedback = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

/* 搜索框 */
.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;

  input {
    flex: 1;
    margin-left: 16rpx;
    font-size: 28rpx;
  }
}

/* 分类列表 */
.category-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 20rpx;

  .category-item {
    display: flex;
    align-items: center;
    padding: 24rpx;
    background-color: #fff;
    border-radius: $border-radius-lg;

    .icon-wrap {
      width: 64rpx;
      height: 64rpx;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16rpx;
    }

    .name {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }
  }
}

/* 热门问题 */
.hot-section {
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
  overflow: hidden;

  .section-title {
    padding: 24rpx 30rpx;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    border-bottom: 1rpx solid #f0f0f0;
  }
}

.faq-list {
  .faq-item {
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .faq-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 28rpx 30rpx;

      .question {
        flex: 1;
        font-size: 28rpx;
        color: #333;
      }
    }

    .faq-content {
      padding: 0 30rpx 28rpx;
      background-color: #fafafa;

      text {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
      }
    }
  }
}

/* 联系我们 */
.contact-section {
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .section-title {
    padding: 24rpx 30rpx;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    border-bottom: 1rpx solid #f0f0f0;
  }
}

.contact-list {
  .contact-item {
    display: flex;
    align-items: center;
    padding: 28rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .contact-info {
      flex: 1;
      margin-left: 20rpx;

      .label {
        font-size: 28rpx;
        color: #333;
        display: block;
      }

      .value {
        font-size: 24rpx;
        color: #999;
        margin-top: 4rpx;
        display: block;
      }
    }

    .contact-btn {
      padding: 12rpx 32rpx;
      background-color: $primary-color;
      border-radius: 32rpx;

      text {
        font-size: 26rpx;
        color: #fff;
      }
    }
  }
}
</style>
