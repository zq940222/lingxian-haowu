/**
 * uni-app 网络请求封装
 */

// 开发环境使用本地地址，生产环境使用线上地址
const BASE_URL = process.env.NODE_ENV === 'development'
  ? 'http://localhost:8085/api'
  : 'https://api.lingxian.com/api'

// 请求拦截
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }

    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data)
          } else if (res.data.code === 401) {
            // token过期，清除登录状态
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.showToast({
              title: '请重新登录',
              icon: 'none'
            })
            // 跳转登录
            setTimeout(() => {
              uni.navigateTo({ url: '/pages/user/index' })
            }, 1500)
            reject(res.data)
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(res.data)
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络异常',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// GET请求
export const get = (url, data = {}) => request({ url, method: 'GET', data })

// POST请求
export const post = (url, data = {}) => request({ url, method: 'POST', data })

// PUT请求
export const put = (url, data = {}) => request({ url, method: 'PUT', data })

// DELETE请求
export const del = (url, data = {}) => request({ url, method: 'DELETE', data })

export default request
