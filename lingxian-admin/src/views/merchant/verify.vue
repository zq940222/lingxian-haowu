<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>商户审核</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商户名称" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { merchantApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await merchantApi.getPendingList({
      page: pagination.page,
      size: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取待审核商户列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入审核备注', '通过审核', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '选填'
  })
  try {
    await merchantApi.verify(row.id, { verifyStatus: 2, remark: value })
    ElMessage.success('审核通过')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleReject = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请填写拒绝原因',
    inputValidator: (val) => val ? true : '请输入拒绝原因'
  })
  try {
    await merchantApi.verify(row.id, { verifyStatus: 3, remark: value })
    ElMessage.success('已拒绝')
    fetchData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleView = (row) => {
  router.push(`/merchant/detail/${row.id}`)
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
