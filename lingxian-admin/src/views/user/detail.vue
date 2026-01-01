<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ userInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ userInfo.points }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
            {{ userInfo.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ userInfo.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { userApi } from '@/api'

const route = useRoute()
const userInfo = ref({})
const loading = ref(false)

const fetchUserDetail = async () => {
  loading.value = true
  try {
    const res = await userApi.getDetail(route.params.id)
    if (res.code === 200) {
      userInfo.value = res.data || {}
    }
  } catch (e) {
    console.error('获取用户详情失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserDetail()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
