<template>
  <view class="container">
    <!-- 顶部提示 -->
    <view class="tip-bar">
      <uni-icons type="info" size="16" color="#1890ff" />
      <text>开启的小区今天可以配送，关闭的小区今天不配送</text>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-btn" @click="openAllCommunities">
        <text>全部开启</text>
      </view>
      <view class="action-btn" @click="closeAllCommunities">
        <text>全部关闭</text>
      </view>
      <view class="action-btn primary" @click="showAddPopup">
        <uni-icons type="plusempty" size="14" color="#1890ff" />
        <text>添加小区</text>
      </view>
    </view>

    <!-- 我的配送小区列表 -->
    <view class="section">
      <view class="section-header">
        <text class="title">我的配送小区</text>
        <text class="count">共 {{ myCommunities.length }} 个</text>
      </view>

      <view v-if="loading" class="loading">
        <uni-icons type="spinner-cycle" size="24" color="#999" />
        <text>加载中...</text>
      </view>

      <view v-else-if="myCommunities.length === 0" class="empty">
        <uni-icons type="map-pin" size="48" color="#ccc" />
        <text>暂无配送小区，请添加</text>
      </view>

      <view v-else class="community-list">
        <view
          v-for="item in myCommunities"
          :key="item.configId"
          class="community-item"
        >
          <view class="community-info">
            <view class="name">{{ item.name }}</view>
            <view class="address">{{ item.fullAddress }}</view>
          </view>
          <view class="community-actions">
            <switch
              :checked="item.enabled"
              @change="(e) => toggleCommunity(item, e.detail.value)"
              color="#1890ff"
            />
            <view class="delete-btn" @click="removeCommunity(item)">
              <uni-icons type="trash" size="20" color="#ff4d4f" />
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 添加小区弹窗 -->
    <uni-popup ref="addPopup" type="bottom">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">选择小区</text>
          <view class="popup-close" @click="closeAddPopup">
            <uni-icons type="closeempty" size="24" color="#666" />
          </view>
        </view>

        <scroll-view scroll-y class="popup-body">
          <view v-if="availableCommunities.length === 0" class="empty-popup">
            <text>暂无可添加的小区</text>
          </view>
          <view
            v-for="item in availableCommunities"
            :key="item.id"
            class="select-item"
            @click="addCommunity(item)"
          >
            <view class="select-info">
              <view class="name">{{ item.name }}</view>
              <view class="address">{{ item.fullAddress }}</view>
            </view>
            <uni-icons type="plusempty" size="20" color="#1890ff" />
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { communityApi } from '@/api'

const loading = ref(false)
const allCommunities = ref([])
const addPopup = ref(null)

// 我的配送小区
const myCommunities = computed(() => {
  return allCommunities.value.filter(c => c.configured)
})

// 可添加的小区
const availableCommunities = computed(() => {
  return allCommunities.value.filter(c => !c.configured)
})

onShow(() => {
  loadCommunities()
})

// 加载小区列表
const loadCommunities = async () => {
  try {
    loading.value = true
    const res = await communityApi.getList()
    if (res.code === 200) {
      allCommunities.value = res.data || []
    }
  } catch (e) {
    console.error('加载小区失败', e)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 切换配送状态
const toggleCommunity = async (item, enabled) => {
  try {
    const res = await communityApi.toggle(item.configId, enabled)
    if (res.code === 200) {
      item.enabled = enabled
      uni.showToast({
        title: enabled ? '已开启配送' : '已关闭配送',
        icon: 'none'
      })
    } else {
      // 恢复原状态
      item.enabled = !enabled
    }
  } catch (e) {
    console.error('切换状态失败', e)
    item.enabled = !enabled
  }
}

// 删除配送小区
const removeCommunity = (item) => {
  uni.showModal({
    title: '确认删除',
    content: `确定删除「${item.name}」吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })
          const result = await communityApi.remove(item.configId)
          if (result.code === 200) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            loadCommunities()
          }
        } catch (e) {
          console.error('删除失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 全部开启
const openAllCommunities = async () => {
  const configured = myCommunities.value
  if (configured.length === 0) {
    uni.showToast({ title: '暂无配送小区', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '处理中...' })
    const configIds = configured.map(c => c.configId)
    const res = await communityApi.batchToggle(configIds, true)
    if (res.code === 200) {
      configured.forEach(c => c.enabled = true)
      uni.showToast({ title: '已全部开启', icon: 'success' })
    }
  } catch (e) {
    console.error('批量开启失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 全部关闭
const closeAllCommunities = async () => {
  const configured = myCommunities.value
  if (configured.length === 0) {
    uni.showToast({ title: '暂无配送小区', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '处理中...' })
    const configIds = configured.map(c => c.configId)
    const res = await communityApi.batchToggle(configIds, false)
    if (res.code === 200) {
      configured.forEach(c => c.enabled = false)
      uni.showToast({ title: '已全部关闭', icon: 'success' })
    }
  } catch (e) {
    console.error('批量关闭失败', e)
    uni.showToast({ title: '操作失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 显示添加弹窗
const showAddPopup = () => {
  addPopup.value.open()
}

// 关闭添加弹窗
const closeAddPopup = () => {
  addPopup.value.close()
}

// 添加配送小区
const addCommunity = async (item) => {
  try {
    uni.showLoading({ title: '添加中...' })
    const res = await communityApi.add(item.id)
    if (res.code === 200) {
      uni.showToast({ title: '添加成功', icon: 'success' })
      closeAddPopup()
      loadCommunities()
    }
  } catch (e) {
    console.error('添加失败', e)
    uni.showToast({ title: '添加失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

.tip-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #e6f7ff;

  text {
    margin-left: 10rpx;
    font-size: 24rpx;
    color: #1890ff;
  }
}

.quick-actions {
  display: flex;
  padding: 20rpx 30rpx;
  gap: 20rpx;

  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 72rpx;
    background-color: #fff;
    border-radius: 36rpx;
    border: 1rpx solid #ddd;

    text {
      font-size: 26rpx;
      color: #666;
      margin-left: 6rpx;
    }

    &.primary {
      border-color: #1890ff;
      background-color: #e6f7ff;

      text {
        color: #1890ff;
      }
    }
  }
}

.section {
  margin: 0 20rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .title {
    font-size: 30rpx;
    font-weight: bold;
    color: #333;
  }

  .count {
    font-size: 24rpx;
    color: #999;
  }
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;

  text {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #999;
  }
}

.community-list {
  padding: 0 30rpx;
}

.community-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.community-info {
  flex: 1;

  .name {
    font-size: 30rpx;
    color: #333;
    margin-bottom: 8rpx;
  }

  .address {
    font-size: 24rpx;
    color: #999;
  }
}

.community-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;

  .delete-btn {
    padding: 10rpx;
  }
}

/* 弹窗样式 */
.popup-content {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 70vh;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .popup-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }

  .popup-close {
    padding: 10rpx;
  }
}

.popup-body {
  max-height: 60vh;
  padding: 0 30rpx;
}

.empty-popup {
  padding: 80rpx 0;
  text-align: center;

  text {
    font-size: 28rpx;
    color: #999;
  }
}

.select-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .select-info {
    flex: 1;

    .name {
      font-size: 30rpx;
      color: #333;
      margin-bottom: 8rpx;
    }

    .address {
      font-size: 24rpx;
      color: #999;
    }
  }
}
</style>
