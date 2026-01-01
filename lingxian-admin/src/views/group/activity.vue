<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>拼团活动</span>
          <el-button type="primary" @click="handleAdd">新增活动</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="searchForm.name" placeholder="活动名称" clearable style="width: 200px" @clear="fetchData" @keyup.enter="fetchData" />
        <el-select v-model="searchForm.status" placeholder="活动状态" clearable style="width: 150px" @change="fetchData">
          <el-option :value="0" label="未开始" />
          <el-option :value="1" label="进行中" />
          <el-option :value="2" label="已结束" />
          <el-option :value="3" label="已下架" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.productImage" :src="row.productImage" :preview-src-list="[row.productImage]" style="width: 60px; height: 60px" fit="cover" />
            <span v-else class="no-image">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="merchantName" label="关联商家" width="150" />
        <el-table-column label="价格" width="150">
          <template #default="{ row }">
            <div class="price-info">
              <span class="group-price">¥{{ row.groupPrice }}</span>
              <span class="original-price">¥{{ row.originalPrice }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="groupSize" label="成团人数" width="100" />
        <el-table-column label="库存/已售" width="100">
          <template #default="{ row }">
            {{ row.soldCount || 0 }}/{{ row.stock }}
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            <div class="time-info">
              <div>{{ formatDateTime(row.startTime) }}</div>
              <div>至 {{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 3" type="warning" link @click="handleOffline(row)">下架</el-button>
            <el-button v-if="row.status === 3" type="success" link @click="handleOnline(row)">上架</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="选择商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width: 100%" @change="handleProductChange">
            <el-option v-for="item in productList" :key="item.id" :value="item.id" :label="item.name">
              <div class="product-option">
                <span>{{ item.name }}</span>
                <span class="product-price">¥{{ item.price }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拼团价" prop="groupPrice">
              <el-input-number v-model="form.groupPrice" :min="0.01" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成团人数" prop="groupSize">
              <el-input-number v-model="form.groupSize" :min="2" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每人限购" prop="limitPerUser">
              <el-input-number v-model="form.limitPerUser" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动库存" prop="stock">
              <el-input-number v-model="form.stock" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成团时限" prop="expireHours">
              <el-input-number v-model="form.expireHours" :min="1" :max="72" style="width: 100%">
                <template #suffix>小时</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动时间" prop="activityTime">
          <el-date-picker
            v-model="form.activityTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="自动取消">
          <el-switch v-model="form.autoCancel" :active-value="1" :inactive-value="0" />
          <span class="form-tip">开启后，超时未成团将自动取消并退款</span>
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="活动详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="活动名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detail.productName }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ detail.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="原价">¥{{ detail.originalPrice }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">¥{{ detail.groupPrice }}</el-descriptions-item>
        <el-descriptions-item label="成团人数">{{ detail.groupSize }}人</el-descriptions-item>
        <el-descriptions-item label="每人限购">{{ detail.limitPerUser }}件</el-descriptions-item>
        <el-descriptions-item label="活动库存">{{ detail.stock }}</el-descriptions-item>
        <el-descriptions-item label="已售数量">{{ detail.soldCount }}</el-descriptions-item>
        <el-descriptions-item label="开团数量">{{ detail.groupCount }}</el-descriptions-item>
        <el-descriptions-item label="成功拼团">{{ detail.successGroupCount }}</el-descriptions-item>
        <el-descriptions-item label="成团时限">{{ detail.expireHours }}小时</el-descriptions-item>
        <el-descriptions-item label="自动取消">{{ detail.autoCancel === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="活动状态">
          <el-tag :type="getStatusType(detail.status)">{{ detail.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="活动描述" :span="2">{{ detail.description || '无' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 统计数据 -->
      <div v-if="detail.stats" class="stats-container">
        <h4>统计数据</h4>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ detail.stats.totalOrders || 0 }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value warning">{{ detail.stats.pendingGroups || 0 }}</div>
              <div class="stat-label">拼团中</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value success">{{ detail.stats.successGroups || 0 }}</div>
              <div class="stat-label">已成团</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value danger">{{ detail.stats.failedGroups || 0 }}</div>
              <div class="stat-label">已失败</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { groupApi, productApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const productList = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ name: '', status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('新增活动')
const detailDialogVisible = ref(false)
const detail = reactive({})
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  productId: null,
  merchantId: null,
  originalPrice: null,
  groupPrice: null,
  groupSize: 2,
  limitPerUser: 1,
  stock: 100,
  expireHours: 24,
  autoCancel: 1,
  activityTime: [],
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  groupPrice: [{ required: true, message: '请输入拼团价', trigger: 'blur' }],
  groupSize: [{ required: true, message: '请输入成团人数', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  activityTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await groupApi.getActivities({
      page: pagination.page,
      size: pagination.pageSize,
      name: searchForm.name || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取拼团活动列表失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchProducts = async () => {
  try {
    const res = await productApi.getList({ page: 1, size: 1000, status: 1 })
    if (res.code === 200) {
      productList.value = res.data.records || []
    }
  } catch (e) {
    console.error('获取商品列表失败:', e)
  }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = null
  pagination.page = 1
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增活动'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑活动'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    productId: row.productId,
    merchantId: row.merchantId,
    originalPrice: row.originalPrice,
    groupPrice: row.groupPrice,
    groupSize: row.groupSize,
    limitPerUser: row.limitPerUser,
    stock: row.stock,
    expireHours: row.expireHours,
    autoCancel: row.autoCancel,
    activityTime: [row.startTime, row.endTime],
    description: row.description
  })
  dialogVisible.value = true
}

const handleDetail = async (row) => {
  try {
    const res = await groupApi.getActivityDetail(row.id)
    if (res.code === 200) {
      Object.assign(detail, res.data)
      detailDialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const handleProductChange = (productId) => {
  const product = productList.value.find(p => p.id === productId)
  if (product) {
    form.originalPrice = product.price
    form.merchantId = product.merchantId
    // 默认拼团价为原价的 80%
    form.groupPrice = Math.round(product.price * 0.8 * 100) / 100
  }
}

const handleOffline = async (row) => {
  await ElMessageBox.confirm('确定要下架该活动吗？下架后用户将无法参与', '提示', { type: 'warning' })
  try {
    await groupApi.updateActivityStatus(row.id, 3)
    ElMessage.success('下架成功')
    fetchData()
  } catch (e) {
    ElMessage.error('下架失败')
  }
}

const handleOnline = async (row) => {
  await ElMessageBox.confirm('确定要重新上架该活动吗？', '提示', { type: 'warning' })
  try {
    // 根据当前时间判断状态
    const now = new Date()
    const startTime = new Date(row.startTime)
    const endTime = new Date(row.endTime)
    let newStatus = 0
    if (now >= startTime && now <= endTime) {
      newStatus = 1 // 进行中
    } else if (now < startTime) {
      newStatus = 0 // 未开始
    } else {
      newStatus = 2 // 已结束
    }
    await groupApi.updateActivityStatus(row.id, newStatus)
    ElMessage.success('上架成功')
    fetchData()
  } catch (e) {
    ElMessage.error('上架失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.groupPrice >= form.originalPrice) {
    ElMessage.warning('拼团价必须小于原价')
    return
  }

  submitLoading.value = true
  try {
    const data = {
      ...form,
      startTime: form.activityTime[0],
      endTime: form.activityTime[1]
    }
    delete data.activityTime

    if (form.id) {
      await groupApi.updateActivity(form.id, data)
    } else {
      await groupApi.createActivity(data)
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

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    productId: null,
    merchantId: null,
    originalPrice: null,
    groupPrice: null,
    groupSize: 2,
    limitPerUser: 1,
    stock: 100,
    expireHours: 24,
    autoCancel: 1,
    activityTime: [],
    description: ''
  })
  formRef.value?.resetFields()
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status] || 'info'
}

onMounted(() => {
  fetchData()
  fetchProducts()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.no-image { color: #999; font-size: 12px; }

.price-info {
  display: flex;
  flex-direction: column;
}
.group-price {
  color: #f56c6c;
  font-weight: bold;
}
.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 12px;
}
.time-info {
  font-size: 12px;
  color: #666;
}
.product-option {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
.product-price {
  color: #f56c6c;
}
.form-tip {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}
.stats-container {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}
.stats-container h4 {
  margin: 0 0 15px 0;
  color: #303133;
}
.stat-item {
  text-align: center;
  padding: 15px;
  background: #fff;
  border-radius: 4px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}
.stat-value.warning { color: #e6a23c; }
.stat-value.success { color: #67c23a; }
.stat-value.danger { color: #f56c6c; }
.stat-label {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}
</style>
