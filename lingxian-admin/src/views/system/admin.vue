<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>管理员管理</span>
          <el-button type="primary" @click="handleAdd">新增管理员</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="roleName" label="角色" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="170">
          <template #default="{ row }">
            {{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="row.username === 'admin'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" required>
          <el-input v-model="form.password" type="password" placeholder="请输入密码，不填则默认123456" show-password />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const roles = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const dialogTitle = ref('新增管理员')
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', email: '', roleId: null, status: 1 })

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchRoles = async () => {
  try {
    const res = await systemApi.getRoles()
    if (res.code === 200) {
      roles.value = res.data || []
    }
  } catch (e) {
    console.error('获取角色列表失败:', e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await systemApi.getAdmins({
      page: pagination.page,
      size: pagination.pageSize
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取管理员列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增管理员'
  Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', email: '', roleId: null, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑管理员'
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm('确定重置该管理员的密码为 123456？', '提示', { type: 'warning' })
  try {
    await systemApi.resetAdminPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  } catch (e) {
    ElMessage.error('重置失败')
  }
}

const handleDelete = async (row) => {
  if (row.username === 'admin') {
    ElMessage.warning('不能删除超级管理员')
    return
  }
  await ElMessageBox.confirm('确定要删除该管理员吗？', '提示', { type: 'warning' })
  try {
    await systemApi.deleteAdmin(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async () => {
  if (!form.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.realName) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!form.roleId) {
    ElMessage.warning('请选择角色')
    return
  }
  try {
    if (form.id) {
      await systemApi.updateAdmin(form.id, form)
    } else {
      await systemApi.createAdmin(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchRoles()
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
