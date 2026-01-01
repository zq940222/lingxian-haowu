/**
 * API接口统一管理
 */
import { get, post, put, del } from '@/utils/request'

// ============ 用户认证 ============
export const authApi = {
  // 微信登录
  login: (data) => post('/user/auth/login', data),
  // 获取手机号
  getPhoneNumber: (data) => post('/user/auth/phone', data),
  // 获取用户信息
  getUserInfo: () => get('/user/info'),
  // 更新用户信息
  updateUserInfo: (data) => put('/user/info', data)
}

// ============ 首页数据 ============
export const homeApi = {
  // 获取首页数据
  getHomeData: () => get('/user/home'),
  // 获取轮播图
  getBanners: () => get('/user/banners'),
  // 获取推荐商品
  getRecommendProducts: (params) => get('/user/products/recommend', params),
  // 获取限时抢购商品
  getFlashSaleProducts: (params) => get('/user/flash-sale/products', params),
  // 搜索
  search: (params) => get('/user/search', params)
}

// ============ 分类 ============
export const categoryApi = {
  // 获取分类列表
  getCategories: () => get('/user/categories'),
  // 获取分类下商品
  getCategoryProducts: (categoryId, params) => get(`/user/categories/${categoryId}/products`, params)
}

// ============ 商品 ============
export const productApi = {
  // 获取商品详情
  getDetail: (id) => get(`/user/products/${id}`),
  // 获取商品列表
  getList: (params) => get('/user/products', params),
  // 获取商品评价列表
  getComments: (id, params) => get(`/user/products/${id}/comments`, params)
}

// ============ 购物车 ============
export const cartApi = {
  // 获取购物车
  getList: () => get('/user/cart'),
  // 添加到购物车
  add: (data) => post('/user/cart', data),
  // 更新数量
  updateQuantity: (id, quantity) => put(`/user/cart/${id}`, { quantity }),
  // 删除
  remove: (id) => del(`/user/cart/${id}`),
  // 清空
  clear: () => del('/user/cart'),
  // 选中/取消
  select: (id, selected) => put(`/user/cart/${id}/select`, { selected }),
  // 全选/取消全选
  selectAll: (selected) => put('/user/cart/select-all', { selected }),
  // 选中/取消选中某商户的所有商品
  selectMerchant: (merchantId, selected) => put(`/user/cart/merchant/${merchantId}/select`, { selected })
}

// ============ 订单 ============
export const orderApi = {
  // 创建订单
  create: (data) => post('/user/orders', data),
  // 获取订单列表
  getList: (params) => get('/user/orders', params),
  // 获取订单详情
  getDetail: (id) => get(`/user/orders/${id}`),
  // 取消订单
  cancel: (id, reason) => put(`/user/orders/${id}/cancel`, { reason }),
  // 确认收货
  confirm: (id) => put(`/user/orders/${id}/confirm`),
  // 支付订单
  pay: (id) => post(`/user/orders/${id}/pay`),
  // 删除订单
  remove: (id) => del(`/user/orders/${id}`),
  // 评价订单
  comment: (id, data) => post(`/user/orders/${id}/comment`, data),
  // 再来一单
  reorder: (id) => post(`/user/orders/${id}/reorder`)
}

// ============ 地址 ============
export const addressApi = {
  // 获取地址列表
  getList: () => get('/user/addresses'),
  // 获取地址详情
  getDetail: (id) => get(`/user/addresses/${id}`),
  // 获取默认地址
  getDefault: () => get('/user/addresses/default'),
  // 添加地址
  add: (data) => post('/user/addresses', data),
  // 更新地址
  update: (id, data) => put(`/user/addresses/${id}`, data),
  // 删除地址
  remove: (id) => del(`/user/addresses/${id}`),
  // 设为默认
  setDefault: (id) => put(`/user/addresses/${id}/default`)
}

// ============ 拼团 ============
export const groupApi = {
  // 获取拼团活动列表
  getActivities: (params) => get('/user/groups/activities', params),
  // 获取活动详情
  getActivityDetail: (id) => get(`/user/groups/activities/${id}`),
  // 获取进行中的拼团
  getOngoingGroups: (activityId) => get(`/user/groups/activities/${activityId}/records`),
  // 发起拼团
  start: (data) => post('/user/groups/start', data),
  // 参与拼团
  join: (data) => post('/user/groups/join', data),
  // 获取我的拼团
  getMyGroups: (params) => get('/user/groups/my', params),
  // 获取拼团详情
  getGroupDetail: (id) => get(`/user/groups/records/${id}`)
}

// ============ 积分 ============
export const pointsApi = {
  // 签到
  signIn: () => post('/user/points/sign-in'),
  // 获取签到状态
  getSignStatus: () => get('/user/points/sign-status'),
  // 获取积分记录
  getRecords: (params) => get('/user/points/records', params)
}

// ============ 商户 ============
export const merchantApi = {
  // 获取附近商户
  getNearby: (params) => get('/user/merchants/nearby', params),
  // 获取商户详情
  getDetail: (id) => get(`/user/merchants/${id}`),
  // 获取商户商品
  getProducts: (id, params) => get(`/user/merchants/${id}/products`, params)
}
