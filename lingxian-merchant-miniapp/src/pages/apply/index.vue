<template>
  <view class="container">
    <!-- 顶部标题 -->
    <view class="header">
      <text class="title">商户入驻申请</text>
      <text class="subtitle">请填写真实信息，审核通过后即可开店</text>
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
            <image v-if="previewUrls.logo" :src="previewUrls.logo" mode="aspectFill" />
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

        <view class="form-item">
          <text class="label required">省份</text>
          <input
            v-model="form.province"
            placeholder="请输入省份（如：广东省）"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <text class="label required">城市</text>
          <input
            v-model="form.city"
            placeholder="请输入城市（如：深圳市）"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <text class="label required">区/县</text>
          <input
            v-model="form.district"
            placeholder="请输入区/县（如：南山区）"
            maxlength="20"
          />
        </view>

        <view class="form-item">
          <text class="label required">详细地址</text>
          <input
            v-model="form.address"
            placeholder="请输入详细地址（街道、门牌号等）"
            maxlength="100"
          />
        </view>

        <!-- 可选：使用定位获取 -->
        <view class="location-tip" @click="chooseLocation">
          <uni-icons type="location" size="16" color="#1890ff" />
          <text>使用定位自动填写</text>
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
            <image v-if="previewUrls.licenseImage" :src="previewUrls.licenseImage" mode="aspectFill" />
            <view v-else class="upload-placeholder">
              <uni-icons type="camera" size="40" color="#ccc" />
              <text>上传营业执照</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label required">身份证正面</text>
          <view class="upload-box id-card" @click="uploadIdCardFront">
            <image v-if="previewUrls.idCardFront" :src="previewUrls.idCardFront" mode="aspectFill" />
            <view v-else class="upload-placeholder">
              <uni-icons type="contact" size="40" color="#ccc" />
              <text>上传身份证正面</text>
            </view>
          </view>
        </view>

        <view class="form-item">
          <text class="label required">身份证反面</text>
          <view class="upload-box id-card" @click="uploadIdCardBack">
            <image v-if="previewUrls.idCardBack" :src="previewUrls.idCardBack" mode="aspectFill" />
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
      <view class="agreement" @click="agreed = !agreed">
        <view class="checkbox-wrap">
          <uni-icons
            :type="agreed ? 'checkbox-filled' : 'circle'"
            :color="agreed ? '#1890ff' : '#999'"
            size="22"
          />
        </view>
        <view class="agreement-text">
          <text>我已阅读并同意</text>
          <text class="link" @click.stop="showAgreement">《商户入驻协议》</text>
        </view>
      </view>
      <button class="submit-btn" :class="{ disabled: !agreed }" @click="handleSubmit">
        提交申请
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useMerchantStore } from '@/store/merchant'
import { uploadApi } from '@/api'

const merchantStore = useMerchantStore()

// 上传中状态
const uploading = ref(false)

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

// 图片预览URL（用于显示，存储路径无法直接在浏览器显示）
const previewUrls = ref({
  logo: '',
  licenseImage: '',
  idCardFront: '',
  idCardBack: ''
})

const agreed = ref(false)

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

const onCategoryChange = (e) => {
  form.value.category = categoryOptions[e.detail.value]
}

const showAgreement = () => {
  uni.showModal({
    title: '商户入驻协议',
    content: '商户入驻协议内容...\n\n1. 商户需提供真实有效的营业执照和身份证信息\n2. 商户需遵守平台规则，诚信经营\n3. 商户需保证商品质量，确保食品安全\n4. 平台有权对违规商户进行处罚',
    showCancel: false,
    confirmText: '我知道了'
  })
}

const chooseLocation = () => {
  uni.chooseLocation({
    success: (res) => {
      // 填充详细地址
      if (res.address) {
        form.value.address = res.address
      }
      form.value.longitude = res.longitude
      form.value.latitude = res.latitude
      uni.showToast({ title: '已获取位置，请补充省市区', icon: 'none' })
    },
    fail: (err) => {
      console.log('定位失败', err)
      uni.showToast({ title: '定位不可用，请手动输入地址', icon: 'none' })
    }
  })
}

// 选择并上传图片到服务器
const uploadImage = (fieldName) => {
  if (uploading.value) return

  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]

      try {
        uploading.value = true
        uni.showLoading({ title: '上传中...' })

        // 上传到服务器
        const uploadRes = await uploadApi.uploadImage(tempFilePath)

        // 存储路径保存到表单（用于提交到数据库）
        form.value[fieldName] = uploadRes.url
        // 预览URL用于页面显示
        previewUrls.value[fieldName] = uploadRes.previewUrl

        uni.hideLoading()
        uni.showToast({ title: '上传成功', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        console.error('上传失败:', e)
        uni.showToast({ title: e.message || '上传失败', icon: 'none' })
      } finally {
        uploading.value = false
      }
    }
  })
}

const uploadLogo = () => {
  uploadImage('logo')
}

const uploadLicense = () => {
  uploadImage('licenseImage')
}

const uploadIdCardFront = () => {
  uploadImage('idCardFront')
}

const uploadIdCardBack = () => {
  uploadImage('idCardBack')
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
  if (!form.value.province) {
    uni.showToast({ title: '请输入省份', icon: 'none' })
    return false
  }
  if (!form.value.city) {
    uni.showToast({ title: '请输入城市', icon: 'none' })
    return false
  }
  if (!form.value.district) {
    uni.showToast({ title: '请输入区/县', icon: 'none' })
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
  if (!agreed.value) {
    uni.showToast({ title: '请先同意入驻协议', icon: 'none' })
    return
  }

  if (!validateForm()) {
    return
  }

  try {
    uni.showLoading({ title: '提交中...' })
    await merchantStore.submitApply(form.value)
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
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: calc(200rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(200rpx + env(safe-area-inset-bottom));
}

.header {
  background: linear-gradient(135deg, $primary-color, #36cfc9);
  padding: 60rpx 40rpx;
  color: #fff;

  .title {
    display: block;
    font-size: 44rpx;
    font-weight: bold;
  }

  .subtitle {
    display: block;
    font-size: 26rpx;
    margin-top: 16rpx;
    opacity: 0.9;
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

.location-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx;
  margin-top: -10rpx;

  text {
    font-size: 26rpx;
    color: #1890ff;
    margin-left: 8rpx;
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

  .agreement {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx;
    margin-bottom: 20rpx;
    background-color: #f9f9f9;
    border-radius: 12rpx;

    .checkbox-wrap {
      margin-right: 12rpx;
      padding: 8rpx;
    }

    .agreement-text {
      font-size: 26rpx;
      color: #666;

      .link {
        color: $primary-color;
        font-weight: 500;
      }
    }
  }

  .submit-btn {
    width: 100%;
    height: 96rpx;
    line-height: 96rpx;
    background: linear-gradient(135deg, $primary-color, #36cfc9);
    color: #fff;
    font-size: 32rpx;
    border-radius: 48rpx;
    border: none;

    &.disabled {
      background: #ccc !important;
      color: #999;
    }
  }
}
</style>
