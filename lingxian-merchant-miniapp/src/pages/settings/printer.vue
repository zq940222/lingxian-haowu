<template>
  <view class="container">
    <view class="form">
      <!-- 打印机列表 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">我的打印机</text>
          <view class="add-btn" @click="addPrinter">
            <uni-icons type="plusempty" size="14" color="#1890ff" />
            <text>添加</text>
          </view>
        </view>
        <view class="printer-list">
          <view class="printer-item" v-for="(printer, index) in printers" :key="index">
            <view class="printer-info">
              <uni-icons type="paperplane" size="28" :color="printer.online ? '#52c41a' : '#999'" />
              <view class="printer-detail">
                <text class="printer-name">{{ printer.name }}</text>
                <text class="printer-sn">SN: {{ printer.sn }}</text>
              </view>
            </view>
            <view class="printer-status">
              <text class="status-tag" :class="{ online: printer.online }">
                {{ printer.online ? '在线' : '离线' }}
              </text>
              <view class="actions">
                <view class="action-btn" @click="testPrint(printer)">测试</view>
                <view class="action-btn danger" @click="deletePrinter(printer)">删除</view>
              </view>
            </view>
          </view>
          <view class="empty" v-if="printers.length === 0">
            <uni-icons type="paperplane" size="60" color="#ddd" />
            <text>暂无打印机</text>
            <text class="tip">点击右上角添加打印机</text>
          </view>
        </view>
      </view>

      <!-- 打印设置 -->
      <view class="form-section">
        <view class="section-title">打印设置</view>
        <view class="form-item switch-item">
          <text class="label">自动打印订单</text>
          <view class="switch-wrap">
            <text class="desc">接单后自动打印</text>
            <switch :checked="settings.autoPrint" @change="onAutoPrintChange" color="#1890ff" />
          </view>
        </view>
        <view class="form-item">
          <text class="label">打印份数</text>
          <view class="stepper">
            <view class="stepper-btn" :class="{ disabled: settings.printCount <= 1 }" @click="decreasePrintCount">
              <text>-</text>
            </view>
            <text class="stepper-value">{{ settings.printCount }}</text>
            <view class="stepper-btn" :class="{ disabled: settings.printCount >= 5 }" @click="increasePrintCount">
              <text>+</text>
            </view>
          </view>
        </view>
        <view class="form-item switch-item">
          <text class="label">打印商品图片</text>
          <switch :checked="settings.printImage" @change="onPrintImageChange" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <text class="label">打印商家联系方式</text>
          <switch :checked="settings.printContact" @change="onPrintContactChange" color="#1890ff" />
        </view>
      </view>

      <!-- 打印内容 -->
      <view class="form-section">
        <view class="section-title">打印内容</view>
        <view class="form-item switch-item">
          <text class="label">订单编号</text>
          <switch :checked="settings.printOrderNo" @change="onPrintOrderNoChange" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <text class="label">下单时间</text>
          <switch :checked="settings.printOrderTime" @change="onPrintOrderTimeChange" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <text class="label">配送地址</text>
          <switch :checked="settings.printAddress" @change="onPrintAddressChange" color="#1890ff" />
        </view>
        <view class="form-item switch-item">
          <text class="label">订单备注</text>
          <switch :checked="settings.printRemark" @change="onPrintRemarkChange" color="#1890ff" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { printerApi } from '@/api'

const printers = ref([])
const settings = ref({
  autoPrint: true,
  printCount: 1,
  printImage: false,
  printContact: true,
  printOrderNo: true,
  printOrderTime: true,
  printAddress: true,
  printRemark: true
})

onLoad(() => {
  loadPrinters()
  loadSettings()
})

// 加载打印机列表
const loadPrinters = async () => {
  try {
    const res = await printerApi.getList()
    if (res.code === 200) {
      printers.value = res.data || []
    }
  } catch (e) {
    console.error('加载打印机列表失败', e)
  }
}

// 加载打印设置
const loadSettings = async () => {
  try {
    const res = await printerApi.getSettings()
    if (res.code === 200 && res.data) {
      settings.value = { ...settings.value, ...res.data }
    }
  } catch (e) {
    console.error('加载打印设置失败', e)
  }
}

// 保存设置
const saveSettings = async () => {
  try {
    await printerApi.updateSettings(settings.value)
  } catch (e) {
    console.error('保存设置失败', e)
  }
}

// 添加打印机
const addPrinter = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

// 测试打印
const testPrint = async (printer) => {
  try {
    uni.showLoading({ title: '打印中...' })
    const res = await printerApi.test(printer.id)
    if (res.code === 200) {
      uni.showToast({ title: '打印成功', icon: 'success' })
    }
  } catch (e) {
    uni.showToast({ title: '打印失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 删除打印机
const deletePrinter = async (printer) => {
  try {
    await uni.showModal({
      title: '提示',
      content: `确定要删除打印机"${printer.name}"吗？`
    })

    const res = await printerApi.delete(printer.id)
    if (res.code === 200) {
      uni.showToast({ title: '删除成功', icon: 'success' })
      loadPrinters()
    }
  } catch (e) {
    // 取消
  }
}

// 自动打印切换
const onAutoPrintChange = (e) => {
  settings.value.autoPrint = e.detail.value
  saveSettings()
}

// 减少打印份数
const decreasePrintCount = () => {
  if (settings.value.printCount > 1) {
    settings.value.printCount--
    saveSettings()
  }
}

// 增加打印份数
const increasePrintCount = () => {
  if (settings.value.printCount < 5) {
    settings.value.printCount++
    saveSettings()
  }
}

// 打印图片切换
const onPrintImageChange = (e) => {
  settings.value.printImage = e.detail.value
  saveSettings()
}

// 打印联系方式切换
const onPrintContactChange = (e) => {
  settings.value.printContact = e.detail.value
  saveSettings()
}

// 打印订单编号切换
const onPrintOrderNoChange = (e) => {
  settings.value.printOrderNo = e.detail.value
  saveSettings()
}

// 打印下单时间切换
const onPrintOrderTimeChange = (e) => {
  settings.value.printOrderTime = e.detail.value
  saveSettings()
}

// 打印配送地址切换
const onPrintAddressChange = (e) => {
  settings.value.printAddress = e.detail.value
  saveSettings()
}

// 打印订单备注切换
const onPrintRemarkChange = (e) => {
  settings.value.printRemark = e.detail.value
  saveSettings()
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.form {
  padding: 20rpx;
}

.form-section {
  background-color: #fff;
  border-radius: $border-radius-lg;
  margin-bottom: 20rpx;
  overflow: hidden;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .section-title {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
    }

    .add-btn {
      display: flex;
      align-items: center;
      font-size: 26rpx;
      color: $primary-color;

      text {
        margin-left: 6rpx;
      }
    }
  }

  .section-title {
    padding: 24rpx 30rpx;
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
    border-bottom: 1rpx solid #f0f0f0;
  }
}

.printer-list {
  .printer-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .printer-info {
      display: flex;
      align-items: center;

      .printer-detail {
        margin-left: 20rpx;

        .printer-name {
          font-size: 28rpx;
          color: #333;
          display: block;
        }

        .printer-sn {
          font-size: 24rpx;
          color: #999;
          margin-top: 6rpx;
          display: block;
        }
      }
    }

    .printer-status {
      display: flex;
      flex-direction: column;
      align-items: flex-end;

      .status-tag {
        font-size: 22rpx;
        color: #999;
        padding: 4rpx 12rpx;
        background-color: #f5f5f5;
        border-radius: 12rpx;

        &.online {
          color: $success-color;
          background-color: rgba(82, 196, 26, 0.1);
        }
      }

      .actions {
        display: flex;
        margin-top: 12rpx;

        .action-btn {
          font-size: 24rpx;
          color: $primary-color;
          padding: 6rpx 16rpx;
          border: 1rpx solid $primary-color;
          border-radius: 16rpx;
          margin-left: 16rpx;

          &.danger {
            color: #ff4d4f;
            border-color: #ff4d4f;
          }
        }
      }
    }
  }

  .empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60rpx 0;

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
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .label {
    font-size: 28rpx;
    color: #333;
  }

  &.switch-item {
    .switch-wrap {
      display: flex;
      align-items: center;

      .desc {
        font-size: 24rpx;
        color: #999;
        margin-right: 16rpx;
      }
    }
  }

  .stepper {
    display: flex;
    align-items: center;

    .stepper-btn {
      width: 56rpx;
      height: 56rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 8rpx;

      text {
        font-size: 32rpx;
        color: #333;
      }

      &.disabled {
        text {
          color: #ccc;
        }
      }
    }

    .stepper-value {
      width: 80rpx;
      text-align: center;
      font-size: 28rpx;
      color: #333;
    }
  }
}
</style>
