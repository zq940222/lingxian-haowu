<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>拼团活动</span>
          <el-button type="primary" @click="handleAdd">新增活动</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="groupPrice" label="拼团价" width="100">
          <template #default="{ row }">¥{{ row.groupPrice }}</template>
        </el-table-column>
        <el-table-column prop="minPeople" label="成团人数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

const fetchData = async () => {
  loading.value = true
  setTimeout(() => { tableData.value = []; loading.value = false }, 500)
}

const handleAdd = () => {}
const handleEdit = () => {}
const handleDelete = () => {}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
