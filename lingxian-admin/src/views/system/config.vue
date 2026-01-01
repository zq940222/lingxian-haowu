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

        <el-tab-pane label="存储配置" name="storage">
          <el-form :model="storageForm" label-width="120px" style="max-width: 700px">
            <el-form-item label="存储类型">
              <el-radio-group v-model="storageForm.type" @change="handleStorageTypeChange">
                <el-radio value="local">本地存储</el-radio>
                <el-radio value="minio">MinIO</el-radio>
                <el-radio value="aliyun">阿里云 OSS</el-radio>
                <el-radio value="huawei">华为云 OBS</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- MinIO 配置 -->
            <template v-if="storageForm.type === 'minio'">
              <el-divider content-position="left">MinIO 配置</el-divider>
              <el-form-item label="Endpoint">
                <el-input v-model="storageForm.minio.endpoint" placeholder="如：http://localhost:9000" />
              </el-form-item>
              <el-form-item label="Access Key">
                <el-input v-model="storageForm.minio.accessKey" placeholder="请输入 Access Key" />
              </el-form-item>
              <el-form-item label="Secret Key">
                <el-input v-model="storageForm.minio.secretKey" type="password" show-password placeholder="请输入 Secret Key" />
              </el-form-item>
              <el-form-item label="Bucket 名称">
                <el-input v-model="storageForm.minio.bucketName" placeholder="请输入 Bucket 名称" />
              </el-form-item>
              <el-form-item label="访问域名">
                <el-input v-model="storageForm.minio.domain" placeholder="可选，用于 CDN 加速" />
                <div class="form-tip">不填则使用 Endpoint 作为访问域名</div>
              </el-form-item>
              <el-form-item label="存储目录">
                <el-input v-model="storageForm.minio.prefix" placeholder="如：uploads/" />
                <div class="form-tip">文件上传的目录前缀，以 / 结尾</div>
              </el-form-item>
            </template>

            <!-- 阿里云 OSS 配置 -->
            <template v-if="storageForm.type === 'aliyun'">
              <el-divider content-position="left">阿里云 OSS 配置</el-divider>
              <el-form-item label="Endpoint">
                <el-input v-model="storageForm.aliyun.endpoint" placeholder="如：oss-cn-hangzhou.aliyuncs.com" />
              </el-form-item>
              <el-form-item label="AccessKey ID">
                <el-input v-model="storageForm.aliyun.accessKeyId" placeholder="请输入 AccessKey ID" />
              </el-form-item>
              <el-form-item label="AccessKey Secret">
                <el-input v-model="storageForm.aliyun.accessKeySecret" type="password" show-password placeholder="请输入 AccessKey Secret" />
              </el-form-item>
              <el-form-item label="Bucket 名称">
                <el-input v-model="storageForm.aliyun.bucketName" placeholder="请输入 Bucket 名称" />
              </el-form-item>
              <el-form-item label="访问域名">
                <el-input v-model="storageForm.aliyun.domain" placeholder="如：https://your-bucket.oss-cn-hangzhou.aliyuncs.com" />
                <div class="form-tip">用于拼接文件访问地址，可使用 CDN 域名</div>
              </el-form-item>
              <el-form-item label="存储目录">
                <el-input v-model="storageForm.aliyun.prefix" placeholder="如：uploads/" />
                <div class="form-tip">文件上传的目录前缀，以 / 结尾</div>
              </el-form-item>
            </template>

            <!-- 华为云 OBS 配置 -->
            <template v-if="storageForm.type === 'huawei'">
              <el-divider content-position="left">华为云 OBS 配置</el-divider>
              <el-form-item label="Endpoint">
                <el-input v-model="storageForm.huawei.endpoint" placeholder="如：obs.cn-north-4.myhuaweicloud.com" />
              </el-form-item>
              <el-form-item label="Access Key ID">
                <el-input v-model="storageForm.huawei.accessKeyId" placeholder="请输入 Access Key ID" />
              </el-form-item>
              <el-form-item label="Secret Access Key">
                <el-input v-model="storageForm.huawei.secretAccessKey" type="password" show-password placeholder="请输入 Secret Access Key" />
              </el-form-item>
              <el-form-item label="Bucket 名称">
                <el-input v-model="storageForm.huawei.bucketName" placeholder="请输入 Bucket 名称" />
              </el-form-item>
              <el-form-item label="访问域名">
                <el-input v-model="storageForm.huawei.domain" placeholder="如：https://your-bucket.obs.cn-north-4.myhuaweicloud.com" />
                <div class="form-tip">用于拼接文件访问地址，可使用 CDN 域名</div>
              </el-form-item>
              <el-form-item label="存储目录">
                <el-input v-model="storageForm.huawei.prefix" placeholder="如：uploads/" />
                <div class="form-tip">文件上传的目录前缀，以 / 结尾</div>
              </el-form-item>
            </template>

            <!-- 本地存储配置 -->
            <template v-if="storageForm.type === 'local'">
              <el-divider content-position="left">本地存储配置</el-divider>
              <el-form-item label="访问域名">
                <el-input v-model="storageForm.local.domain" placeholder="如：http://localhost:8087" />
                <div class="form-tip">本地文件的访问地址前缀</div>
              </el-form-item>
              <el-form-item label="存储路径">
                <el-input v-model="storageForm.local.path" placeholder="如：/data/uploads" />
                <div class="form-tip">服务器上的文件存储路径</div>
              </el-form-item>
            </template>

            <el-form-item>
              <el-button type="primary" @click="handleSaveStorage" :loading="savingStorage">保存配置</el-button>
              <el-button @click="handleTestStorage" :loading="testingStorage">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '@/api'

const activeTab = ref('basic')
const loading = ref(false)

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

// 存储配置
const storageForm = reactive({
  type: 'local',
  minio: {
    endpoint: 'http://localhost:9000',
    accessKey: '',
    secretKey: '',
    bucketName: 'lingxian',
    domain: '',
    prefix: 'uploads/'
  },
  aliyun: {
    endpoint: '',
    accessKeyId: '',
    accessKeySecret: '',
    bucketName: '',
    domain: '',
    prefix: 'uploads/'
  },
  huawei: {
    endpoint: '',
    accessKeyId: '',
    secretAccessKey: '',
    bucketName: '',
    domain: '',
    prefix: 'uploads/'
  },
  local: {
    domain: 'http://localhost:8087',
    path: '/data/uploads'
  }
})
const savingStorage = ref(false)
const testingStorage = ref(false)

const handleStorageTypeChange = () => {
  // 切换存储类型时的处理
}

const fetchConfigs = async () => {
  loading.value = true
  try {
    const res = await systemApi.getConfigs()
    if (res.code === 200 && res.data) {
      const { basic, delivery, storage } = res.data
      if (basic) {
        basicForm.siteName = basic.siteName || '铃鲜好物'
        basicForm.servicePhone = basic.contactPhone || ''
        basicForm.about = basic.siteDescription || ''
      }
      if (delivery) {
        deliveryForm.defaultFee = delivery.defaultFreight || 3
        deliveryForm.freeAmount = delivery.freeShippingAmount || 30
      }
      if (storage) {
        storageForm.type = storage.type || 'local'
        if (storage.minio) {
          Object.assign(storageForm.minio, storage.minio)
        }
        if (storage.aliyun) {
          Object.assign(storageForm.aliyun, storage.aliyun)
        }
        if (storage.huawei) {
          Object.assign(storageForm.huawei, storage.huawei)
        }
        if (storage.local) {
          Object.assign(storageForm.local, storage.local)
        }
      }
    }
  } catch (e) {
    console.error('获取系统配置失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSaveBasic = async () => {
  try {
    await systemApi.updateConfigs({
      basic: {
        siteName: basicForm.siteName,
        contactPhone: basicForm.servicePhone,
        siteDescription: basicForm.about
      }
    })
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleSaveDelivery = async () => {
  try {
    await systemApi.updateConfigs({
      delivery: {
        defaultFreight: deliveryForm.defaultFee,
        freeShippingAmount: deliveryForm.freeAmount
      }
    })
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleSaveStorage = async () => {
  savingStorage.value = true
  try {
    const storageConfig = {
      type: storageForm.type
    }
    // 根据类型只保存对应的配置
    if (storageForm.type === 'minio') {
      storageConfig.minio = { ...storageForm.minio }
    } else if (storageForm.type === 'aliyun') {
      storageConfig.aliyun = { ...storageForm.aliyun }
    } else if (storageForm.type === 'huawei') {
      storageConfig.huawei = { ...storageForm.huawei }
    } else {
      storageConfig.local = { ...storageForm.local }
    }
    await systemApi.updateConfigs({ storage: storageConfig })
    ElMessage.success('存储配置保存成功')
  } catch (e) {
    ElMessage.error('存储配置保存失败')
  } finally {
    savingStorage.value = false
  }
}

const handleTestStorage = async () => {
  testingStorage.value = true
  try {
    const res = await systemApi.testStorage({ type: storageForm.type })
    if (res.code === 200) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败，请检查配置')
  } finally {
    testingStorage.value = false
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.form-tip { margin-left: 10px; color: #909399; font-size: 12px; }
</style>
