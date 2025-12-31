<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-descriptions title="订单信息" :column="2" border>
        <el-descriptions-item label="订单号">{{ orderInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag>{{ orderInfo.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ orderInfo.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ orderInfo.paidAt }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions title="收货信息" :column="2" border style="margin-top: 20px">
        <el-descriptions-item label="收货人">{{ orderInfo.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ orderInfo.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ orderInfo.receiverAddress }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions title="金额信息" :column="2" border style="margin-top: 20px">
        <el-descriptions-item label="商品金额">¥{{ orderInfo.productAmount }}</el-descriptions-item>
        <el-descriptions-item label="配送费">¥{{ orderInfo.deliveryFee }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ orderInfo.totalAmount }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const orderInfo = ref({})

onMounted(() => {
  // TODO: 根据 route.params.id 获取订单详情
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
