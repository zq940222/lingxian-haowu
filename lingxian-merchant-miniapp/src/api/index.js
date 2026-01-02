/**
 * 商户端API接口
 */
import { get, post, put, del } from '@/utils/request'

// ============ 认证 ============
export const authApi = {
  // 账号密码登录
  login: (data) => post('/merchant/auth/login', data),
  // 微信登录
  wxLogin: (data) => post('/merchant/auth/wx-login', data),
  // 检查登录状态
  checkLogin: () => get('/merchant/auth/check'),
  // 获取商户信息
  getMerchantInfo: () => get('/merchant/auth/info'),
  // 更新商户信息
  updateMerchantInfo: (data) => put('/merchant/auth/info', data),
  // 提交入驻申请
  applyMerchant: (data) => post('/merchant/auth/apply', data),
  // 更新入驻申请
  updateApply: (data) => put('/merchant/auth/apply', data),
  // 查询申请状态
  getApplyStatus: () => get('/merchant/auth/apply/status'),
  // 刷新token
  refreshToken: (data) => post('/merchant/auth/refresh', data)
}

// ============ 工作台 ============
export const dashboardApi = {
  // 获取工作台数据
  getData: () => get('/merchant/dashboard'),
  // 获取今日统计
  getTodayStats: () => get('/merchant/dashboard/today'),
  // 获取待处理数量
  getPendingCount: () => get('/merchant/dashboard/pending')
}

// ============ 订单 ============
export const orderApi = {
  // 获取订单列表
  getList: (params) => get('/merchant/orders', params),
  // 获取订单详情
  getDetail: (id) => get(`/merchant/orders/${id}`),
  // 接单
  accept: (id) => put(`/merchant/orders/${id}/accept`),
  // 拒单
  reject: (id, reason) => put(`/merchant/orders/${id}/reject`, { reason }),
  // 开始配送
  startDelivery: (id) => put(`/merchant/orders/${id}/delivery`),
  // 完成配送
  completeDelivery: (id) => put(`/merchant/orders/${id}/complete`)
}

// ============ 配送 ============
export const deliveryApi = {
  // 获取待配送列表
  getPending: (params) => get('/merchant/deliveries/pending', params),
  // 获取配送中列表
  getDelivering: (params) => get('/merchant/deliveries/delivering', params),
  // 获取已完成
  getCompleted: (params) => get('/merchant/deliveries/completed', params),
  // 获取配送详情
  getDetail: (id) => get(`/merchant/deliveries/${id}`),
  // 批量配送
  batchDelivery: (ids) => post('/merchant/deliveries/batch', { orderIds: ids })
}

// ============ 商品 ============
export const productApi = {
  // 获取商品列表
  getList: (params) => get('/merchant/products', params),
  // 获取商品详情
  getDetail: (id) => get(`/merchant/products/${id}`),
  // 添加商品
  add: (data) => post('/merchant/products', data),
  // 更新商品
  update: (id, data) => put(`/merchant/products/${id}`, data),
  // 删除商品
  remove: (id) => del(`/merchant/products/${id}`),
  // 上下架
  updateStatus: (id, status) => put(`/merchant/products/${id}/status`, { status }),
  // 更新库存
  updateStock: (id, stock) => put(`/merchant/products/${id}/stock`, { stock })
}

// ============ 分类 ============
export const categoryApi = {
  // 获取分类列表（扁平结构）
  getList: () => get('/merchant/categories'),
  // 获取分类树形结构（用于二级联动选择）
  getTree: () => get('/merchant/categories/tree'),
  // 添加分类
  add: (data) => post('/merchant/categories', data),
  // 更新分类
  update: (id, data) => put(`/merchant/categories/${id}`, data),
  // 删除分类
  remove: (id) => del(`/merchant/categories/${id}`)
}

// ============ 统计 ============
export const statisticsApi = {
  // 销售统计
  getSales: (params) => get('/merchant/statistics/sales', params),
  // 订单统计
  getOrders: (params) => get('/merchant/statistics/orders', params),
  // 商品统计
  getProducts: (params) => get('/merchant/statistics/products', params)
}

// ============ 评价 ============
export const commentApi = {
  // 获取评价列表
  getList: (params) => get('/merchant/comments', params),
  // 获取评价详情
  getDetail: (id) => get(`/merchant/comments/${id}`),
  // 回复评价
  reply: (id, content) => put(`/merchant/comments/${id}/reply`, { content }),
  // 获取评价统计
  getStats: () => get('/merchant/comments/stats')
}

// ============ 店铺设置 ============
export const shopApi = {
  // 获取店铺信息（包含审核状态、本月修改次数）
  getInfo: () => get('/merchant/shop'),
  // 更新店铺信息（需要审核的字段变更会自动提交审核）
  updateInfo: (data) => put('/merchant/shop', data),
  // 更新营业状态（即时生效）
  updateStatus: (status) => put('/merchant/shop/status', { status }),
  // 更新营业时间（即时生效）
  updateHours: (data) => put('/merchant/shop/hours', data),
  // 获取配送设置
  getDeliverySettings: () => get('/merchant/shop/delivery-settings'),
  // 更新配送设置（即时生效）
  updateDeliverySettings: (data) => put('/merchant/shop/delivery-settings', data)
}

// ============ 配送小区管理 ============
export const communityApi = {
  // 获取所有小区列表（含配送状态）
  getList: () => get('/merchant/community/list'),
  // 获取我的配送小区
  getMyCommunities: () => get('/merchant/community/my'),
  // 获取当前开放的配送小区
  getEnabled: () => get('/merchant/community/enabled'),
  // 添加配送小区
  add: (communityId) => post('/merchant/community/add', { communityId }),
  // 删除配送小区
  remove: (configId) => del(`/merchant/community/${configId}`),
  // 切换配送小区状态
  toggle: (configId, enabled) => put(`/merchant/community/${configId}/toggle`, { enabled }),
  // 批量切换状态
  batchToggle: (configIds, enabled) => put('/merchant/community/batch-toggle', { configIds, enabled })
}

// ============ 用户/账号 ============
export const userApi = {
  // 获取账号信息
  getAccountInfo: () => get('/merchant/account'),
  // 更新登录验证设置
  updateLoginVerify: (enabled) => put('/merchant/account/login-verify', { enabled }),
  // 移除设备
  removeDevice: (deviceId) => del(`/merchant/account/devices/${deviceId}`)
}

// ============ 打印机 ============
export const printerApi = {
  // 获取打印机列表
  getList: () => get('/merchant/printers'),
  // 添加打印机
  add: (data) => post('/merchant/printers', data),
  // 删除打印机
  delete: (id) => del(`/merchant/printers/${id}`),
  // 测试打印
  test: (id) => post(`/merchant/printers/${id}/test`),
  // 获取打印设置
  getSettings: () => get('/merchant/printers/settings'),
  // 更新打印设置
  updateSettings: (data) => put('/merchant/printers/settings', data)
}

// ============ 通知设置 ============
export const notificationApi = {
  // 获取通知设置
  getSettings: () => get('/merchant/notification/settings'),
  // 更新通知设置
  updateSettings: (data) => put('/merchant/notification/settings', data)
}

// ============ 钱包/提现 ============
export const walletApi = {
  // 获取钱包余额
  getBalance: () => get('/merchant/wallet/balance'),
  // 获取钱包明细
  getRecords: (params) => get('/merchant/wallet/records', params),
  // 获取提现账户列表
  getAccounts: () => get('/merchant/wallet/accounts'),
  // 添加提现账户
  addAccount: (data) => post('/merchant/wallet/accounts', data),
  // 删除提现账户
  removeAccount: (id) => del(`/merchant/wallet/accounts/${id}`),
  // 设置默认提现账户
  setDefaultAccount: (id) => put(`/merchant/wallet/accounts/${id}/default`),
  // 发起提现
  withdraw: (data) => post('/merchant/wallet/withdraw', data),
  // 获取提现记录
  getWithdrawRecords: (params) => get('/merchant/wallet/withdraw/records', params),
  // 获取提现详情
  getWithdrawDetail: (id) => get(`/merchant/wallet/withdraw/${id}`)
}

// ============ 文件上传 ============
export const uploadApi = {
  // 上传图片（返回 Promise）
  uploadImage: (filePath) => {
    return new Promise((resolve, reject) => {
      // 获取上传基础URL，需要包含 /api 前缀
      let baseUrl
      // #ifdef H5
      baseUrl = '/api'
      // #endif
      // #ifndef H5
      baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8086/api'
      // #endif
      const token = uni.getStorageSync('merchant_token')

      uni.uploadFile({
        url: `${baseUrl}/merchant/upload/image`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data)
              if (data.code === 200) {
                resolve(data.data)
              } else {
                reject(new Error(data.message || '上传失败'))
              }
            } catch (e) {
              reject(new Error('解析响应失败'))
            }
          } else {
            reject(new Error(`上传失败: ${res.statusCode}`))
          }
        },
        fail: (err) => {
          reject(new Error(err.errMsg || '上传失败'))
        }
      })
    })
  }
}
