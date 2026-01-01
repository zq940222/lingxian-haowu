<template>
  <view class="container">
    <!-- 地址列表 -->
    <view class="address-list">
      <view
        class="address-item"
        v-for="item in addresses"
        :key="item.id"
        @click="handleSelect(item)"
      >
        <view class="address-content">
          <view class="receiver">
            <text class="name">{{ item.name }}</text>
            <text class="phone">{{ item.phone }}</text>
            <view class="tag" v-if="item.isDefault">默认</view>
          </view>
          <text class="address">{{ item.fullAddress }}</text>
        </view>
        <view class="address-actions">
          <view class="action-btn" @click.stop="setDefault(item)" v-if="!item.isDefault">
            <uni-icons type="checkbox" size="18" color="#999" />
            <text>设为默认</text>
          </view>
          <view class="action-btn" @click.stop="editAddress(item)">
            <uni-icons type="compose" size="18" color="#999" />
            <text>编辑</text>
          </view>
          <view class="action-btn" @click.stop="deleteAddress(item)">
            <uni-icons type="trash" size="18" color="#999" />
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty" v-if="addresses.length === 0">
      <uni-icons type="location" size="60" color="#ccc" />
      <text>暂无收货地址</text>
    </view>

    <!-- 添加地址按钮 -->
    <view class="add-btn" @click="goAdd">
      <uni-icons type="plusempty" size="18" color="#fff" />
      <text>新增收货地址</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { addressApi } from '@/api'

const addresses = ref([])
const isSelectMode = ref(false)

onLoad((options) => {
  if (options.select === '1') {
    isSelectMode.value = true
  }
})

onShow(() => {
  loadAddresses()
})

// 加载地址列表
const loadAddresses = async () => {
  try {
    const res = await addressApi.getList()
    if (res.code === 200) {
      addresses.value = res.data || []
    }
  } catch (e) {
    console.error('加载地址失败', e)
  }
}

// 选择地址
const handleSelect = (item) => {
  if (isSelectMode.value) {
    const eventChannel = getOpenerEventChannel()
    eventChannel.emit('selectAddress', item)
    uni.navigateBack()
  } else {
    editAddress(item)
  }
}

// 设为默认
const setDefault = async (item) => {
  try {
    const res = await addressApi.setDefault(item.id)
    if (res.code === 200) {
      uni.showToast({ title: '设置成功', icon: 'success' })
      loadAddresses()
    }
  } catch (e) {
    console.error('设置默认地址失败', e)
  }
}

// 编辑地址
const editAddress = (item) => {
  uni.navigateTo({ url: `/pages/address/edit?id=${item.id}` })
}

// 删除地址
const deleteAddress = async (item) => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要删除该地址吗？'
    })

    const res = await addressApi.remove(item.id)
    if (res.code === 200) {
      uni.showToast({ title: '删除成功', icon: 'success' })
      loadAddresses()
    }
  } catch (e) {
    // 取消
  }
}

// 新增地址
const goAdd = () => {
  uni.navigateTo({ url: '/pages/address/edit' })
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: calc(140rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.address-list {
  .address-item {
    background-color: #fff;
    border-radius: $border-radius-lg;
    margin-bottom: 20rpx;
    overflow: hidden;

    .address-content {
      padding: 24rpx;

      .receiver {
        display: flex;
        align-items: center;
        margin-bottom: 12rpx;

        .name {
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
          margin-right: 20rpx;
        }

        .phone {
          font-size: 28rpx;
          color: #666;
        }

        .tag {
          margin-left: 16rpx;
          padding: 4rpx 12rpx;
          font-size: 22rpx;
          color: $primary-color;
          background-color: rgba(24, 144, 255, 0.1);
          border-radius: 4rpx;
        }
      }

      .address {
        font-size: 28rpx;
        color: #666;
        line-height: 1.5;
      }
    }

    .address-actions {
      display: flex;
      border-top: 1rpx solid #f0f0f0;

      .action-btn {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 20rpx 0;
        border-right: 1rpx solid #f0f0f0;

        &:last-child {
          border-right: none;
        }

        text {
          font-size: 24rpx;
          color: #666;
          margin-left: 8rpx;
        }
      }
    }
  }
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;

  text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }
}

.add-btn {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 96rpx;
  background: linear-gradient(135deg, $primary-color, #36cfc9);
  border-radius: 48rpx;
  margin-bottom: env(safe-area-inset-bottom);

  text {
    font-size: 32rpx;
    color: #fff;
    margin-left: 12rpx;
  }
}
</style>
