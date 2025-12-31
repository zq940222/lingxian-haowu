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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  setTimeout(() => { tableData.value = []; loading.value = false }, 500)
}

const handleApprove = async (row) => {
  await ElMessageBox.confirm('确定通过该商户的入驻申请？', '提示')
  ElMessage.success('审核通过')
  fetchData()
}

const handleReject = async (row) => {
  await ElMessageBox.confirm('确定拒绝该商户的入驻申请？', '提示', { type: 'warning' })
  ElMessage.success('已拒绝')
  fetchData()
}

const handleView = () => {}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
