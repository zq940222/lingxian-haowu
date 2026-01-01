<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品分类</span>
          <el-button type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图标" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.icon"
              :src="row.icon"
              :preview-src-list="[row.icon]"
              style="width: 40px; height: 40px"
              fit="cover"
            />
            <span v-else class="no-icon">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="productCount" label="商品数量" width="120" />
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
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类图标">
          <div class="icon-uploader">
            <el-upload
              class="icon-upload"
              :show-file-list="false"
              :http-request="handleIconUpload"
              accept="image/*"
            >
              <el-image
                v-if="iconPreviewUrl"
                :src="iconPreviewUrl"
                class="icon-preview"
                fit="cover"
              />
              <el-icon v-else class="icon-upload-placeholder"><Plus /></el-icon>
            </el-upload>
            <el-button v-if="iconPreviewUrl" type="danger" link @click="handleDeleteIcon">删除图标</el-button>
          </div>
          <div class="upload-tip">建议尺寸: 80x80像素，支持 JPG、PNG 格式</div>
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="form.parentId" placeholder="选择父级分类（留空为一级分类）" clearable style="width: 100%">
            <el-option :value="0" label="无（一级分类）" />
            <el-option
              v-for="item in parentCategories"
              :key="item.id"
              :value="item.id"
              :label="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { categoryApi, uploadApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const form = reactive({ id: null, name: '', sort: 0, icon: '', parentId: 0 })
// 用于预览的图标URL（可能是签名后的完整URL）
const iconPreviewUrl = ref('')

// 获取一级分类作为父级选项
const parentCategories = computed(() => {
  return tableData.value.filter(item => item.parentId === 0 && item.id !== form.id)
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await categoryApi.getTree()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    console.error('获取分类列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  form.id = null
  form.name = ''
  form.sort = 0
  form.icon = ''
  form.parentId = 0
  iconPreviewUrl.value = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    sort: row.sort,
    icon: row.icon || '',
    parentId: row.parentId || 0
  })
  // row.icon 已经是后端处理过的完整URL，可直接用于预览
  iconPreviewUrl.value = row.icon || ''
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该分类吗？删除后不可恢复', '提示', { type: 'warning' })
  try {
    await categoryApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleStatusChange = async (row) => {
  try {
    await categoryApi.update(row.id, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 上传图标
const handleIconUpload = async ({ file }) => {
  try {
    const res = await uploadApi.uploadImage(file)
    if (res.code === 200) {
      form.icon = res.data.url  // 存储路径，保存到数据库
      iconPreviewUrl.value = res.data.previewUrl || res.data.url  // 预览URL
      ElMessage.success('图标上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    console.error('上传图标失败:', e)
    ElMessage.error('上传图标失败')
  }
}

// 删除图标
const handleDeleteIcon = () => {
  form.icon = ''
  iconPreviewUrl.value = ''
}

const handleSubmit = async () => {
  if (!form.name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  submitLoading.value = true
  try {
    if (form.id) {
      await categoryApi.update(form.id, form)
    } else {
      await categoryApi.create(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.no-icon { color: #999; font-size: 12px; }

.icon-uploader {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-upload {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.icon-upload:hover {
  border-color: #409eff;
}

.icon-preview {
  width: 80px;
  height: 80px;
}

.icon-upload-placeholder {
  font-size: 28px;
  color: #8c939d;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
