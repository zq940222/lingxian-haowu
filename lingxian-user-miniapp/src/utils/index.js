/**
 * 工具函数
 */

/**
 * 格式化价格
 */
export const formatPrice = (price) => {
  if (!price && price !== 0) return '0.00'
  return Number(price).toFixed(2)
}

/**
 * 格式化时间
 */
export const formatTime = (date, fmt = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''
  const d = new Date(date)
  const opt = {
    'Y+': d.getFullYear().toString(),
    'M+': (d.getMonth() + 1).toString(),
    'D+': d.getDate().toString(),
    'H+': d.getHours().toString(),
    'm+': d.getMinutes().toString(),
    's+': d.getSeconds().toString()
  }
  for (let k in opt) {
    const ret = new RegExp('(' + k + ')').exec(fmt)
    if (ret) {
      fmt = fmt.replace(ret[1], ret[1].length === 1 ? opt[k] : opt[k].padStart(ret[1].length, '0'))
    }
  }
  return fmt
}

/**
 * 防抖函数
 */
export const debounce = (fn, delay = 300) => {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 */
export const throttle = (fn, delay = 300) => {
  let last = 0
  return function (...args) {
    const now = Date.now()
    if (now - last >= delay) {
      last = now
      fn.apply(this, args)
    }
  }
}

/**
 * 显示加载
 */
export const showLoading = (title = '加载中...') => {
  uni.showLoading({ title, mask: true })
}

/**
 * 隐藏加载
 */
export const hideLoading = () => {
  uni.hideLoading()
}

/**
 * 显示成功提示
 */
export const showSuccess = (title) => {
  uni.showToast({ title, icon: 'success' })
}

/**
 * 显示错误提示
 */
export const showError = (title) => {
  uni.showToast({ title, icon: 'none' })
}

/**
 * 确认弹窗
 */
export const showConfirm = (content, title = '提示') => {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        if (res.confirm) {
          resolve(true)
        } else {
          reject(false)
        }
      }
    })
  })
}

/**
 * 计算距离
 */
export const calculateDistance = (lat1, lng1, lat2, lng2) => {
  const radLat1 = (lat1 * Math.PI) / 180
  const radLat2 = (lat2 * Math.PI) / 180
  const a = radLat1 - radLat2
  const b = ((lng1 - lng2) * Math.PI) / 180
  let s = 2 * Math.asin(
    Math.sqrt(
      Math.pow(Math.sin(a / 2), 2) +
      Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)
    )
  )
  s = s * 6378.137
  return Math.round(s * 100) / 100
}
