<template>
  <view class="container">
    <view class="form">
      <!-- 商品图片 -->
      <view class="form-section">
        <view class="section-title">商品图片</view>
        <view class="image-upload">
          <view class="image-list">
            <view class="image-item" v-for="(img, index) in images" :key="index">
              <image :src="img" mode="aspectFill" />
              <view class="delete-btn" @click="removeImage(index)">
                <uni-icons type="close" size="14" color="#fff" />
              </view>
            </view>
            <view class="add-image" v-if="images.length < 5" @click="chooseImage">
              <uni-icons type="plusempty" size="40" color="#ccc" />
              <text>添加图片</text>
            </view>
          </view>
          <text class="tip">最多上传5张图片，首张为主图</text>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>
        <view class="form-item">
          <text class="label required">商品名称</text>
          <input type="text" v-model="form.name" placeholder="请输入商品名称" maxlength="50" />
        </view>
        <view class="form-item">
          <text class="label required">商品分类</text>
          <picker mode="multiSelector" :value="categoryIndexes" :range="categoryColumns" @change="onCategoryChange" @columnchange="onColumnChange">
            <view class="picker">
              <text :class="{ placeholder: !selectedCategoryText }">
                {{ selectedCategoryText || '请选择分类' }}
              </text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">商品规格</text>
          <input type="text" v-model="form.spec" placeholder="如：500g/份" />
        </view>
        <view class="form-item textarea-item">
          <text class="label">商品描述</text>
          <textarea v-model="form.description" placeholder="请输入商品描述" maxlength="500" />
        </view>
      </view>

      <!-- 价格库存 -->
      <view class="form-section">
        <view class="section-title">价格库存</view>
        <view class="form-item">
          <text class="label required">商品价格</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.price" placeholder="0.00" />
            <text class="unit">元</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label">原价</text>
          <view class="input-with-unit">
            <input type="digit" v-model="form.originalPrice" placeholder="0.00" />
            <text class="unit">元</text>
          </view>
        </view>
        <view class="form-item">
          <text class="label required">库存数量</text>
          <view class="input-with-unit">
            <input type="number" v-model="form.stock" placeholder="0" />
            <text class="unit">件</text>
          </view>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="form-section">
        <view class="section-title">其他设置</view>
        <view class="form-item switch-item">
          <text class="label">上架销售</text>
          <switch :checked="form.status === 1" @change="onStatusChange" color="#1890ff" />
        </view>
        <view class="form-item">
          <text class="label">排序权重</text>
          <input type="number" v-model="form.sort" placeholder="数值越大越靠前" />
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="footer">
      <view class="btn cancel" @click="goBack">取消</view>
      <view class="btn submit" @click="submit">{{ isEdit ? '保存' : '添加' }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { productApi, categoryApi, uploadApi } from '@/api'

const productId = ref('')
const isEdit = computed(() => !!productId.value)

// 分类数据（树形结构）
const categoryTree = ref([])
// 二级联动选择器的索引 [一级索引, 二级索引]
const categoryIndexes = ref([0, 0])
// 二级联动选择器的列数据
const categoryColumns = computed(() => {
  const firstColumn = categoryTree.value.map(c => c.name)
  const parentIdx = categoryIndexes.value[0]
  const parent = categoryTree.value[parentIdx]
  const secondColumn = parent && parent.children ? parent.children.map(c => c.name) : []
  return [firstColumn, secondColumn]
})
// 选中的分类文本
const selectedCategoryText = computed(() => {
  const parentIdx = categoryIndexes.value[0]
  const childIdx = categoryIndexes.value[1]
  const parent = categoryTree.value[parentIdx]
  if (!parent) return ''
  const child = parent.children?.[childIdx]
  if (!child) return parent.name
  return `${parent.name} / ${child.name}`
})

const images = ref([])  // 预览URL列表
const imagePaths = ref([])  // 存储路径列表（用于提交到后端）
const form = ref({
  name: '',
  categoryId: '',
  spec: '',
  description: '',
  price: '',
  originalPrice: '',
  stock: '',
  status: 1,
  sort: ''
})

onLoad((options) => {
  loadCategories()
  if (options.id) {
    productId.value = options.id
    uni.setNavigationBarTitle({ title: '编辑商品' })
    loadProduct()
  } else {
    uni.setNavigationBarTitle({ title: '添加商品' })
  }
})

// 加载分类（树形结构）
const loadCategories = async () => {
  try {
    const res = await categoryApi.getTree()
    if (res.code === 200) {
      categoryTree.value = res.data || []
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

// 加载商品详情
const loadProduct = async () => {
  try {
    uni.showLoading({ title: '加载中...' })
    const res = await productApi.getDetail(productId.value)
    if (res.code === 200) {
      const data = res.data
      form.value = {
        name: data.name,
        categoryId: data.categoryId,
        spec: data.spec || '',
        description: data.description || '',
        price: String(data.price),
        originalPrice: data.originalPrice ? String(data.originalPrice) : '',
        stock: String(data.stock),
        status: data.status,
        sort: data.sort ? String(data.sort) : ''
      }
      // 编辑时，后端返回的已是预览URL，需要同时设置预览和路径
      // 注意：编辑已有商品时，images返回的是预览URL，需要从mainImage获取存储路径
      images.value = data.images || (data.mainImage ? [data.mainImage] : [])
      // 编辑时假设后端已处理好，存储路径需要后端返回原始路径
      // 如果后端没有返回原始路径，编辑保存时会使用预览URL（后端需要能处理）
      imagePaths.value = [...images.value]

      // 设置二级分类索引
      setCategoryIndexById(data.categoryId)
    }
  } catch (e) {
    console.error('加载商品失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 选择图片
const chooseImage = async () => {
  try {
    const res = await uni.chooseImage({
      count: 5 - images.value.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera']
    })

    // 上传图片
    for (const tempFilePath of res.tempFilePaths) {
      await uploadImage(tempFilePath)
    }
  } catch (e) {
    // 取消选择
  }
}

// 上传图片
const uploadImage = async (filePath) => {
  try {
    uni.showLoading({ title: '上传中...' })
    const uploadRes = await uploadApi.uploadImage(filePath)
    // uploadRes 包含 { url: '存储路径', previewUrl: '预览URL' }
    // 显示使用 previewUrl，保存使用 url（存储路径）
    images.value.push(uploadRes.previewUrl)
    imagePaths.value.push(uploadRes.url)
  } catch (e) {
    console.error('上传失败', e)
    uni.showToast({ title: e.message || '上传失败', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

// 删除图片
const removeImage = (index) => {
  images.value.splice(index, 1)
  imagePaths.value.splice(index, 1)
}

// 根据分类ID设置索引（用于编辑时回显）
const setCategoryIndexById = (categoryId) => {
  for (let i = 0; i < categoryTree.value.length; i++) {
    const parent = categoryTree.value[i]
    // 检查是否是一级分类（虽然商品应该选到二级，但做兼容）
    if (parent.id === categoryId) {
      categoryIndexes.value = [i, 0]
      return
    }
    // 检查子分类
    if (parent.children) {
      for (let j = 0; j < parent.children.length; j++) {
        if (parent.children[j].id === categoryId) {
          categoryIndexes.value = [i, j]
          return
        }
      }
    }
  }
}

// 列变化（切换一级分类时，重置二级分类索引）
const onColumnChange = (e) => {
  const { column, value } = e.detail
  if (column === 0) {
    // 一级分类变化，重置二级索引为0
    categoryIndexes.value = [value, 0]
  }
}

// 分类选择确认
const onCategoryChange = (e) => {
  const [parentIdx, childIdx] = e.detail.value
  categoryIndexes.value = [parentIdx, childIdx]

  // 获取选中的二级分类ID
  const parent = categoryTree.value[parentIdx]
  if (parent && parent.children && parent.children[childIdx]) {
    form.value.categoryId = parent.children[childIdx].id
  } else if (parent) {
    // 如果没有子分类，使用一级分类ID（兜底）
    form.value.categoryId = parent.id
  }
}

// 状态变更
const onStatusChange = (e) => {
  form.value.status = e.detail.value ? 1 : 0
}

// 验证表单
const validate = () => {
  if (images.value.length === 0) {
    uni.showToast({ title: '请上传商品图片', icon: 'none' })
    return false
  }
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入商品名称', icon: 'none' })
    return false
  }
  if (!form.value.categoryId) {
    uni.showToast({ title: '请选择商品分类', icon: 'none' })
    return false
  }
  // 验证是否选择了二级分类
  const parentIdx = categoryIndexes.value[0]
  const parent = categoryTree.value[parentIdx]
  if (parent && parent.children && parent.children.length > 0) {
    const childIdx = categoryIndexes.value[1]
    if (childIdx < 0 || !parent.children[childIdx]) {
      uni.showToast({ title: '请选择二级分类', icon: 'none' })
      return false
    }
  }
  if (!form.value.price || parseFloat(form.value.price) <= 0) {
    uni.showToast({ title: '请输入有效价格', icon: 'none' })
    return false
  }
  if (!form.value.stock || parseInt(form.value.stock) < 0) {
    uni.showToast({ title: '请输入有效库存', icon: 'none' })
    return false
  }
  return true
}

// 提交
const submit = async () => {
  if (!validate()) return

  try {
    uni.showLoading({ title: '保存中...' })

    const data = {
      ...form.value,
      // 使用存储路径提交，而非预览URL
      mainImage: imagePaths.value[0],
      images: imagePaths.value,
      price: parseFloat(form.value.price),
      originalPrice: form.value.originalPrice ? parseFloat(form.value.originalPrice) : null,
      stock: parseInt(form.value.stock),
      sort: form.value.sort ? parseInt(form.value.sort) : 0
    }

    let res
    if (isEdit.value) {
      res = await productApi.update(productId.value, data)
    } else {
      res = await productApi.add(data)
    }

    if (res.code === 200) {
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    uni.hideLoading()
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: calc(140rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.form {
  padding: 20rpx;
}

.form-section {
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

/* 图片上传 */
.image-upload {
  padding: 24rpx 30rpx;

  .image-list {
    display: flex;
    flex-wrap: wrap;

    .image-item {
      position: relative;
      width: 160rpx;
      height: 160rpx;
      margin-right: 20rpx;
      margin-bottom: 20rpx;

      image {
        width: 100%;
        height: 100%;
        border-radius: $border-radius-sm;
      }

      .delete-btn {
        position: absolute;
        top: -10rpx;
        right: -10rpx;
        width: 36rpx;
        height: 36rpx;
        background-color: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .add-image {
      width: 160rpx;
      height: 160rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border: 2rpx dashed #ddd;
      border-radius: $border-radius-sm;

      text {
        font-size: 22rpx;
        color: #999;
        margin-top: 10rpx;
      }
    }
  }

  .tip {
    font-size: 24rpx;
    color: #999;
  }
}

/* 表单项 */
.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx;
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
    text-align: right;
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
      height: 200rpx;
      font-size: 28rpx;
      padding: 16rpx;
      background-color: #f5f5f5;
      border-radius: $border-radius-sm;
    }
  }

  &.switch-item {
    justify-content: space-between;
  }

  .input-with-unit {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    input {
      width: 200rpx;
    }

    .unit {
      font-size: 28rpx;
      color: #666;
      margin-left: 10rpx;
    }
  }
}

/* 底部按钮 */
.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));

  .btn {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    border-radius: 44rpx;
    font-size: 32rpx;

    &.cancel {
      background-color: #f5f5f5;
      color: #666;
      margin-right: 20rpx;
    }

    &.submit {
      background-color: $primary-color;
      color: #fff;
    }
  }
}
</style>
