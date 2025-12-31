import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: JSON.parse(localStorage.getItem('admin_userInfo') || '{}'),
    roles: []
  }),

  getters: {
    isLogin: (state) => !!state.token,
    username: (state) => state.userInfo?.username || ''
  },

  actions: {
    // 登录
    async login(username, password) {
      try {
        const res = await login({ username, password })
        if (res.code === 200) {
          this.token = res.data.token
          this.userInfo = res.data.userInfo
          localStorage.setItem('admin_token', res.data.token)
          localStorage.setItem('admin_userInfo', JSON.stringify(res.data.userInfo))
          return res.data
        }
        throw new Error(res.message)
      } catch (error) {
        throw error
      }
    },

    // 获取用户信息
    async getUserInfo() {
      try {
        const res = await getUserInfo()
        if (res.code === 200) {
          this.userInfo = res.data
          this.roles = res.data.roles || []
          localStorage.setItem('admin_userInfo', JSON.stringify(res.data))
          return res.data
        }
        throw new Error(res.message)
      } catch (error) {
        throw error
      }
    },

    // 退出登录
    async logout() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.userInfo = {}
        this.roles = []
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_userInfo')
      }
    },

    // 重置状态
    resetState() {
      this.token = ''
      this.userInfo = {}
      this.roles = []
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_userInfo')
    }
  }
})
