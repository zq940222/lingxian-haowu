<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>拼团记录</span>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="activityName" label="活动名称" />
        <el-table-column prop="leaderName" label="团长" width="120" />
        <el-table-column prop="currentPeople" label="当前人数" width="100" />
        <el-table-column prop="targetPeople" label="目标人数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypes[row.status]">{{ statusLabels[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="开团时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(false)
const tableData = ref([])

const statusLabels = ['拼团中', '已成团', '已失败']
const statusTypes = ['warning', 'success', 'danger']

const fetchData = async () => {
  loading.value = true
  setTimeout(() => { tableData.value = []; loading.value = false }, 500)
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
</style>
