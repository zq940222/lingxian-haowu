<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>退款管理</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="refundNo" label="退款单号" width="200" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="amount" label="退款金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="退款原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'warning'">
              {{ ['待处理', '已退款', '已拒绝'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" link @click="handleApprove(row)">同意</el-button>
              <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
            </template>
            <el-button type="primary" link v-else>查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { refundApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await refundApi.getList({
      page: pagination.page,
      size: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取退款列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入审核备注', '同意退款', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '选填'
  })
  try {
    await refundApi.approve(row.id, { remark: value })
    ElMessage.success('退款成功')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请填写拒绝原因',
    inputValidator: (val) => val ? true : '请输入拒绝原因'
  })
  try {
    await refundApi.reject(row.id, { remark: value })
    ElMessage.success('已拒绝')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
