import request from './request'

// ============ 用户管理 ============
export const userApi = {
  getList: (params) => request({ url: '/admin/users', method: 'get', params }),
  getDetail: (id) => request({ url: `/admin/users/${id}`, method: 'get' }),
  update: (id, data) => request({ url: `/admin/users/${id}`, method: 'put', data }),
  updateStatus: (id, status) => request({ url: `/admin/users/${id}/status`, method: 'put', data: { status } }),
  getPointsRecords: (userId, params) => request({ url: `/admin/users/${userId}/points`, method: 'get', params })
}

// ============ 商户管理 ============
export const merchantApi = {
  getList: (params) => request({ url: '/admin/merchants', method: 'get', params }),
  getDetail: (id) => request({ url: `/admin/merchants/${id}`, method: 'get' }),
  update: (id, data) => request({ url: `/admin/merchants/${id}`, method: 'put', data }),
  updateStatus: (id, status) => request({ url: `/admin/merchants/${id}/status`, method: 'put', data: { status } }),
  verify: (id, data) => request({ url: `/admin/merchants/${id}/verify`, method: 'put', data }),
  getPendingList: (params) => request({ url: '/admin/merchants/pending', method: 'get', params })
}

// ============ 商品管理 ============
export const productApi = {
  getList: (params) => request({ url: '/admin/products', method: 'get', params }),
  getDetail: (id) => request({ url: `/admin/products/${id}`, method: 'get' }),
  updateStatus: (id, status) => request({ url: `/admin/products/${id}/status`, method: 'put', data: { status } }),
  delete: (id) => request({ url: `/admin/products/${id}`, method: 'delete' })
}

// ============ 分类管理 ============
export const categoryApi = {
  getList: (params) => request({ url: '/admin/categories', method: 'get', params }),
  getTree: () => request({ url: '/admin/categories/tree', method: 'get' }),
  create: (data) => request({ url: '/admin/categories', method: 'post', data }),
  update: (id, data) => request({ url: `/admin/categories/${id}`, method: 'put', data }),
  delete: (id) => request({ url: `/admin/categories/${id}`, method: 'delete' }),
  updateSort: (data) => request({ url: '/admin/categories/sort', method: 'put', data })
}

// ============ 订单管理 ============
export const orderApi = {
  getList: (params) => request({ url: '/admin/orders', method: 'get', params }),
  getDetail: (id) => request({ url: `/admin/orders/${id}`, method: 'get' }),
  getStats: (params) => request({ url: '/admin/orders/stats', method: 'get', params }),
  export: (params) => request({ url: '/admin/orders/export', method: 'get', params, responseType: 'blob' })
}

// ============ 退款管理 ============
export const refundApi = {
  getList: (params) => request({ url: '/admin/refunds', method: 'get', params }),
  getDetail: (id) => request({ url: `/admin/refunds/${id}`, method: 'get' }),
  approve: (id, data) => request({ url: `/admin/refunds/${id}/approve`, method: 'put', data }),
  reject: (id, data) => request({ url: `/admin/refunds/${id}/reject`, method: 'put', data })
}

// ============ 拼团管理 ============
export const groupApi = {
  getActivities: (params) => request({ url: '/admin/groups/activities', method: 'get', params }),
  getActivityDetail: (id) => request({ url: `/admin/groups/activities/${id}`, method: 'get' }),
  createActivity: (data) => request({ url: '/admin/groups/activities', method: 'post', data }),
  updateActivity: (id, data) => request({ url: `/admin/groups/activities/${id}`, method: 'put', data }),
  updateActivityStatus: (id, status) => request({ url: `/admin/groups/activities/${id}/status`, method: 'put', data: { status } }),
  getRecords: (params) => request({ url: '/admin/groups/records', method: 'get', params }),
  getRecordDetail: (id) => request({ url: `/admin/groups/records/${id}`, method: 'get' })
}

// ============ 轮播图管理 ============
export const bannerApi = {
  getList: (params) => request({ url: '/admin/banners', method: 'get', params }),
  create: (data) => request({ url: '/admin/banners', method: 'post', data }),
  update: (id, data) => request({ url: `/admin/banners/${id}`, method: 'put', data }),
  delete: (id) => request({ url: `/admin/banners/${id}`, method: 'delete' }),
  updateSort: (data) => request({ url: '/admin/banners/sort', method: 'put', data })
}

// ============ 系统管理 ============
export const systemApi = {
  // 管理员
  getAdmins: (params) => request({ url: '/admin/system/admins', method: 'get', params }),
  createAdmin: (data) => request({ url: '/admin/system/admins', method: 'post', data }),
  updateAdmin: (id, data) => request({ url: `/admin/system/admins/${id}`, method: 'put', data }),
  deleteAdmin: (id) => request({ url: `/admin/system/admins/${id}`, method: 'delete' }),
  resetAdminPassword: (id) => request({ url: `/admin/system/admins/${id}/reset-password`, method: 'put' }),
  // 角色
  getRoles: () => request({ url: '/admin/system/roles', method: 'get' }),
  createRole: (data) => request({ url: '/admin/system/roles', method: 'post', data }),
  updateRole: (id, data) => request({ url: `/admin/system/roles/${id}`, method: 'put', data }),
  deleteRole: (id) => request({ url: `/admin/system/roles/${id}`, method: 'delete' }),
  // 权限
  getPermissions: () => request({ url: '/admin/system/permissions', method: 'get' }),
  // 系统配置
  getConfigs: () => request({ url: '/admin/system/configs', method: 'get' }),
  updateConfigs: (data) => request({ url: '/admin/system/configs', method: 'put', data }),
  // 存储测试
  testStorage: (data) => request({ url: '/admin/system/storage/test', method: 'post', data })
}

// ============ 统计数据 ============
export const statisticsApi = {
  getDashboard: () => request({ url: '/admin/dashboard', method: 'get' }),
  getSalesStats: (params) => request({ url: '/admin/statistics/sales', method: 'get', params }),
  getOrderStats: (params) => request({ url: '/admin/statistics/orders', method: 'get', params }),
  getUserStats: (params) => request({ url: '/admin/statistics/users', method: 'get', params }),
  getMerchantStats: (params) => request({ url: '/admin/statistics/merchants', method: 'get', params })
}

// ============ 文件上传 ============
export const uploadApi = {
  uploadImage: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/admin/upload/image',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
