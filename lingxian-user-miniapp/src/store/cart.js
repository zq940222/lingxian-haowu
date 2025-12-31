/**
 * 购物车状态管理
 */
import { defineStore } from 'pinia'
import { cartApi } from '@/api'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),

  getters: {
    // 购物车数量
    totalCount: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    },
    // 选中商品数量
    selectedCount: (state) => {
      return state.items.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
    },
    // 选中商品总价
    selectedAmount: (state) => {
      return state.items
        .filter(item => item.selected)
        .reduce((sum, item) => sum + item.price * item.quantity, 0)
    },
    // 是否全选
    isAllSelected: (state) => {
      return state.items.length > 0 && state.items.every(item => item.selected)
    },
    // 按商户分组
    groupedByMerchant: (state) => {
      const groups = {}
      state.items.forEach(item => {
        if (!groups[item.merchantId]) {
          groups[item.merchantId] = {
            merchantId: item.merchantId,
            merchantName: item.merchantName,
            items: []
          }
        }
        groups[item.merchantId].items.push(item)
      })
      return Object.values(groups)
    }
  },

  actions: {
    // 获取购物车列表
    async fetchList() {
      this.loading = true
      try {
        const res = await cartApi.getList()
        if (res.code === 200) {
          this.items = res.data || []
        }
      } catch (e) {
        console.error('获取购物车失败', e)
      } finally {
        this.loading = false
      }
    },

    // 添加到购物车
    async add(product, quantity = 1) {
      try {
        const res = await cartApi.add({
          productId: product.id,
          quantity
        })
        if (res.code === 200) {
          await this.fetchList()
          uni.showToast({ title: '已加入购物车', icon: 'success' })
          return true
        }
      } catch (e) {
        console.error('添加购物车失败', e)
      }
      return false
    },

    // 更新数量
    async updateQuantity(id, quantity) {
      if (quantity < 1) return this.remove(id)

      const item = this.items.find(i => i.id === id)
      if (item) {
        const oldQuantity = item.quantity
        item.quantity = quantity // 乐观更新

        try {
          const res = await cartApi.updateQuantity(id, quantity)
          if (res.code !== 200) {
            item.quantity = oldQuantity // 回滚
          }
        } catch (e) {
          item.quantity = oldQuantity // 回滚
        }
      }
    },

    // 删除商品
    async remove(id) {
      try {
        const res = await cartApi.remove(id)
        if (res.code === 200) {
          this.items = this.items.filter(item => item.id !== id)
        }
      } catch (e) {
        console.error('删除失败', e)
      }
    },

    // 选中/取消选中
    async select(id, selected) {
      const item = this.items.find(i => i.id === id)
      if (item) {
        item.selected = selected
        try {
          await cartApi.select(id, selected)
        } catch (e) {
          item.selected = !selected // 回滚
        }
      }
    },

    // 全选/取消全选
    async selectAll(selected) {
      const oldStates = this.items.map(item => ({ id: item.id, selected: item.selected }))
      this.items.forEach(item => item.selected = selected)

      try {
        await cartApi.selectAll(selected)
      } catch (e) {
        // 回滚
        oldStates.forEach(state => {
          const item = this.items.find(i => i.id === state.id)
          if (item) item.selected = state.selected
        })
      }
    },

    // 清空购物车
    async clear() {
      try {
        const res = await cartApi.clear()
        if (res.code === 200) {
          this.items = []
        }
      } catch (e) {
        console.error('清空失败', e)
      }
    },

    // 获取选中商品
    getSelectedItems() {
      return this.items.filter(item => item.selected)
    }
  }
})
