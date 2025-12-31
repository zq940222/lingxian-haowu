<template>
  <view class="container">
    <!-- 添加分类 -->
    <view class="add-bar" @click="showAddModal">
      <uni-icons type="plusempty" size="20" color="#1890ff" />
      <text>添加分类</text>
    </view>

    <!-- 分类列表 -->
    <view class="category-list">
      <view class="category-item" v-for="item in categories" :key="item.id">
        <view class="drag-handle">
          <uni-icons type="bars" size="20" color="#ccc" />
        </view>
        <view class="info">
          <text class="name">{{ item.name }}</text>
          <text class="count">{{ item.productCount || 0 }}件商品</text>
        </view>
        <view class="actions">
          <view class="btn" @click="editCategory(item)">
            <uni-icons type="compose" size="20" color="#666" />
          </view>
          <view class="btn" @click="deleteCategory(item)">
            <uni-icons type="trash" size="20" color="#ff4d4f" />
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty" v-if="categories.length === 0">
      <uni-icons type="folder-add" size="60" color="#ccc" />
      <text>暂无分类</text>
      <text class="tip">点击上方添加分类</text>
    </view>

    <!-- 添加/编辑弹窗 -->
    <uni-popup ref="popup" type="center">
      <view class="popup-content">
        <view class="popup-header">
          <text>{{ isEdit ? '编辑分类' : '添加分类' }}</text>
        </view>
        <view class="popup-body">
          <input
            type="text"
            v-model="categoryName"
            placeholder="请输入分类名称"
            maxlength="20"
          />
        </view>
        <view class="popup-footer">
          <view class="btn cancel" @click="closePopup">取消</view>
          <view class="btn confirm" @click="saveCategory">确定</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { categoryApi } from '@/api'

const categories = ref([])
const popup = ref(null)
const categoryName = ref('')
const editId = ref('')
const isEdit = ref(false)

onShow(() => {
  loadCategories()
})

// 加载分类
const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 显示添加弹窗
const showAddModal = () => {
  isEdit.value = false
  editId.value = ''
  categoryName.value = ''
  popup.value.open()
}

// 编辑分类
const editCategory = (item) => {
  isEdit.value = true
  editId.value = item.id
  categoryName.value = item.name
  popup.value.open()
}

// 关闭弹窗
const closePopup = () => {
  popup.value.close()
}

// 保存分类
const saveCategory = async () => {
  if (!categoryName.value.trim()) {
    uni.showToast({ title: '请输入分类名称', icon: 'none' })
    return
  }

  try {
    let res
    if (isEdit.value) {
      res = await categoryApi.update(editId.value, { name: categoryName.value })
    } else {
      res = await categoryApi.add({ name: categoryName.value })
    }

    if (res.code === 200) {
      uni.showToast({ title: '保存成功', icon: 'success' })
      closePopup()
      loadCategories()
    }
  } catch (e) {
    console.error('保存分类失败', e)
  }
}

// 删除分类
const deleteCategory = async (item) => {
  try {
    await uni.showModal({
      title: '提示',
      content: `确定删除分类"${item.name}"吗？`
    })

    const res = await categoryApi.remove(item.id)
    if (res.code === 200) {
      uni.showToast({ title: '删除成功', icon: 'success' })
      loadCategories()
    }
  } catch (e) {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 添加栏 */
.add-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100rpx;
  background-color: #fff;
  margin: 20rpx;
  border-radius: $border-radius-lg;
  border: 2rpx dashed #1890ff;

  text {
    font-size: 28rpx;
    color: #1890ff;
    margin-left: 10rpx;
  }
}

/* 分类列表 */
.category-list {
  margin: 0 20rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .drag-handle {
    padding: 0 16rpx;
  }

  .info {
    flex: 1;
    margin-left: 16rpx;

    .name {
      font-size: 30rpx;
      color: #333;
      display: block;
    }

    .count {
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
      display: block;
    }
  }

  .actions {
    display: flex;

    .btn {
      width: 64rpx;
      height: 64rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

/* 空状态 */
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

  .tip {
    font-size: 24rpx;
    color: #ccc;
    margin-top: 10rpx;
  }
}

/* 弹窗 */
.popup-content {
  width: 560rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;

  .popup-header {
    padding: 30rpx;
    text-align: center;
    border-bottom: 1rpx solid #f0f0f0;

    text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .popup-body {
    padding: 40rpx 30rpx;

    input {
      width: 100%;
      height: 88rpx;
      padding: 0 24rpx;
      font-size: 28rpx;
      background-color: #f5f5f5;
      border-radius: $border-radius-sm;
    }
  }

  .popup-footer {
    display: flex;
    border-top: 1rpx solid #f0f0f0;

    .btn {
      flex: 1;
      height: 100rpx;
      line-height: 100rpx;
      text-align: center;
      font-size: 30rpx;

      &.cancel {
        color: #666;
        border-right: 1rpx solid #f0f0f0;
      }

      &.confirm {
        color: $primary-color;
      }
    }
  }
}
</style>
