<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="adminCount" label="管理员数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handlePermission(row)">权限配置</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="row.code === 'ROLE_ADMIN'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="form.code" placeholder="请输入角色编码（如：ROLE_OPERATOR）" :disabled="form.code === 'ROLE_ADMIN'" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
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

    <!-- 权限配置弹窗 -->
    <el-dialog v-model="permissionDialogVisible" title="权限配置" width="500px">
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedPermissions"
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const form = reactive({ id: null, name: '', code: '', description: '', status: 1 })

// 权限配置相关
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref(null)
const currentRoleId = ref(null)
const checkedPermissions = ref([])
const permissionTree = ref([])

// 获取权限树
const fetchPermissionTree = async () => {
  try {
    const res = await systemApi.getPermissions()
    if (res.code === 200) {
      permissionTree.value = res.data || []
    }
  } catch (e) {
    console.error('获取权限树失败:', e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await systemApi.getRoles()
    if (res.code === 200) {
      tableData.value = res.data || []
    }
  } catch (e) {
    console.error('获取角色列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  Object.assign(form, { id: null, name: '', code: '', description: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handlePermission = async (row) => {
  currentRoleId.value = row.id
  // 使用角色的权限ID列表
  checkedPermissions.value = row.permissionIds || []
  permissionDialogVisible.value = true

  // 等待弹窗渲染后设置选中状态
  await nextTick()
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys(checkedPermissions.value)
  }
}

const handleSavePermission = async () => {
  try {
    const checkedKeys = permissionTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = permissionTreeRef.value?.getHalfCheckedKeys() || []
    // 合并全选和半选的节点
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    await systemApi.updateRole(currentRoleId.value, { permissionIds: allKeys })
    ElMessage.success('权限配置保存成功')
    permissionDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('权限配置保存失败')
  }
}

const handleDelete = async (row) => {
  if (row.code === 'ROLE_ADMIN') {
    ElMessage.warning('不能删除超级管理员角色')
    return
  }
  if (row.adminCount > 0) {
    ElMessage.warning('该角色下还有管理员，不能删除')
    return
  }
  await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' })
  try {
    await systemApi.deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await systemApi.updateRole(form.id, form)
    } else {
      await systemApi.createRole(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchPermissionTree()
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
