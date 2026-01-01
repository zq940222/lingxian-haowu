<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>订单列表</span>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="配送中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="merchantName" label="商户" width="150" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypes[row.status]">{{ statusLabels[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
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
import { orderApi } from '@/api'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ orderNo: '', status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const statusLabels = ['待付款', '待发货', '待收货', '已完成', '已取消', '退款中']
const statusTypes = ['warning', 'primary', 'info', 'success', 'danger', 'warning']

const fetchData = async () => {
  loading.value = true
  try {
    const res = await orderApi.getList({
      page: pagination.page,
      size: pagination.pageSize,
      orderNo: searchForm.orderNo || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取订单列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { searchForm.orderNo = ''; searchForm.status = ''; handleSearch() }
const handleView = (row) => router.push(`/order/detail/${row.id}`)

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
