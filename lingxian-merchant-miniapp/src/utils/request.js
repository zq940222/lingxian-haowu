/**
 * 商户端网络请求封装
 */

// 根据环境配置 API 地址
const getBaseUrl = () => {
  // H5 开发环境使用代理
  // #ifdef H5
  return '/api'
  // #endif
  // 其他环境使用实际地址
  // #ifndef H5
  return 'https://api.lingxian.com/api'
  // #endif
}

const BASE_URL = getBaseUrl()

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('merchant_token')

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
            uni.removeStorageSync('merchant_token')
            uni.removeStorageSync('merchantInfo')
            uni.reLaunch({ url: '/pages/login/index' })
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

export const get = (url, data = {}) => request({ url, method: 'GET', data })
export const post = (url, data = {}) => request({ url, method: 'POST', data })
export const put = (url, data = {}) => request({ url, method: 'PUT', data })
export const del = (url, data = {}) => request({ url, method: 'DELETE', data })

export default request
