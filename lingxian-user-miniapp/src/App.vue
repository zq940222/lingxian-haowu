<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'

onLaunch(() => {
  console.log('铃鲜好物启动')

  // 检查登录状态
  const userStore = useUserStore()
  userStore.checkLoginStatus()

  // 获取位置信息
  getLocation()
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})

// 获取位置信息
const getLocation = () => {
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      uni.setStorageSync('location', {
        latitude: res.latitude,
        longitude: res.longitude
      })
    },
    fail: () => {
      console.log('获取位置失败')
    }
  })
}
</script>

<style lang="scss">
/* 全局样式 */
page {
  background-color: #f5f5f5;
  font-size: 28rpx;
  color: #333;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 清除默认样式 */
view, text, image {
  box-sizing: border-box;
}

/* 通用类 */
.flex {
  display: flex;
}

.flex-column {
  display: flex;
  flex-direction: column;
}

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flex-1 {
  flex: 1;
}

.text-center {
  text-align: center;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 价格样式 */
.price {
  color: #ff4d4f;
  font-weight: bold;
}

.price-original {
  color: #999;
  text-decoration: line-through;
  font-size: 24rpx;
  margin-left: 8rpx;
}

/* 按钮样式 */
.btn-primary {
  background-color: #07c160;
  color: #fff;
  border-radius: 40rpx;
  padding: 20rpx 40rpx;
  text-align: center;
  font-size: 28rpx;
}

.btn-primary:active {
  opacity: 0.8;
}

/* 安全区域 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
