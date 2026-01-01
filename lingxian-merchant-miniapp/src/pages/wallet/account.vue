<template>
  <view class="container">
    <!-- 账户列表 -->
    <view class="account-list">
      <view
        class="account-item"
        v-for="item in accounts"
        :key="item.id"
        @click="selectAccount(item)"
      >
        <view class="icon-wrap" :class="item.type">
          <text v-if="item.type === 'wechat'" class="icon-text">微</text>
          <text v-else-if="item.type === 'alipay'" class="icon-text">支</text>
          <uni-icons v-else type="creditcard" size="24" color="#fff" />
        </view>
        <view class="info">
          <view class="name-row">
            <text class="name">{{ item.name }}</text>
            <text class="default-tag" v-if="item.isDefault">默认</text>
          </view>
          <text class="account">{{ item.accountNo }}</text>
        </view>
        <view class="actions">
          <uni-icons
            type="trash"
            size="20"
            color="#ff4d4f"
            @click.stop="deleteAccount(item)"
          />
        </view>
      </view>

      <view class="empty" v-if="accounts.length === 0">
        <uni-icons type="wallet" size="60" color="#ccc" />
        <text>暂无提现账户</text>
        <text class="tip">添加账户后可提现到对应账户</text>
      </view>
    </view>

    <!-- 添加账户按钮 -->
    <view class="add-section">
      <view class="add-btn" @click="showAddPopup = true">
        <uni-icons type="plusempty" size="20" color="#1890ff" />
        <text>添加提现账户</text>
      </view>
    </view>

    <!-- 添加账户弹窗 -->
    <uni-popup ref="addPopup" type="bottom" :show="showAddPopup" @close="showAddPopup = false">
      <view class="add-popup">
        <view class="popup-header">
          <text>添加提现账户</text>
          <uni-icons type="closeempty" size="20" color="#999" @click="showAddPopup = false" />
        </view>

        <!-- 账户类型选择 -->
        <view class="type-selector">
          <view
            class="type-item"
            :class="{ active: addForm.type === 'wechat' }"
            @click="addForm.type = 'wechat'"
          >
            <view class="type-icon wechat">
              <text class="icon-text">微</text>
            </view>
            <text>微信</text>
          </view>
          <view
            class="type-item"
            :class="{ active: addForm.type === 'alipay' }"
            @click="addForm.type = 'alipay'"
          >
            <view class="type-icon alipay">
              <text class="icon-text">支</text>
            </view>
            <text>支付宝</text>
          </view>
          <view
            class="type-item"
            :class="{ active: addForm.type === 'bank' }"
            @click="addForm.type = 'bank'"
          >
            <view class="type-icon bank">
              <text class="icon-text">银</text>
            </view>
            <text>银行卡</text>
          </view>
        </view>

        <!-- 表单 -->
        <view class="form">
          <view class="form-item">
            <text class="label">真实姓名</text>
            <input type="text" v-model="addForm.realName" placeholder="请输入真实姓名" />
          </view>

          <template v-if="addForm.type === 'wechat'">
            <view class="form-item">
              <text class="label">微信号</text>
              <input type="text" v-model="addForm.accountNo" placeholder="请输入微信号" />
            </view>
          </template>

          <template v-else-if="addForm.type === 'alipay'">
            <view class="form-item">
              <text class="label">支付宝账号</text>
              <input type="text" v-model="addForm.accountNo" placeholder="请输入支付宝账号" />
            </view>
          </template>

          <template v-else>
            <view class="form-item">
              <text class="label">开户银行</text>
              <input type="text" v-model="addForm.bankName" placeholder="请输入开户银行" />
            </view>
            <view class="form-item">
              <text class="label">银行卡号</text>
              <input type="text" v-model="addForm.accountNo" placeholder="请输入银行卡号" />
            </view>
            <view class="form-item">
              <text class="label">开户支行</text>
              <input type="text" v-model="addForm.bankBranch" placeholder="请输入开户支行(选填)" />
            </view>
          </template>

          <view class="form-item switch-item">
            <text class="label">设为默认</text>
            <switch :checked="addForm.isDefault" @change="addForm.isDefault = $event.detail.value" color="#1890ff" />
          </view>
        </view>

        <view class="submit-btn" @click="submitAdd">确认添加</view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { walletApi } from '@/api'

const accounts = ref([])
const showAddPopup = ref(false)

const addForm = reactive({
  type: 'wechat',
  realName: '',
  accountNo: '',
  bankName: '',
  bankBranch: '',
  isDefault: false
})

onLoad(() => {
  uni.setNavigationBarTitle({ title: '提现账户' })
})

onShow(() => {
  loadAccounts()
})

// 加载账户列表
const loadAccounts = async () => {
  try {
    const res = await walletApi.getAccounts()
    if (res.code === 200) {
      accounts.value = res.data || []
    }
  } catch (e) {
    console.error('加载账户失败', e)
  }
}

// 选择账户（设为默认）
const selectAccount = async (item) => {
  if (item.isDefault) return

  try {
    await uni.showModal({
      title: '提示',
      content: '是否将此账户设为默认提现账户？'
    })

    const res = await walletApi.setDefaultAccount(item.id)
    if (res.code === 200) {
      uni.showToast({ title: '设置成功', icon: 'success' })
      loadAccounts()
    }
  } catch (e) {
    // 取消或失败
  }
}

// 删除账户
const deleteAccount = async (item) => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要删除此提现账户吗？'
    })

    const res = await walletApi.removeAccount(item.id)
    if (res.code === 200) {
      uni.showToast({ title: '删除成功', icon: 'success' })
      loadAccounts()
    }
  } catch (e) {
    // 取消或失败
  }
}

// 重置表单
const resetForm = () => {
  addForm.type = 'wechat'
  addForm.realName = ''
  addForm.accountNo = ''
  addForm.bankName = ''
  addForm.bankBranch = ''
  addForm.isDefault = false
}

// 提交添加
const submitAdd = async () => {
  if (!addForm.realName.trim()) {
    uni.showToast({ title: '请输入真实姓名', icon: 'none' })
    return
  }
  if (!addForm.accountNo.trim()) {
    const tip = addForm.type === 'wechat' ? '微信号' : (addForm.type === 'alipay' ? '支付宝账号' : '银行卡号')
    uni.showToast({ title: `请输入${tip}`, icon: 'none' })
    return
  }
  if (addForm.type === 'bank' && !addForm.bankName.trim()) {
    uni.showToast({ title: '请输入开户银行', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '添加中...' })
    const data = {
      type: addForm.type,
      realName: addForm.realName,
      accountNo: addForm.accountNo,
      bankName: addForm.bankName || '',
      bankBranch: addForm.bankBranch || '',
      isDefault: addForm.isDefault
    }

    const res = await walletApi.addAccount(data)
    if (res.code === 200) {
      uni.showToast({ title: '添加成功', icon: 'success' })
      showAddPopup.value = false
      resetForm()
      loadAccounts()
    }
  } catch (e) {
    console.error('添加失败', e)
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

/* 账户列表 */
.account-list {
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.account-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .icon-wrap {
    width: 80rpx;
    height: 80rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;

    &.wechat {
      background-color: #07c160;
    }
    &.alipay {
      background-color: #1677ff;
    }
    &.bank {
      background-color: #1890ff;
    }

    .icon-text {
      font-size: 32rpx;
      font-weight: bold;
      color: #fff;
    }
  }

  .info {
    flex: 1;

    .name-row {
      display: flex;
      align-items: center;
      margin-bottom: 8rpx;

      .name {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
      }

      .default-tag {
        margin-left: 16rpx;
        padding: 4rpx 12rpx;
        background-color: #fff7e6;
        color: #fa8c16;
        font-size: 22rpx;
        border-radius: 4rpx;
      }
    }

    .account {
      font-size: 26rpx;
      color: #999;
    }
  }

  .actions {
    padding: 10rpx;
  }
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;

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

/* 添加按钮 */
.add-section {
  margin-top: 30rpx;
}

.add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx;
  background-color: #fff;
  border-radius: $border-radius-lg;

  text {
    font-size: 28rpx;
    color: #1890ff;
    margin-left: 10rpx;
  }
}

/* 添加弹窗 */
.add-popup {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;
  max-height: 80vh;
  overflow-y: auto;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }
}

/* 类型选择器 */
.type-selector {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30rpx;

  .type-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 40rpx;
    border: 2rpx solid #e8e8e8;
    border-radius: 12rpx;

    &.active {
      border-color: #1890ff;
      background-color: #e6f7ff;
    }

    .type-icon {
      width: 56rpx;
      height: 56rpx;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 10rpx;

      &.wechat { background-color: #07c160; }
      &.alipay { background-color: #1677ff; }
      &.bank { background-color: #1890ff; }

      .icon-text {
        font-size: 28rpx;
        font-weight: bold;
        color: #fff;
      }
    }

    text {
      font-size: 26rpx;
      color: #333;
    }
  }
}

/* 表单 */
.form {
  .form-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    .label {
      width: 160rpx;
      font-size: 28rpx;
      color: #333;
    }

    input {
      flex: 1;
      font-size: 28rpx;
      text-align: right;
    }

    &.switch-item {
      justify-content: space-between;
    }
  }
}

.submit-btn {
  margin-top: 40rpx;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background-color: #1890ff;
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
}
</style>
