<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>系统配置</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="平台名称">
              <el-input v-model="basicForm.siteName" />
            </el-form-item>
            <el-form-item label="客服电话">
              <el-input v-model="basicForm.servicePhone" />
            </el-form-item>
            <el-form-item label="关于我们">
              <el-input v-model="basicForm.about" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveBasic">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="配送配置" name="delivery">
          <el-form :model="deliveryForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="默认配送费">
              <el-input-number v-model="deliveryForm.defaultFee" :precision="2" :min="0" />
              <span class="form-tip">元</span>
            </el-form-item>
            <el-form-item label="免配送费金额">
              <el-input-number v-model="deliveryForm.freeAmount" :precision="2" :min="0" />
              <span class="form-tip">元，订单满此金额免配送费</span>
            </el-form-item>
            <el-form-item label="最大配送距离">
              <el-input-number v-model="deliveryForm.maxDistance" :min="1" />
              <span class="form-tip">公里</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveDelivery">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

const basicForm = reactive({
  siteName: '铃鲜好物',
  servicePhone: '',
  about: ''
})

const deliveryForm = reactive({
  defaultFee: 3,
  freeAmount: 30,
  maxDistance: 5
})

const handleSaveBasic = () => {
  ElMessage.success('保存成功')
}

const handleSaveDelivery = () => {
  ElMessage.success('保存成功')
}
</script>

<style scoped>
.page-container { padding: 20px; }
.form-tip { margin-left: 10px; color: #909399; font-size: 12px; }
</style>
