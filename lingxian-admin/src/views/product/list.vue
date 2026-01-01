<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>商品列表</span>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商户">
          <el-select v-model="searchForm.merchantId" placeholder="请选择商户" clearable>
            <el-option v-for="item in merchants" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image :src="row.image" style="width: 60px; height: 60px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" />
        <el-table-column prop="merchantName" label="所属商户" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { productApi, categoryApi, merchantApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const merchants = ref([])
const searchForm = reactive({ name: '', merchantId: '', categoryId: '', status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchCategories = async () => {
  try {
    const res = await categoryApi.getTree()
    if (res.code === 200) {
      categories.value = flattenCategories(res.data || [])
    }
  } catch (e) {
    console.error('获取分类失败:', e)
  }
}

// 将树形分类扁平化，便于下拉选择
const flattenCategories = (tree, result = []) => {
  tree.forEach(item => {
    result.push(item)
    if (item.children && item.children.length > 0) {
      flattenCategories(item.children, result)
    }
  })
  return result
}

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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await productApi.getList({
      page: pagination.page,
      size: pagination.pageSize,
      name: searchForm.name || undefined,
      merchantId: searchForm.merchantId || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取商品列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => {
  searchForm.name = ''
  searchForm.merchantId = ''
  searchForm.categoryId = ''
  searchForm.status = ''
  handleSearch()
}

const handleToggleStatus = async (row) => {
  const action = row.status === 1 ? '下架' : '上架'
  await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', { type: 'warning' })
  try {
    await productApi.updateStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (e) {
    ElMessage.error(`${action}失败`)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
  try {
    await productApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchCategories()
  fetchMerchants()
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
