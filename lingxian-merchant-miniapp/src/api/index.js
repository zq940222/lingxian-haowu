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
  // 获取分类列表
  getList: () => get('/merchant/categories'),
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

// ============ 店铺设置 ============
export const shopApi = {
  // 获取店铺信息
  getInfo: () => get('/merchant/shop'),
  // 更新店铺信息
  updateInfo: (data) => put('/merchant/shop', data),
  // 更新营业状态
  updateStatus: (status) => put('/merchant/shop/status', { status }),
  // 更新营业时间
  updateHours: (data) => put('/merchant/shop/hours', data),
  // 获取配送设置
  getDeliverySettings: () => get('/merchant/shop/delivery-settings'),
  // 更新配送设置
  updateDeliverySettings: (data) => put('/merchant/shop/delivery-settings', data)
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
