<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="icon" style="background-color: #1890ff;">
          <el-icon><User /></el-icon>
        </div>
        <div class="content">
          <div class="title">用户总数</div>
          <div class="value">{{ stats.userCount || 0 }}</div>
          <div class="trend up">较昨日 +{{ stats.userGrowth || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="icon" style="background-color: #52c41a;">
          <el-icon><Shop /></el-icon>
        </div>
        <div class="content">
          <div class="title">商户总数</div>
          <div class="value">{{ stats.merchantCount || 0 }}</div>
          <div class="trend up">较昨日 +{{ stats.merchantGrowth || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="icon" style="background-color: #faad14;">
          <el-icon><List /></el-icon>
        </div>
        <div class="content">
          <div class="title">今日订单</div>
          <div class="value">{{ stats.todayOrders || 0 }}</div>
          <div class="trend up">较昨日 +{{ stats.orderGrowth || 0 }}%</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="icon" style="background-color: #ff4d4f;">
          <el-icon><Money /></el-icon>
        </div>
        <div class="content">
          <div class="title">今日销售额</div>
          <div class="value">¥{{ stats.todaySales || '0.00' }}</div>
          <div class="trend up">较昨日 +{{ stats.salesGrowth || 0 }}%</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <el-col :span="16">
        <div class="chart-card">
          <div class="card-header">
            <span class="title">销售趋势</span>
            <el-radio-group v-model="chartType" size="small">
              <el-radio-button label="week">近7天</el-radio-button>
              <el-radio-button label="month">近30天</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="salesChartRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <span class="title">订单状态分布</span>
          </div>
          <div ref="orderChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <div class="todo-card">
          <div class="card-header">
            <span class="title">待处理事项</span>
          </div>
          <div class="todo-list">
            <div class="todo-item" @click="goTo('/merchant/verify')">
              <span class="label">待审核商户</span>
              <el-badge :value="todos.pendingMerchants" :max="99" />
            </div>
            <div class="todo-item" @click="goTo('/order/refund')">
              <span class="label">待处理退款</span>
              <el-badge :value="todos.pendingRefunds" :max="99" />
            </div>
            <div class="todo-item" @click="goTo('/order/list?status=4')">
              <span class="label">异常订单</span>
              <el-badge :value="todos.abnormalOrders" :max="99" />
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="todo-card">
          <div class="card-header">
            <span class="title">最新订单</span>
            <el-link type="primary" @click="goTo('/order/list')">查看全部</el-link>
          </div>
          <el-table :data="recentOrders" size="small" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="160" />
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="payAmount" label="金额">
              <template #default="{ row }">
                ¥{{ row.payAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api'

const router = useRouter()

const stats = reactive({
  userCount: 0,
  userGrowth: 0,
  merchantCount: 0,
  merchantGrowth: 0,
  todayOrders: 0,
  orderGrowth: 0,
  todaySales: '0.00',
  salesGrowth: 0
})

const todos = reactive({
  pendingMerchants: 0,
  pendingRefunds: 0,
  abnormalOrders: 0
})

const recentOrders = ref([])
const chartType = ref('week')
const salesChartRef = ref()
const orderChartRef = ref()

let salesChart = null
let orderChart = null

// 加载数据
const loadData = async () => {
  try {
    const res = await statisticsApi.getDashboard()
    if (res.code === 200) {
      Object.assign(stats, res.data.stats)
      Object.assign(todos, res.data.todos)
      recentOrders.value = res.data.recentOrders || []
    }
  } catch (e) {
    // 使用模拟数据
    stats.userCount = 12580
    stats.merchantCount = 156
    stats.todayOrders = 328
    stats.todaySales = '28,650.00'
    todos.pendingMerchants = 5
    todos.pendingRefunds = 3
  }
}

// 初始化销售趋势图
const initSalesChart = () => {
  if (salesChart) salesChart.dispose()
  salesChart = echarts.init(salesChartRef.value)

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'] },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: [
      { type: 'value', name: '销售额(元)' },
      { type: 'value', name: '订单数' }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: [3200, 4500, 3800, 5200, 4800, 6500, 5800]
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: [45, 62, 53, 78, 65, 92, 85]
      }
    ]
  }
  salesChart.setOption(option)
}

// 初始化订单状态图
const initOrderChart = () => {
  if (orderChart) orderChart.dispose()
  orderChart = echarts.init(orderChartRef.value)

  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        label: { show: false },
        data: [
          { value: 45, name: '待支付' },
          { value: 120, name: '已支付' },
          { value: 80, name: '配送中' },
          { value: 200, name: '已完成' },
          { value: 15, name: '已取消' }
        ]
      }
    ]
  }
  orderChart.setOption(option)
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '已支付',
    2: '配送中',
    3: '已完成',
    4: '已取消'
  }
  return texts[status] || '未知'
}

// 跳转
const goTo = (path) => {
  router.push(path)
}

onMounted(() => {
  loadData()
  initSalesChart()
  initOrderChart()

  window.addEventListener('resize', () => {
    salesChart?.resize()
    orderChart?.resize()
  })
})

watch(chartType, () => {
  initSalesChart()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 16px;

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background-color: #fff;
      border-radius: 4px;

      .icon {
        width: 56px;
        height: 56px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;

        .el-icon {
          font-size: 28px;
          color: #fff;
        }
      }

      .content {
        .title {
          font-size: 14px;
          color: #666;
        }
        .value {
          font-size: 28px;
          font-weight: 600;
          color: #333;
          margin: 8px 0;
        }
        .trend {
          font-size: 12px;
          &.up { color: #52c41a; }
          &.down { color: #ff4d4f; }
        }
      }
    }
  }

  .chart-card, .todo-card {
    background-color: #fff;
    border-radius: 4px;
    padding: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
    }

    .chart {
      height: 300px;
    }
  }

  .todo-list {
    .todo-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;

      &:hover {
        background-color: #fafafa;
      }

      &:last-child {
        border-bottom: none;
      }

      .label {
        font-size: 14px;
        color: #333;
      }
    }
  }
}
</style>
