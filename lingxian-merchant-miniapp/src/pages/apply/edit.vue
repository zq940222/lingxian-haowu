<template>
  <view class="container">
    <!-- 顶部提示 -->
    <view class="header-tip">
      <uni-icons type="info" size="18" color="#faad14" />
      <text>您的申请被拒绝，请修改后重新提交</text>
    </view>

    <!-- 拒绝原因 -->
    <view v-if="rejectRemark" class="reject-box">
      <text class="label">拒绝原因:</text>
      <text class="content">{{ rejectRemark }}</text>
    </view>

    <!-- 表单 -->
    <view class="form-section">
      <!-- 基本信息 -->
      <view class="section">
        <view class="section-title">基本信息</view>

        <view class="form-item">
          <text class="label required">店铺名称</text>
          <input
            v-model="form.shopName"
            placeholder="请输入店铺名称"
            maxlength="30"
          />
        </view>

        <view class="form-item">
          <text class="label">店铺Logo</text>
          <view class="upload-box" @click="uploadLogo">
            <image v-if="form.logo" :src="form.logo" mode="aspectFill" />
            <view v-else class="upload-placeholder">
              <uni-icons type="plusempty" size="40" color="#ccc" />
              <text>上传Logo</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label">店铺简介</text>
          <textarea
            v-model="form.description"
            placeholder="请输入店铺简介"
            maxlength="200"
          />
        </view>

        <view class="form-item">
          <text class="label required">经营类目</text>
          <picker :range="categoryOptions" @change="onCategoryChange">
            <view class="picker">
              <text :class="{ placeholder: !form.category }">
                {{ form.category || '请选择经营类目' }}
              </text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>
      </view>

      <!-- 联系信息 -->
      <view class="section">
        <view class="section-title">联系信息</view>

        <view class="form-item">
          <text class="label required">联系人</text>
          <input
            v-model="form.contactName"
            placeholder="请输入联系人姓名"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <text class="label required">联系电话</text>
          <input
            v-model="form.contactPhone"
            type="number"
            placeholder="请输入联系电话"
            maxlength="11"
          />
        </view>
      </view>

      <!-- 地址信息 -->
      <view class="section">
        <view class="section-title">店铺地址</view>

        <view class="form-item" @click="chooseLocation">
          <text class="label required">选择位置</text>
          <view class="picker">
            <text :class="{ placeholder: !fullAddress }">
              {{ fullAddress || '请选择店铺位置' }}
            </text>
            <uni-icons type="location" size="16" color="#999" />
          </view>
        </view>

        <view class="form-item">
          <text class="label required">详细地址</text>
          <input
            v-model="form.address"
            placeholder="请输入详细地址（门牌号等）"
            maxlength="100"
          />
        </view>
      </view>

      <!-- 资质信息 -->
      <view class="section">
        <view class="section-title">资质信息</view>

        <view class="form-item">
          <text class="label required">营业执照号</text>
          <input
            v-model="form.businessLicense"
            placeholder="请输入营业执照号"
            maxlength="30"
          />
        </view>

        <view class="form-item">
          <text class="label required">营业执照照片</text>
          <view class="upload-box license" @click="uploadLicense">
            <image v-if="form.licenseImage" :src="form.licenseImage" mode="aspectFill" />
            <view v-else class="upload-placeholder">
              <uni-icons type="camera" size="40" color="#ccc" />
              <text>上传营业执照</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label required">身份证正面</text>
          <view class="upload-box id-card" @click="uploadIdCardFront">
            <image v-if="form.idCardFront" :src="form.idCardFront" mode="aspectFill" />
            <view v-else class="upload-placeholder">
              <uni-icons type="contact" size="40" color="#ccc" />
              <text>上传身份证正面</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label required">身份证反面</text>
          <view class="upload-box id-card" @click="uploadIdCardBack">
            <image v-if="form.idCardBack" :src="form.idCardBack" mode="aspectFill" />
            <view v-else class="upload-placeholder">
              <uni-icons type="contact" size="40" color="#ccc" />
              <text>上传身份证反面</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="submit-btn" @click="handleSubmit">
        重新提交申请
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMerchantStore } from '@/store/merchant'

const merchantStore = useMerchantStore()

const rejectRemark = ref('')

const form = ref({
  shopName: '',
  logo: '',
  description: '',
  category: '',
  contactName: '',
  contactPhone: '',
  province: '',
  city: '',
  district: '',
  address: '',
  longitude: null,
  latitude: null,
  businessLicense: '',
  licenseImage: '',
  idCardFront: '',
  idCardBack: ''
})

const categoryOptions = [
  '生鲜蔬菜',
  '水果',
  '肉禽蛋奶',
  '海鲜水产',
  '粮油调味',
  '零食饮料',
  '日用百货',
  '其他'
]

const fullAddress = computed(() => {
  if (form.value.province) {
    return `${form.value.province}${form.value.city}${form.value.district}`
  }
  return ''
})

const onCategoryChange = (e) => {
  form.value.category = categoryOptions[e.detail.value]
}

const chooseLocation = () => {
  uni.chooseLocation({
    success: (res) => {
      form.value.address = res.address
      form.value.longitude = res.longitude
      form.value.latitude = res.latitude
    },
    fail: () => {
      uni.showToast({ title: '请授权位置权限', icon: 'none' })
    }
  })
}

const uploadImage = (callback) => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      callback(tempFilePath)
    }
  })
}

const uploadLogo = () => {
  uploadImage((path) => {
    form.value.logo = path
  })
}

const uploadLicense = () => {
  uploadImage((path) => {
    form.value.licenseImage = path
  })
}

const uploadIdCardFront = () => {
  uploadImage((path) => {
    form.value.idCardFront = path
  })
}

const uploadIdCardBack = () => {
  uploadImage((path) => {
    form.value.idCardBack = path
  })
}

const validateForm = () => {
  if (!form.value.shopName) {
    uni.showToast({ title: '请输入店铺名称', icon: 'none' })
    return false
  }
  if (!form.value.category) {
    uni.showToast({ title: '请选择经营类目', icon: 'none' })
    return false
  }
  if (!form.value.contactName) {
    uni.showToast({ title: '请输入联系人', icon: 'none' })
    return false
  }
  if (!form.value.contactPhone || form.value.contactPhone.length !== 11) {
    uni.showToast({ title: '请输入正确的联系电话', icon: 'none' })
    return false
  }
  if (!form.value.address) {
    uni.showToast({ title: '请输入详细地址', icon: 'none' })
    return false
  }
  if (!form.value.businessLicense) {
    uni.showToast({ title: '请输入营业执照号', icon: 'none' })
    return false
  }
  if (!form.value.licenseImage) {
    uni.showToast({ title: '请上传营业执照照片', icon: 'none' })
    return false
  }
  if (!form.value.idCardFront) {
    uni.showToast({ title: '请上传身份证正面', icon: 'none' })
    return false
  }
  if (!form.value.idCardBack) {
    uni.showToast({ title: '请上传身份证反面', icon: 'none' })
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  try {
    uni.showLoading({ title: '提交中...' })
    await merchantStore.updateApply(form.value)
    uni.hideLoading()
    uni.showToast({ title: '提交成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/apply/status' })
    }, 1500)
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  }
}

// 加载原有数据
const loadMerchantInfo = async () => {
  try {
    const data = await merchantStore.fetchApplyStatus()
    if (data.merchantInfo) {
      const info = data.merchantInfo
      form.value = {
        shopName: info.shopName || '',
        logo: info.logo || '',
        description: info.description || '',
        category: info.category || '',
        contactName: info.contactName || '',
        contactPhone: info.contactPhone || '',
        province: info.province || '',
        city: info.city || '',
        district: info.district || '',
        address: info.address || '',
        longitude: info.longitude,
        latitude: info.latitude,
        businessLicense: info.businessLicense || '',
        licenseImage: info.licenseImage || '',
        idCardFront: info.idCardFront || '',
        idCardBack: info.idCardBack || ''
      }
      rejectRemark.value = data.verifyRemark || ''
    }
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

onMounted(() => {
  if (!merchantStore.checkLoginStatus()) {
    return
  }
  loadMerchantInfo()
})
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: calc(150rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(150rpx + env(safe-area-inset-bottom));
}

.header-tip {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
  background-color: #fffbe6;
  border-bottom: 1rpx solid #ffe58f;

  text {
    font-size: 26rpx;
    color: #d48806;
    margin-left: 12rpx;
  }
}

.reject-box {
  margin: 20rpx;
  padding: 24rpx;
  background-color: #fff2f0;
  border-radius: 12rpx;
  border: 1rpx solid #ffccc7;

  .label {
    font-size: 26rpx;
    color: #ff4d4f;
    font-weight: bold;
  }

  .content {
    display: block;
    font-size: 26rpx;
    color: #666;
    margin-top: 12rpx;
    line-height: 1.6;
  }
}

.form-section {
  padding: 20rpx;
}

.section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
    padding-left: 20rpx;
    border-left: 6rpx solid $primary-color;
  }
}

.form-item {
  margin-bottom: 30rpx;

  .label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 16rpx;

    &.required::before {
      content: '*';
      color: #ff4d4f;
      margin-right: 8rpx;
    }
  }

  input {
    width: 100%;
    height: 88rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    box-sizing: border-box;
  }

  textarea {
    width: 100%;
    height: 200rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    padding: 24rpx;
    font-size: 28rpx;
    box-sizing: border-box;
  }

  .picker {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;

    .placeholder {
      color: #999;
    }
  }
}

.upload-box {
  width: 200rpx;
  height: 200rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  overflow: hidden;
  border: 2rpx dashed #ddd;

  image {
    width: 100%;
    height: 100%;
  }

  &.license, &.id-card {
    width: 100%;
    height: 300rpx;
  }

  .upload-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    text {
      font-size: 24rpx;
      color: #999;
      margin-top: 16rpx;
    }
  }
}

.submit-section {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #fff;
  padding: 30rpx 40rpx;
  padding-bottom: calc(30rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);

  .submit-btn {
    width: 100%;
    height: 96rpx;
    line-height: 96rpx;
    background: linear-gradient(135deg, $primary-color, #36cfc9);
    color: #fff;
    font-size: 32rpx;
    border-radius: 48rpx;
    border: none;
  }
}
</style>
