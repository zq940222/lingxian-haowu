/**
 * 商户状态管理
 */
import { defineStore } from 'pinia'
import { authApi } from '@/api'

// 审核状态枚举
export const VerifyStatus = {
  NOT_SUBMITTED: 0, // 未提交申请
  PENDING: 1,       // 待审核
  APPROVED: 2,      // 审核通过
  REJECTED: 3       // 审核拒绝
}

export const useMerchantStore = defineStore('merchant', {
  state: () => ({
    token: '',
    refreshToken: '',
    userInfo: null,      // 商户用户信息
    merchantInfo: null,  // 商户店铺信息
    verifyStatus: 0,     // 审核状态
    isLogin: false
  }),

  getters: {
    // 店铺名称
    shopName: (state) => state.merchantInfo?.shopName || '我的店铺',
    // 店铺logo
    shopLogo: (state) => state.merchantInfo?.logo || '/static/images/default-shop.png',
    // 店铺状态
    shopStatus: (state) => state.merchantInfo?.status || 0,
    // 是否已提交申请
    hasApplied: (state) => state.verifyStatus > 0,
    // 是否审核通过
    isApproved: (state) => state.verifyStatus === VerifyStatus.APPROVED,
    // 是否待审核
    isPending: (state) => state.verifyStatus === VerifyStatus.PENDING,
    // 是否被拒绝
    isRejected: (state) => state.verifyStatus === VerifyStatus.REJECTED,
    // 审核状态文本
    verifyStatusText: (state) => {
      switch (state.verifyStatus) {
        case VerifyStatus.NOT_SUBMITTED: return '未提交申请'
        case VerifyStatus.PENDING: return '待审核'
        case VerifyStatus.APPROVED: return '审核通过'
        case VerifyStatus.REJECTED: return '审核拒绝'
        default: return '未知状态'
      }
    }
  },

  actions: {
    // 检查登录状态
    checkLoginStatus() {
      const token = uni.getStorageSync('merchant_token')
      const refreshToken = uni.getStorageSync('merchant_refresh_token')
      const userInfo = uni.getStorageSync('merchant_user_info')
      const merchantInfo = uni.getStorageSync('merchant_info')
      const verifyStatus = uni.getStorageSync('merchant_verify_status')

      if (token && userInfo) {
        this.token = token
        this.refreshToken = refreshToken
        this.userInfo = userInfo
        this.merchantInfo = merchantInfo
        this.verifyStatus = verifyStatus || 0
        this.isLogin = true
        return true
      } else {
        // 未登录，跳转登录页
        const pages = getCurrentPages()
        if (pages.length > 0) {
          const currentPage = pages[pages.length - 1]
          if (currentPage.route !== 'pages/login/index') {
            uni.reLaunch({ url: '/pages/login/index' })
          }
        }
        return false
      }
    },

    // 检查是否可以访问功能（审核通过才能使用）
    checkAccess() {
      if (!this.isLogin) {
        uni.reLaunch({ url: '/pages/login/index' })
        return false
      }
      if (this.verifyStatus !== VerifyStatus.APPROVED) {
        // 未通过审核，跳转到申请/状态页面
        uni.reLaunch({ url: '/pages/apply/status' })
        return false
      }
      return true
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
      this.refreshToken = data.refreshToken
      this.userInfo = data.userInfo
      this.merchantInfo = data.merchantInfo || null
      this.verifyStatus = data.verifyStatus || 0
      this.isLogin = true

      uni.setStorageSync('merchant_token', data.token)
      uni.setStorageSync('merchant_refresh_token', data.refreshToken)
      uni.setStorageSync('merchant_user_info', data.userInfo)
      if (data.merchantInfo) {
        uni.setStorageSync('merchant_info', data.merchantInfo)
      }
      uni.setStorageSync('merchant_verify_status', data.verifyStatus || 0)
    },

    // 更新商户信息
    updateMerchantInfo(info) {
      this.merchantInfo = { ...this.merchantInfo, ...info }
      uni.setStorageSync('merchant_info', this.merchantInfo)
    },

    // 更新审核状态
    updateVerifyStatus(status) {
      this.verifyStatus = status
      uni.setStorageSync('merchant_verify_status', status)
    },

    // 提交入驻申请
    async submitApply(data) {
      try {
        const res = await authApi.applyMerchant(data)
        if (res.code === 200) {
          this.verifyStatus = res.data.verifyStatus
          uni.setStorageSync('merchant_verify_status', res.data.verifyStatus)
          return res.data
        }
        throw new Error(res.message)
      } catch (e) {
        throw e
      }
    },

    // 更新入驻申请（被拒绝后重新提交）
    async updateApply(data) {
      try {
        const res = await authApi.updateApply(data)
        if (res.code === 200) {
          this.verifyStatus = res.data.verifyStatus
          uni.setStorageSync('merchant_verify_status', res.data.verifyStatus)
          return res.data
        }
        throw new Error(res.message)
      } catch (e) {
        throw e
      }
    },

    // 查询申请状态
    async fetchApplyStatus() {
      try {
        const res = await authApi.getApplyStatus()
        if (res.code === 200) {
          this.verifyStatus = res.data.verifyStatus
          if (res.data.merchantInfo) {
            this.merchantInfo = res.data.merchantInfo
            uni.setStorageSync('merchant_info', res.data.merchantInfo)
          }
          uni.setStorageSync('merchant_verify_status', res.data.verifyStatus)
          return res.data
        }
        throw new Error(res.message)
      } catch (e) {
        throw e
      }
    },

    // 获取最新商户信息
    async fetchMerchantInfo() {
      try {
        const res = await authApi.getMerchantInfo()
        if (res.code === 200) {
          this.merchantInfo = res.data
          uni.setStorageSync('merchant_info', res.data)
          return res.data
        }
      } catch (e) {
        console.error('获取商户信息失败', e)
      }
    },

    // 退出登录
    logout() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      this.merchantInfo = null
      this.verifyStatus = 0
      this.isLogin = false

      uni.removeStorageSync('merchant_token')
      uni.removeStorageSync('merchant_refresh_token')
      uni.removeStorageSync('merchant_user_info')
      uni.removeStorageSync('merchant_info')
      uni.removeStorageSync('merchant_verify_status')

      uni.reLaunch({ url: '/pages/login/index' })
    }
  }
})
