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

    // 微信登录（开发模式使用模拟登录）
    async wxLogin() {
      // 开发环境：使用模拟登录，跳过微信授权
      const isDev = true // 正式上线时改为 false

      if (isDev) {
        // 模拟登录数据
        const mockData = {
          token: 'mock_token_' + Date.now(),
          refreshToken: 'mock_refresh_' + Date.now(),
          userInfo: {
            id: 1,
            nickname: '测试用户',
            avatar: '/static/images/default-avatar.png',
            phone: '138****8888',
            gender: 1,
            points: 100,
            balance: '0.00'
          }
        }

        // 调用后端模拟登录接口
        try {
          const res = await authApi.login({ code: 'mock_1' })
          if (res.code === 200) {
            this.setLoginInfo(res.data)
            return res.data
          }
        } catch (e) {
          // 后端接口失败时使用纯前端模拟
          console.log('使用纯前端模拟登录')
        }

        // 纯前端模拟（后端不可用时）
        this.setLoginInfo(mockData)
        return mockData
      }

      // 正式环境：调用微信登录
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
