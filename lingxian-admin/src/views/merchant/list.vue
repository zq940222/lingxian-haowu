<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>商户列表</span>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商户名称">
          <el-input v-model="searchForm.name" placeholder="请输入商户名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="营业中" :value="1" />
            <el-option label="休息中" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商户名称" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '营业中' : '休息中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="入驻时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ name: '', status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    tableData.value = []
    loading.value = false
  }, 500)
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.name = ''; searchForm.status = ''; handleSearch() }
const handleView = (row) => router.push(`/merchant/detail/${row.id}`)
const handleDelete = () => {}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
