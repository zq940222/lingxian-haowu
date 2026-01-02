/**
 * 购物车状态管理 - 支持多商户分组
 */
import { defineStore } from 'pinia'
import { cartApi } from '@/api'

export const useCartStore = defineStore('cart', {
  state: () => ({
    merchantGroups: [], // 按商户分组的购物车数据
    totalCount: 0,
    totalPrice: 0,
    selectedCount: 0,
    selectedPrice: 0,
    loading: false
  }),

  getters: {
    // 所有商品列表（扁平化）
    allItems: (state) => {
      return state.merchantGroups.flatMap(group => group.items || [])
    },
    // 选中商品总价
    selectedAmount: (state) => {
      return state.selectedPrice
    },
    // 是否全选
    isAllSelected: (state) => {
      if (state.merchantGroups.length === 0) return false
      return state.merchantGroups.every(group => group.allSelected)
    },
    // 是否有商品
    hasItems: (state) => {
      return state.merchantGroups.length > 0
    }
  },

  actions: {
    // 更新tabBar购物车角标
    updateTabBarBadge() {
      const count = this.totalCount
      if (count > 0) {
        uni.setTabBarBadge({
          index: 2, // 购物车是第3个tab
          text: count > 99 ? '99+' : String(count)
        })
      } else {
        uni.removeTabBarBadge({ index: 2 })
      }
    },

    // 获取购物车列表
    async fetchList() {
      this.loading = true
      try {
        const res = await cartApi.getList()
        if (res.code === 200) {
          this.merchantGroups = res.data?.merchantGroups || []
          this.totalCount = res.data?.totalCount || 0
          this.totalPrice = res.data?.totalPrice || 0
          this.selectedCount = res.data?.selectedCount || 0
          this.selectedPrice = res.data?.selectedPrice || 0
          this.updateTabBarBadge()
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

    // 查找商品项
    findItem(id) {
      for (const group of this.merchantGroups) {
        const item = group.items?.find(i => i.id === id)
        if (item) return { item, group }
      }
      return null
    },

    // 更新数量
    async updateQuantity(id, quantity) {
      if (quantity < 1) return this.remove(id)

      const found = this.findItem(id)
      if (found) {
        const { item } = found
        const oldQuantity = item.quantity
        item.quantity = quantity // 乐观更新
        this.recalculateTotals()
        this.updateTabBarBadge()

        try {
          const res = await cartApi.updateQuantity(id, quantity)
          if (res.code !== 200) {
            item.quantity = oldQuantity // 回滚
            this.recalculateTotals()
            this.updateTabBarBadge()
          }
        } catch (e) {
          item.quantity = oldQuantity // 回滚
          this.recalculateTotals()
          this.updateTabBarBadge()
        }
      }
    },

    // 删除商品
    async remove(id) {
      try {
        await cartApi.remove(id)
        await this.fetchList()
      } catch (e) {
        console.error('删除失败', e)
        await this.fetchList()
      }
    },

    // 选中/取消选中单个商品
    async select(id, selected) {
      const found = this.findItem(id)
      if (found) {
        const { item, group } = found
        item.selected = selected
        // 更新商户分组的全选状态
        group.allSelected = group.items.every(i => i.selected)
        this.recalculateTotals()

        try {
          await cartApi.select(id, selected)
        } catch (e) {
          item.selected = !selected // 回滚
          group.allSelected = group.items.every(i => i.selected)
          this.recalculateTotals()
        }
      }
    },

    // 选中/取消选中某商户的所有商品
    async selectMerchant(merchantId, selected) {
      const group = this.merchantGroups.find(g => g.merchantId === merchantId)
      if (group) {
        const oldStates = group.items.map(item => ({ id: item.id, selected: item.selected }))
        group.items.forEach(item => item.selected = selected)
        group.allSelected = selected
        this.recalculateTotals()

        try {
          await cartApi.selectMerchant(merchantId, selected)
        } catch (e) {
          // 回滚
          oldStates.forEach(state => {
            const item = group.items.find(i => i.id === state.id)
            if (item) item.selected = state.selected
          })
          group.allSelected = group.items.every(i => i.selected)
          this.recalculateTotals()
        }
      }
    },

    // 全选/取消全选
    async selectAll(selected) {
      const oldStates = this.merchantGroups.map(group => ({
        merchantId: group.merchantId,
        allSelected: group.allSelected,
        items: group.items.map(item => ({ id: item.id, selected: item.selected }))
      }))

      this.merchantGroups.forEach(group => {
        group.items.forEach(item => item.selected = selected)
        group.allSelected = selected
      })
      this.recalculateTotals()

      try {
        await cartApi.selectAll(selected)
      } catch (e) {
        // 回滚
        oldStates.forEach(state => {
          const group = this.merchantGroups.find(g => g.merchantId === state.merchantId)
          if (group) {
            state.items.forEach(itemState => {
              const item = group.items.find(i => i.id === itemState.id)
              if (item) item.selected = itemState.selected
            })
            group.allSelected = state.allSelected
          }
        })
        this.recalculateTotals()
      }
    },

    // 重新计算总计
    recalculateTotals() {
      let totalCount = 0
      let totalPrice = 0
      let selectedCount = 0
      let selectedPrice = 0

      this.merchantGroups.forEach(group => {
        group.items?.forEach(item => {
          const subtotal = item.price * item.quantity
          totalCount += item.quantity
          totalPrice += subtotal
          if (item.selected) {
            selectedCount += item.quantity
            selectedPrice += subtotal
          }
        })
      })

      this.totalCount = totalCount
      this.totalPrice = totalPrice
      this.selectedCount = selectedCount
      this.selectedPrice = selectedPrice
    },

    // 清空购物车
    async clear() {
      try {
        const res = await cartApi.clear()
        if (res.code === 200) {
          this.merchantGroups = []
          this.totalCount = 0
          this.totalPrice = 0
          this.selectedCount = 0
          this.selectedPrice = 0
          this.updateTabBarBadge()
        }
      } catch (e) {
        console.error('清空失败', e)
      }
    },

    // 获取选中商品（按商户分组）
    getSelectedItemsByMerchant() {
      return this.merchantGroups
        .map(group => ({
          merchantId: group.merchantId,
          merchantName: group.merchantName,
          merchantLogo: group.merchantLogo,
          items: group.items.filter(item => item.selected)
        }))
        .filter(group => group.items.length > 0)
    }
  }
})
