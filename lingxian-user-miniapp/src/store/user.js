/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null,
    isLogin: false
  }),

  getters: {
    // 获取用户昵称
    nickname: (state) => state.userInfo?.nickname || '未登录',
    // 获取用户头像
    avatar: (state) => state.userInfo?.avatar || '/static/images/default-avatar.png',
    // 获取积分
    points: (state) => state.userInfo?.points || 0
  },

  actions: {
    // 检查登录状态
    checkLoginStatus() {
      const token = uni.getStorageSync('token')
      const userInfo = uni.getStorageSync('userInfo')
      if (token && userInfo) {
        this.token = token
        this.userInfo = userInfo
        this.isLogin = true
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
                const res = await authApi.login({ code: loginRes.code })
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
      this.userInfo = data.userInfo
      this.isLogin = true
      uni.setStorageSync('token', data.token)
      uni.setStorageSync('userInfo', data.userInfo)
    },

    // 更新用户信息
    updateUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      uni.setStorageSync('userInfo', this.userInfo)
    },

    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      this.isLogin = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const res = await authApi.getUserInfo()
        if (res.code === 200) {
          this.userInfo = res.data
          uni.setStorageSync('userInfo', res.data)
        }
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    }
  }
})
