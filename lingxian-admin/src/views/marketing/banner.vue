<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>轮播图管理</span>
          <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="imageUrl" label="图片" width="200">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" style="width: 150px; height: 60px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="图片" required>
          <el-upload
            class="banner-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <el-image v-if="form.imageUrl" :src="form.imageUrl" class="banner-preview" fit="cover" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span>点击上传图片</span>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸：750 x 300 像素，支持 jpg、png 格式</div>
        </el-form-item>
        <el-form-item label="跳转类型">
          <el-select v-model="form.linkType" placeholder="不跳转" clearable @change="handleLinkTypeChange">
            <el-option label="不跳转" :value="null" />
            <el-option label="商品详情" :value="1" />
            <el-option label="商户店铺" :value="2" />
            <el-option label="活动页面" :value="3" />
            <el-option label="外部链接" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.linkType" label="跳转值">
          <el-input v-model="form.linkUrl" :placeholder="linkValuePlaceholder" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { bannerApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新增轮播图')
const form = reactive({ id: null, title: '', imageUrl: '', linkType: null, linkUrl: '', sort: 0, status: 1 })

// 图片上传相关
const uploadUrl = '/api/admin/upload/image'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

const linkValuePlaceholder = computed(() => {
  const placeholders = {
    1: '请输入商品ID',
    2: '请输入商户ID',
    3: '请输入活动ID',
    4: '请输入跳转链接'
  }
  return placeholders[form.linkType] || '请输入跳转值'
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200 && response.data) {
    form.imageUrl = response.data.url || response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const handleLinkTypeChange = () => {
  form.linkUrl = ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await bannerApi.getList({
      page: pagination.page,
      size: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取轮播图列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增轮播图'
  Object.assign(form, { id: null, title: '', imageUrl: '', linkType: null, linkUrl: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑轮播图'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', { type: 'warning' })
  try {
    await bannerApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await bannerApi.update(form.id, form)
    } else {
      await bannerApi.create(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleStatusChange = async (row) => {
  try {
    await bannerApi.update(row.id, { status: row.status })
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.banner-uploader {
  width: 100%;
}
.banner-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 100%;
}
.banner-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}
.banner-preview {
  width: 100%;
  height: 120px;
  display: block;
}
.upload-placeholder {
  width: 100%;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  background: #fafafa;
}
.upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}
.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
