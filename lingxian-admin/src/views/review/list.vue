<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评价管理</span>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="4">
          <div class="stat-card">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">总评价数</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card good">
            <div class="stat-value">{{ stats.goodRate || '0.0' }}%</div>
            <div class="stat-label">好评率</div>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="stat-card">
            <div class="stat-value">{{ stats.rating5 || 0 }}</div>
            <div class="stat-label">5星</div>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="stat-card">
            <div class="stat-value">{{ stats.rating4 || 0 }}</div>
            <div class="stat-label">4星</div>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="stat-card">
            <div class="stat-value">{{ stats.rating3 || 0 }}</div>
            <div class="stat-label">3星</div>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="stat-card">
            <div class="stat-value">{{ stats.rating2 || 0 }}</div>
            <div class="stat-label">2星</div>
          </div>
        </el-col>
        <el-col :span="3">
          <div class="stat-card bad">
            <div class="stat-value">{{ stats.rating1 || 0 }}</div>
            <div class="stat-label">1星</div>
          </div>
        </el-col>
      </el-row>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="searchForm.userNickname" placeholder="请输入用户昵称" clearable />
        </el-form-item>
        <el-form-item label="商户">
          <el-select v-model="searchForm.merchantId" placeholder="请选择商户" clearable>
            <el-option v-for="item in merchants" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="searchForm.rating" placeholder="请选择评分" clearable>
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="显示" :value="1" />
            <el-option label="隐藏" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-info">
              <el-image :src="row.productImage" class="product-image" fit="cover" />
              <span class="product-name">{{ row.productName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="140">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.userAvatar" :size="30" />
              <span class="user-name">{{ row.userNickname || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商户" width="120" />
        <el-table-column label="评分" width="140">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="评价图片" width="120">
          <template #default="{ row }">
            <div v-if="row.images" class="review-images">
              <el-image
                v-for="(img, idx) in row.images.split(',')"
                :key="idx"
                :src="img"
                :preview-src-list="row.images.split(',')"
                class="review-image"
                fit="cover"
              />
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="商家回复" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.replyContent || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '隐藏' : '显示' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 评价详情弹窗 -->
    <el-dialog v-model="detailVisible" title="评价详情" width="600px">
      <el-descriptions :column="2" border v-if="currentReview">
        <el-descriptions-item label="订单编号">{{ currentReview.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ formatDateTime(currentReview.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="用户">
          <div class="user-info">
            <el-avatar :src="currentReview.userAvatar" :size="24" />
            <span>{{ currentReview.userNickname }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="商户">{{ currentReview.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="商品" :span="2">
          <div class="product-info">
            <el-image :src="currentReview.productImage" class="product-image" fit="cover" />
            <span>{{ currentReview.productName }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="评分" :span="2">
          <el-rate v-model="currentReview.rating" disabled />
        </el-descriptions-item>
        <el-descriptions-item label="评价内容" :span="2">{{ currentReview.content || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评价图片" :span="2">
          <div v-if="currentReview.images" class="review-images-large">
            <el-image
              v-for="(img, idx) in currentReview.images.split(',')"
              :key="idx"
              :src="img"
              :preview-src-list="currentReview.images.split(',')"
              class="review-image-large"
              fit="cover"
            />
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="是否匿名">{{ currentReview.isAnonymous === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentReview.status === 1 ? 'success' : 'info'">
            {{ currentReview.status === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商家回复" :span="2">{{ currentReview.replyContent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="回复时间" :span="2" v-if="currentReview.replyContent">
          {{ formatDateTime(currentReview.replyTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reviewApi, merchantApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const merchants = ref([])
const stats = ref({})
const detailVisible = ref(false)
const currentReview = ref(null)

const searchForm = reactive({
  productName: '',
  userNickname: '',
  merchantId: '',
  rating: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const fetchMerchants = async () => {
  try {
    const res = await merchantApi.getList({ page: 1, size: 100 })
    if (res.code === 200) {
      merchants.value = res.data.records || []
    }
  } catch (e) {
    console.error('获取商户列表失败:', e)
  }
}

const fetchStats = async () => {
  try {
    const res = await reviewApi.getStats({
      merchantId: searchForm.merchantId || undefined
    })
    if (res.code === 200) {
      stats.value = res.data || {}
    }
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await reviewApi.getList({
      page: pagination.page,
      size: pagination.pageSize,
      productName: searchForm.productName || undefined,
      userNickname: searchForm.userNickname || undefined,
      merchantId: searchForm.merchantId || undefined,
      rating: searchForm.rating !== '' ? searchForm.rating : undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取评价列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
  fetchStats()
}

const handleReset = () => {
  searchForm.productName = ''
  searchForm.userNickname = ''
  searchForm.merchantId = ''
  searchForm.rating = ''
  searchForm.status = ''
  handleSearch()
}

const handleView = async (row) => {
  try {
    const res = await reviewApi.getDetail(row.id)
    if (res.code === 200) {
      currentReview.value = res.data
      detailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取评价详情失败')
  }
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '隐藏' : '显示'
  await ElMessageBox.confirm(`确定要${action}该评价吗？`, '提示', { type: 'warning' })
  try {
    await reviewApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`${action}成功`)
    fetchData()
    fetchStats()
  } catch (e) {
    ElMessage.error(`${action}失败`)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该评价吗？删除后无法恢复', '提示', { type: 'warning' })
  try {
    await reviewApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
    fetchStats()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleSizeChange = () => fetchData()
const handleCurrentChange = () => fetchData()

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  fetchMerchants()
  fetchStats()
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
}

.stat-card.good {
  background: #f0f9eb;
}

.stat-card.bad {
  background: #fef0f0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-card.good .stat-value {
  color: #67c23a;
}

.stat-card.bad .stat-value {
  color: #f56c6c;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}

.product-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-images {
  display: flex;
  gap: 4px;
}

.review-image {
  width: 30px;
  height: 30px;
  border-radius: 4px;
}

.review-images-large {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-image-large {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}
</style>
