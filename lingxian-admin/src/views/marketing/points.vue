<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>积分配置</span>
      </template>

      <el-form :model="form" label-width="150px" style="max-width: 600px">
        <el-form-item label="每日签到积分">
          <el-input-number v-model="form.signInDaily" :min="0" />
          <span class="form-tip">用户每日签到可获得的积分</span>
        </el-form-item>
        <el-form-item label="订单积分比例">
          <el-input-number v-model="form.orderRate" :min="0" :precision="2" />
          <span class="form-tip">每消费1元可获得的积分</span>
        </el-form-item>
        <el-form-item label="邀请新用户奖励">
          <el-input-number v-model="form.inviteReward" :min="0" />
          <span class="form-tip">邀请新用户注册可获得的积分</span>
        </el-form-item>
        <el-form-item label="积分兑换比例">
          <el-input-number v-model="form.exchangeRate" :min="1" />
          <span class="form-tip">多少积分可兑换1元</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  signInDaily: 5,
  orderRate: 1,
  inviteReward: 100,
  exchangeRate: 100
})

const fetchConfig = async () => {
  // TODO: 获取配置
}

const handleSave = () => {
  ElMessage.success('保存成功')
}

onMounted(() => fetchConfig())
</script>

<style scoped>
.page-container { padding: 20px; }
.form-tip { margin-left: 10px; color: #909399; font-size: 12px; }
</style>
