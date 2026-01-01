<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>拼团记录</span>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="searchForm.groupNo" placeholder="拼团编号" clearable style="width: 200px" @clear="fetchData" @keyup.enter="fetchData" />
        <el-select v-model="searchForm.status" placeholder="拼团状态" clearable style="width: 150px" @change="fetchData">
          <el-option :value="0" label="拼团中" />
          <el-option :value="1" label="拼团成功" />
          <el-option :value="2" label="拼团失败" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="groupNo" label="拼团编号" width="180" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image v-if="row.productImage" :src="row.productImage" :preview-src-list="[row.productImage]" style="width: 60px; height: 60px" fit="cover" />
            <span v-else class="no-image">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="activityName" label="活动名称" min-width="150" />
        <el-table-column label="团长" width="150">
          <template #default="{ row }">
            <div class="leader-info">
              <el-avatar :src="row.leaderAvatar" :size="32" />
              <span class="leader-name">{{ row.leaderNickname || '用户' + row.leaderId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="人数" width="100">
          <template #default="{ row }">
            {{ row.currentSize }}/{{ row.groupSize }}
          </template>
        </el-table-column>
        <el-table-column prop="groupPrice" label="拼团价" width="100">
          <template #default="{ row }">¥{{ row.groupPrice }}</template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypes[row.status]">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.expireTime) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="拼团详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="拼团ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="拼团编号">{{ currentRecord.groupNo }}</el-descriptions-item>
        <el-descriptions-item label="活动名称">{{ currentRecord.activityName }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentRecord.productName }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">¥{{ currentRecord.groupPrice }}</el-descriptions-item>
        <el-descriptions-item label="成团人数">{{ currentRecord.groupSize }}人</el-descriptions-item>
        <el-descriptions-item label="当前人数">{{ currentRecord.currentSize }}人</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTypes[currentRecord.status]">{{ currentRecord.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开团时间">{{ formatDateTime(currentRecord.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ formatDateTime(currentRecord.expireTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRecord.completeTime" label="完成时间" :span="2">
          {{ formatDateTime(currentRecord.completeTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 团长信息 -->
      <div class="section-title">团长信息</div>
      <div class="leader-detail">
        <el-avatar :src="currentRecord.leaderAvatar" :size="48" />
        <div class="leader-info-detail">
          <div class="leader-nickname">{{ currentRecord.leaderNickname || '用户' + currentRecord.leaderId }}</div>
          <div class="leader-id">ID: {{ currentRecord.leaderId }}</div>
        </div>
      </div>

      <!-- 参团成员 -->
      <div class="section-title">参团成员 ({{ currentRecord.members?.length || 0 }}人)</div>
      <el-table :data="currentRecord.members || []" stripe size="small" max-height="300">
        <el-table-column label="用户" width="200">
          <template #default="{ row }">
            <div class="member-info">
              <el-avatar :src="row.avatar" :size="28" />
              <span>{{ row.nickname || '用户' + row.userId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="参团时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="身份" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isLeader === 1 ? 'warning' : ''">{{ row.isLeader === 1 ? '团长' : '团员' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { groupApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ groupNo: '', status: null })
const detailDialogVisible = ref(false)
const currentRecord = reactive({
  id: null,
  groupNo: '',
  activityName: '',
  productName: '',
  productImage: '',
  leaderId: null,
  leaderNickname: '',
  leaderAvatar: '',
  groupSize: 0,
  currentSize: 0,
  groupPrice: 0,
  status: 0,
  statusName: '',
  createTime: '',
  expireTime: '',
  completeTime: '',
  members: []
})

const statusTypes = ['warning', 'success', 'danger']

const fetchData = async () => {
  loading.value = true
  try {
    const res = await groupApi.getRecords({
      page: pagination.page,
      size: pagination.pageSize,
      groupNo: searchForm.groupNo || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error('获取拼团记录列表失败:', e)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.groupNo = ''
  searchForm.status = null
  pagination.page = 1
  fetchData()
}

const handleDetail = (row) => {
  Object.assign(currentRecord, row)
  detailDialogVisible.value = true
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}

onMounted(() => fetchData())
</script>

<style scoped>
.page-container { padding: 20px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
.no-image { color: #999; font-size: 12px; }

.leader-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.leader-name {
  font-size: 13px;
}
.section-title {
  margin: 20px 0 12px;
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}
.leader-detail {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.leader-info-detail {
  flex: 1;
}
.leader-nickname {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.leader-id {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.member-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
