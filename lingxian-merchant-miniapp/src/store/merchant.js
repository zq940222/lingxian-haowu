/**
 * 商户状态管理
 */
import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useMerchantStore = defineStore('merchant', {
  state: () => ({
    token: '',
    merchantInfo: null,
    isLogin: false
  }),

  getters: {
    shopName: (state) => state.merchantInfo?.shopName || '我的店铺',
    shopLogo: (state) => state.merchantInfo?.shopLogo || '/static/images/default-shop.png',
    shopStatus: (state) => state.merchantInfo?.shopStatus || 0
  },

  actions: {
    // 检查登录状态
    checkLoginStatus() {
      const token = uni.getStorageSync('merchant_token')
      const merchantInfo = uni.getStorageSync('merchantInfo')
      if (token && merchantInfo) {
        this.token = token
        this.merchantInfo = merchantInfo
        this.isLogin = true
      } else {
        // 未登录，跳转登录页
        const pages = getCurrentPages()
        if (pages.length > 0) {
          const currentPage = pages[pages.length - 1]
          if (currentPage.route !== 'pages/login/index') {
            uni.reLaunch({ url: '/pages/login/index' })
          }
        }
      }
    },

    // 账号密码登录
    async login(phone, password) {
      try {
        const res = await authApi.login({ phone, password })
        if (res.code === 200) {
          this.setLoginInfo(res.data)
          return res.data
        }
        throw new Error(res.message)
      } catch (e) {
        throw e
      }
    },

    // 微信登录
    async wxLogin() {
      return new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: async (loginRes) => {
            if (loginRes.code) {
              try {
                const res = await authApi.wxLogin({ code: loginRes.code })
                if (res.code === 200) {
                  this.setLoginInfo(res.data)
                  resolve(res.data)
                } else {
                  reject(res.message)
                }
              } catch (e) {
                reject(e)
              }
            } else {
              reject('登录失败')
            }
          },
          fail: reject
        })
      })
    },

    // 设置登录信息
    setLoginInfo(data) {
      this.token = data.token
      this.merchantInfo = data.merchantInfo
      this.isLogin = true
      uni.setStorageSync('merchant_token', data.token)
      uni.setStorageSync('merchantInfo', data.merchantInfo)
    },

    // 更新商户信息
    updateMerchantInfo(info) {
      this.merchantInfo = { ...this.merchantInfo, ...info }
      uni.setStorageSync('merchantInfo', this.merchantInfo)
    },

    // 退出登录
    logout() {
      this.token = ''
      this.merchantInfo = null
      this.isLogin = false
      uni.removeStorageSync('merchant_token')
      uni.removeStorageSync('merchantInfo')
      uni.reLaunch({ url: '/pages/login/index' })
    },

    // 获取最新商户信息
    async fetchMerchantInfo() {
      try {
        const res = await authApi.getMerchantInfo()
        if (res.code === 200) {
          this.merchantInfo = res.data
          uni.setStorageSync('merchantInfo', res.data)
        }
      } catch (e) {
        console.error('获取商户信息失败', e)
      }
    }
  }
})
