<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useMerchantStore } from '@/store/merchant'

onLaunch(() => {
  console.log('铃鲜好物商家版启动')

  // 检查登录状态
  const merchantStore = useMerchantStore()
  merchantStore.checkLoginStatus()
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
@import './uni.scss';

page {
  background-color: #f5f5f5;
  font-size: 28rpx;
  color: #333;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

view, text, image {
  box-sizing: border-box;
}

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

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.safe-area-top {
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

/* 底部固定栏安全区域 */
.fixed-bottom {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background-color: #fff;
  z-index: 100;
}

/* 底部占位，防止fixed元素遮挡内容 */
.bottom-placeholder {
  height: calc(100rpx + constant(safe-area-inset-bottom));
  height: calc(100rpx + env(safe-area-inset-bottom));
}

/* 自定义导航栏状态栏占位 */
.status-bar {
  height: var(--status-bar-height, 44px);
}
</style>
