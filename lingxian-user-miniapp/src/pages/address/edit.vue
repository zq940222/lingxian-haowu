<template>
  <view class="container">
    <view class="form">
      <!-- 联系人 -->
      <view class="form-item">
        <text class="label required">收货人</text>
        <input type="text" v-model="form.name" placeholder="请输入收货人姓名" />
      </view>

      <!-- 手机号 -->
      <view class="form-item">
        <text class="label required">手机号</text>
        <input type="tel" v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
      </view>

      <!-- 所在地区 -->
      <view class="form-item" @click="showRegionPicker = true">
        <text class="label required">所在地区</text>
        <view class="picker">
          <text v-if="form.region">{{ form.region }}</text>
          <text v-else class="placeholder">请选择省市区</text>
          <uni-icons type="right" size="16" color="#999" />
        </view>
      </view>

      <!-- 详细地址 -->
      <view class="form-item textarea-item">
        <text class="label required">详细地址</text>
        <textarea v-model="form.detail" placeholder="请输入详细地址，如街道、门牌号等" />
      </view>

      <!-- 定位 -->
      <view class="form-item location-item" @click="chooseLocation">
        <uni-icons type="location" size="20" color="#1890ff" />
        <text v-if="form.latitude">已定位</text>
        <text v-else class="placeholder">点击获取精确位置</text>
      </view>

      <!-- 设为默认 -->
      <view class="form-item switch-item">
        <text class="label">设为默认地址</text>
        <switch :checked="form.isDefault" @change="onDefaultChange" color="#1890ff" />
      </view>
    </view>

    <!-- 删除按钮 -->
    <view class="delete-btn" v-if="isEdit" @click="handleDelete">
      删除地址
    </view>

    <!-- 保存按钮 -->
    <view class="save-btn" @click="save">
      保存
    </view>

    <!-- 地区选择器 -->
    <uni-popup ref="regionPopup" type="bottom" v-if="showRegionPicker">
      <view class="region-picker">
        <view class="picker-header">
          <text @click="showRegionPicker = false">取消</text>
          <text class="title">选择地区</text>
          <text class="confirm" @click="confirmRegion">确定</text>
        </view>
        <picker-view
          class="picker-view"
          :value="regionIndexes"
          @change="onRegionChange"
        >
          <picker-view-column>
            <view class="picker-item" v-for="item in provinces" :key="item.code">
              {{ item.name }}
            </view>
          </picker-view-column>
          <picker-view-column>
            <view class="picker-item" v-for="item in cities" :key="item.code">
              {{ item.name }}
            </view>
          </picker-view-column>
          <picker-view-column>
            <view class="picker-item" v-for="item in districts" :key="item.code">
              {{ item.name }}
            </view>
          </picker-view-column>
        </picker-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { addressApi } from '@/api'
import regionData from '@/utils/region'

const addressId = ref('')
const isEdit = computed(() => !!addressId.value)

const form = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  region: '',
  detail: '',
  latitude: 0,
  longitude: 0,
  isDefault: false
})

const showRegionPicker = ref(false)
const regionIndexes = ref([0, 0, 0])

// 省市区数据
const provinces = ref(regionData.provinces || [])
const cities = computed(() => {
  const province = provinces.value[regionIndexes.value[0]]
  return province ? province.cities || [] : []
})
const districts = computed(() => {
  const city = cities.value[regionIndexes.value[1]]
  return city ? city.districts || [] : []
})

onLoad((options) => {
  if (options.id) {
    addressId.value = options.id
    uni.setNavigationBarTitle({ title: '编辑地址' })
    loadAddress()
  } else {
    uni.setNavigationBarTitle({ title: '新增地址' })
  }
})

// 加载地址详情
const loadAddress = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await addressApi.getDetail(addressId.value)
    if (res.code === 200) {
      const data = res.data
      form.value = {
        name: data.name,
        phone: data.phone,
        province: data.province,
        city: data.city,
        district: data.district,
        region: `${data.province} ${data.city} ${data.district}`,
        detail: data.detail,
        latitude: data.latitude || 0,
        longitude: data.longitude || 0,
        isDefault: data.isDefault
      }
    }
  } catch (e) {
    console.error('加载地址失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 地区选择变化
const onRegionChange = (e) => {
  const values = e.detail.value
  // 切换省份时重置市和区
  if (values[0] !== regionIndexes.value[0]) {
    values[1] = 0
    values[2] = 0
  }
  // 切换市时重置区
  if (values[1] !== regionIndexes.value[1]) {
    values[2] = 0
  }
  regionIndexes.value = values
}

// 确认地区选择
const confirmRegion = () => {
  const province = provinces.value[regionIndexes.value[0]]
  const city = cities.value[regionIndexes.value[1]]
  const district = districts.value[regionIndexes.value[2]]

  if (province && city && district) {
    form.value.province = province.name
    form.value.city = city.name
    form.value.district = district.name
    form.value.region = `${province.name} ${city.name} ${district.name}`
  }

  showRegionPicker.value = false
}

// 选择位置
const chooseLocation = async () => {
  try {
    const res = await uni.chooseLocation()
    form.value.latitude = res.latitude
    form.value.longitude = res.longitude
    // 如果详细地址为空，填入选择的地址
    if (!form.value.detail && res.address) {
      form.value.detail = res.address
    }
  } catch (e) {
    // 取消
  }
}

// 默认地址变更
const onDefaultChange = (e) => {
  form.value.isDefault = e.detail.value
}

// 验证表单
const validate = () => {
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
    return false
  }
  if (!form.value.phone || !/^1\d{10}$/.test(form.value.phone)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }
  if (!form.value.region) {
    uni.showToast({ title: '请选择所在地区', icon: 'none' })
    return false
  }
  if (!form.value.detail.trim()) {
    uni.showToast({ title: '请输入详细地址', icon: 'none' })
    return false
  }
  return true
}

// 保存
const save = async () => {
  if (!validate()) return

  try {
    uni.showLoading({ title: '保存中...' })

    const data = {
      name: form.value.name,
      phone: form.value.phone,
      province: form.value.province,
      city: form.value.city,
      district: form.value.district,
      detail: form.value.detail,
      latitude: form.value.latitude,
      longitude: form.value.longitude,
      isDefault: form.value.isDefault
    }

    let res
    if (isEdit.value) {
      res = await addressApi.update(addressId.value, data)
    } else {
      res = await addressApi.add(data)
    }

    if (res.code === 200) {
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (e) {
    console.error('保存失败', e)
    uni.showToast({ title: '保存失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 删除
const handleDelete = async () => {
  try {
    await uni.showModal({
      title: '提示',
      content: '确定要删除该地址吗？'
    })

    uni.showLoading({ title: '删除中...' })
    const res = await addressApi.remove(addressId.value)
    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({ title: '删除成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (e) {
    uni.hideLoading()
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 200rpx;
}

.form {
  background-color: #fff;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 28rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .label {
    width: 160rpx;
    font-size: 28rpx;
    color: #333;

    &.required::before {
      content: '*';
      color: $danger-color;
      margin-right: 8rpx;
    }
  }

  input {
    flex: 1;
    height: 60rpx;
    font-size: 28rpx;
  }

  .picker {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    text {
      font-size: 28rpx;
      color: #333;

      &.placeholder {
        color: #999;
      }
    }
  }

  &.textarea-item {
    flex-direction: column;
    align-items: flex-start;

    .label {
      margin-bottom: 16rpx;
    }

    textarea {
      width: 100%;
      height: 160rpx;
      font-size: 28rpx;
      padding: 16rpx;
      background-color: #f5f5f5;
      border-radius: $border-radius-sm;
    }
  }

  &.location-item {
    justify-content: center;
    background-color: #f8f8f8;

    text {
      font-size: 28rpx;
      color: #1890ff;
      margin-left: 10rpx;

      &.placeholder {
        color: #999;
      }
    }
  }

  &.switch-item {
    justify-content: space-between;
  }
}

.delete-btn {
  margin-top: 40rpx;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background-color: #fff;
  border-radius: $border-radius-lg;
  font-size: 30rpx;
  color: $danger-color;
}

.save-btn {
  position: fixed;
  left: 30rpx;
  right: 30rpx;
  bottom: 40rpx;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  background: linear-gradient(135deg, $primary-color, #36cfc9);
  border-radius: 48rpx;
  font-size: 32rpx;
  color: #fff;
  margin-bottom: env(safe-area-inset-bottom);
}

/* 地区选择器 */
.region-picker {
  background-color: #fff;
  border-radius: 24rpx 24rpx 0 0;

  .picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    text {
      font-size: 28rpx;
      color: #999;
    }

    .title {
      font-weight: bold;
      color: #333;
    }

    .confirm {
      color: $primary-color;
    }
  }

  .picker-view {
    height: 500rpx;
  }

  .picker-item {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #333;
  }
}
</style>
