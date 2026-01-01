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
            <el-table-column label="店铺Logo" width="80">
              <template #default="{ row }">
                <el-avatar :src="row.logo" :size="50" shape="square">
                  <span>{{ row.name?.charAt(0) }}</span>
                </el-avatar>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商户名称" min-width="120" />
            <el-table-column label="联系人" width="120">
              <template #default="{ row }">
                <div>{{ row.contactName || '-' }}</div>
                <div class="text-muted">{{ row.contactPhone }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="经营类目" width="100" />
            <el-table-column label="地址" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.province }}{{ row.city }}{{ row.district }}{{ row.address }}
              </template>
            </el-table-column>
            <el-table-column label="证件状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.businessLicense && row.idCardFront ? 'success' : 'warning'" size="small">
                  {{ row.businessLicense && row.idCardFront ? '已上传' : '不完整' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEntryView(row)">详情</el-button>
                <el-button type="success" link @click="handleEntryApprove(row)">通过</el-button>
                <el-button type="danger" link @click="handleEntryReject(row)">拒绝</el-button>
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

    <!-- 入驻审核详情对话框 -->
    <el-dialog v-model="entryDetailVisible" title="商户入驻申请详情" width="900px">
      <div v-if="entryDetail" class="entry-detail">
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="店铺Logo" :span="2">
            <el-image
              v-if="entryDetail.logo"
              :src="entryDetail.logo"
              :preview-src-list="[entryDetail.logo]"
              style="width: 80px; height: 80px; border-radius: 8px;"
              fit="cover"
            />
            <span v-else class="text-muted">未上传</span>
          </el-descriptions-item>
          <el-descriptions-item label="商户名称">{{ entryDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="经营类目">{{ entryDetail.category || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商户简介" :span="2">{{ entryDetail.description || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 联系信息 -->
        <el-divider content-position="left">联系信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="联系人">{{ entryDetail.contactName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ entryDetail.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="所在地区">
            {{ entryDetail.province }}{{ entryDetail.city }}{{ entryDetail.district }}
          </el-descriptions-item>
          <el-descriptions-item label="详细地址">{{ entryDetail.address }}</el-descriptions-item>
        </el-descriptions>

        <!-- 资质证件 -->
        <el-divider content-position="left">资质证件</el-divider>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="营业执照号">{{ entryDetail.businessLicense || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div class="license-images">
          <div class="license-item">
            <div class="license-label">营业执照照片</div>
            <el-image
              v-if="entryDetail.licenseImage"
              :src="entryDetail.licenseImage"
              :preview-src-list="[entryDetail.licenseImage]"
              class="license-image"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
            <div v-else class="image-placeholder">
              <el-icon><Picture /></el-icon>
              <span>未上传</span>
            </div>
          </div>
          <div class="license-item">
            <div class="license-label">身份证正面</div>
            <el-image
              v-if="entryDetail.idCardFront"
              :src="entryDetail.idCardFront"
              :preview-src-list="[entryDetail.idCardFront]"
              class="license-image"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
            <div v-else class="image-placeholder">
              <el-icon><Picture /></el-icon>
              <span>未上传</span>
            </div>
          </div>
          <div class="license-item">
            <div class="license-label">身份证反面</div>
            <el-image
              v-if="entryDetail.idCardBack"
              :src="entryDetail.idCardBack"
              :preview-src-list="[entryDetail.idCardBack]"
              class="license-image"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
            <div v-else class="image-placeholder">
              <el-icon><Picture /></el-icon>
              <span>未上传</span>
            </div>
          </div>
        </div>

        <!-- 其他信息 -->
        <el-divider content-position="left">其他信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请时间">{{ formatDateTime(entryDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getVerifyStatusType(entryDetail.verifyStatus)">
              {{ getVerifyStatusText(entryDetail.verifyStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="佣金比例" v-if="entryDetail.commissionRate">
            {{ (entryDetail.commissionRate * 100).toFixed(0) }}%
          </el-descriptions-item>
          <el-descriptions-item label="店铺横幅" :span="2" v-if="entryDetail.banner">
            <el-image
              :src="entryDetail.banner"
              :preview-src-list="[entryDetail.banner]"
              style="width: 200px; height: 80px; border-radius: 4px;"
              fit="cover"
            />
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="entryDetailVisible = false">关闭</el-button>
        <el-button type="danger" @click="handleEntryReject(entryDetail)">拒绝</el-button>
        <el-button type="success" @click="handleEntryApprove(entryDetail)">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Right, Picture } from '@element-plus/icons-vue'
import { merchantApi } from '@/api'

const router = useRouter()

const activeTab = ref('entry')
const entryDetailVisible = ref(false)
const entryDetail = ref(null)

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
    entryDetailVisible.value = false
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
    entryDetailVisible.value = false
    fetchEntryData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleEntryView = async (row) => {
  try {
    const res = await merchantApi.getDetail(row.id)
    if (res.code === 200) {
      entryDetail.value = res.data
      entryDetailVisible.value = true
    }
  } catch (e) {
    ElMessage.error('获取商户详情失败')
  }
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 审核状态类型
const getVerifyStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

// 审核状态文字
const getVerifyStatusText = (status) => {
  const texts = { 0: '未提交', 1: '待审核', 2: '已通过', 3: '已拒绝' }
  return texts[status] || '未知'
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
.text-muted {
  color: #909399;
  font-size: 12px;
}
.entry-detail {
  padding: 0 10px;
}
.license-images {
  display: flex;
  gap: 20px;
  margin-top: 16px;
  flex-wrap: wrap;
}
.license-item {
  text-align: center;
}
.license-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.license-image {
  width: 200px;
  height: 130px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
.image-placeholder {
  width: 200px;
  height: 130px;
  background: #f5f7fa;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}
.image-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
</style>
