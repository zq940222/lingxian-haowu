<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>商户审核</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="入驻审核" name="entry">
          <el-table :data="entryTableData" v-loading="entryLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="商户名称" />
            <el-table-column prop="contactPhone" label="联系电话" />
            <el-table-column prop="address" label="地址" show-overflow-tooltip />
            <el-table-column prop="createTime" label="申请时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link @click="handleEntryApprove(row)">通过</el-button>
                <el-button type="danger" link @click="handleEntryReject(row)">拒绝</el-button>
                <el-button type="primary" link @click="handleEntryView(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="entryPagination.total > 0"
            class="pagination"
            :current-page="entryPagination.page"
            :page-size="entryPagination.pageSize"
            :total="entryPagination.total"
            layout="total, prev, pager, next"
            @current-change="handleEntryPageChange"
          />
        </el-tab-pane>

        <el-tab-pane label="信息变更审核" name="info">
          <el-table :data="infoTableData" v-loading="infoLoading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="merchantName" label="商户名称" width="150" />
            <el-table-column label="变更内容" min-width="200">
              <template #default="{ row }">
                <el-tag
                  v-for="field in row.changedFields"
                  :key="field"
                  size="small"
                  style="margin-right: 4px;"
                >
                  {{ fieldNameMap[field] || field }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="本月修改" width="100">
              <template #default="{ row }">
                {{ row.monthlyModifyCount }}/3 次
              </template>
            </el-table-column>
            <el-table-column prop="applyTime" label="申请时间" width="180" />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleInfoView(row)">查看变更</el-button>
                <el-button type="success" link @click="handleInfoApprove(row)">通过</el-button>
                <el-button type="danger" link @click="handleInfoReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="infoPagination.total > 0"
            class="pagination"
            :current-page="infoPagination.page"
            :page-size="infoPagination.pageSize"
            :total="infoPagination.total"
            layout="total, prev, pager, next"
            @current-change="handleInfoPageChange"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 信息变更详情对话框 -->
    <el-dialog v-model="infoDetailVisible" title="信息变更详情" width="700px">
      <div v-if="infoDetail" class="info-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商户名称">{{ infoDetail.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="本月修改次数">{{ infoDetail.monthlyModifyCount }}/3 次</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ infoDetail.applyTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">变更对比</el-divider>

        <el-table :data="changeCompareData" border style="width: 100%">
          <el-table-column prop="field" label="字段" width="100" />
          <el-table-column label="当前值">
            <template #default="{ row }">
              <template v-if="row.field === '店铺头像'">
                <el-image
                  :src="row.current"
                  style="width: 60px; height: 60px; border-radius: 4px;"
                  fit="cover"
                />
              </template>
              <template v-else>{{ row.current }}</template>
            </template>
          </el-table-column>
          <el-table-column label="变更为" width="60" align="center">
            <template #default>
              <el-icon><Right /></el-icon>
            </template>
          </el-table-column>
          <el-table-column label="新值">
            <template #default="{ row }">
              <template v-if="row.field === '店铺头像'">
                <el-image
                  :src="row.pending"
                  style="width: 60px; height: 60px; border-radius: 4px;"
                  fit="cover"
                />
              </template>
              <template v-else>
                <span :class="{ 'text-changed': row.changed }">{{ row.pending }}</span>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="infoDetailVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleInfoReject(infoDetail)">拒绝</el-button>
        <el-button type="success" @click="handleInfoApprove(infoDetail)">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right } from '@element-plus/icons-vue'
import { merchantApi } from '@/api'

const router = useRouter()

const activeTab = ref('entry')

// 字段名称映射
const fieldNameMap = {
  shopLogo: '店铺头像',
  shopName: '店铺名称',
  phone: '联系电话',
  address: '店铺地址'
}

// ========== 入驻审核 ==========
const entryLoading = ref(false)
const entryTableData = ref([])
const entryPagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchEntryData = async () => {
  entryLoading.value = true
  try {
    const res = await merchantApi.getPendingList({
      page: entryPagination.page,
      size: entryPagination.pageSize
    })
    if (res.code === 200) {
      entryTableData.value = res.data.records || []
      entryPagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取待审核商户列表失败:', e)
  } finally {
    entryLoading.value = false
  }
}

const handleEntryApprove = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入审核备注', '通过审核', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '选填'
    })
    await merchantApi.verify(row.id, { verifyStatus: 2, remark: value })
    ElMessage.success('审核通过')
    fetchEntryData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleEntryReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请填写拒绝原因',
      inputValidator: (val) => val ? true : '请输入拒绝原因'
    })
    await merchantApi.verify(row.id, { verifyStatus: 3, remark: value })
    ElMessage.success('已拒绝')
    fetchEntryData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleEntryView = (row) => {
  router.push(`/merchant/detail/${row.id}`)
}

const handleEntryPageChange = (page) => {
  entryPagination.page = page
  fetchEntryData()
}

// ========== 信息变更审核 ==========
const infoLoading = ref(false)
const infoTableData = ref([])
const infoPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const infoDetailVisible = ref(false)
const infoDetail = ref(null)

const fetchInfoData = async () => {
  infoLoading.value = true
  try {
    const res = await merchantApi.getInfoAuditList({
      page: infoPagination.page,
      size: infoPagination.pageSize,
      status: 1 // 只获取待审核的
    })
    if (res.code === 200) {
      infoTableData.value = res.data.records || []
      infoPagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取信息变更审核列表失败:', e)
  } finally {
    infoLoading.value = false
  }
}

// 变更对比数据
const changeCompareData = computed(() => {
  if (!infoDetail.value) return []
  const { currentInfo, pendingInfo, changedFields } = infoDetail.value
  const fields = [
    { key: 'shopLogo', label: '店铺头像' },
    { key: 'shopName', label: '店铺名称' },
    { key: 'phone', label: '联系电话' },
    { key: 'address', label: '店铺地址' }
  ]
  return fields
    .filter(f => changedFields.includes(f.key))
    .map(f => ({
      field: f.label,
      current: currentInfo[f.key],
      pending: pendingInfo[f.key],
      changed: currentInfo[f.key] !== pendingInfo[f.key]
    }))
})

const handleInfoView = (row) => {
  infoDetail.value = row
  infoDetailVisible.value = true
}

const handleInfoApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定通过此信息变更申请？', '确认通过', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    await merchantApi.auditInfoChange(row.id, { status: 2 })
    ElMessage.success('审核通过')
    infoDetailVisible.value = false
    fetchInfoData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleInfoReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝变更', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请填写拒绝原因',
      inputValidator: (val) => val ? true : '请输入拒绝原因'
    })
    await merchantApi.auditInfoChange(row.id, { status: 3, rejectReason: value })
    ElMessage.success('已拒绝')
    infoDetailVisible.value = false
    fetchInfoData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleInfoPageChange = (page) => {
  infoPagination.page = page
  fetchInfoData()
}

// Tab 切换
const handleTabChange = (tab) => {
  if (tab === 'entry') {
    fetchEntryData()
  } else if (tab === 'info') {
    fetchInfoData()
  }
}

onMounted(() => {
  fetchEntryData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.info-detail {
  padding: 0 10px;
}
.text-changed {
  color: #e6a23c;
  font-weight: 500;
}
</style>
