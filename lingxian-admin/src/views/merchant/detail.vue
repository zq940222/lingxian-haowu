<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商户详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="商户ID">{{ merchantInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ merchantInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ merchantInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ merchantInfo.address }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="merchantInfo.status === 1 ? 'success' : 'info'">
            {{ merchantInfo.status === 1 ? '营业中' : '休息中' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入驻时间">{{ merchantInfo.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { merchantApi } from '@/api'

const route = useRoute()
const merchantInfo = ref({})
const loading = ref(false)

const fetchMerchantDetail = async () => {
  loading.value = true
  try {
    const res = await merchantApi.getDetail(route.params.id)
    if (res.code === 200) {
      merchantInfo.value = res.data || {}
    }
  } catch (e) {
    console.error('获取商户详情失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMerchantDetail()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
