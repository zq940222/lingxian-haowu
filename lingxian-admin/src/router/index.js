import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/store/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/user',
    component: () => import('@/layouts/index.vue'),
    redirect: '/user/list',
    meta: { title: '用户管理', icon: 'User' },
    children: [
      {
        path: 'list',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户列表' }
      },
      {
        path: 'detail/:id',
        name: 'UserDetail',
        component: () => import('@/views/user/detail.vue'),
        meta: { title: '用户详情', hidden: true }
      }
    ]
  },
  {
    path: '/merchant',
    component: () => import('@/layouts/index.vue'),
    redirect: '/merchant/list',
    meta: { title: '商户管理', icon: 'Shop' },
    children: [
      {
        path: 'list',
        name: 'MerchantList',
        component: () => import('@/views/merchant/list.vue'),
        meta: { title: '商户列表' }
      },
      {
        path: 'verify',
        name: 'MerchantVerify',
        component: () => import('@/views/merchant/verify.vue'),
        meta: { title: '商户审核' }
      },
      {
        path: 'detail/:id',
        name: 'MerchantDetail',
        component: () => import('@/views/merchant/detail.vue'),
        meta: { title: '商户详情', hidden: true }
      }
    ]
  },
  {
    path: '/product',
    component: () => import('@/layouts/index.vue'),
    redirect: '/product/list',
    meta: { title: '商品管理', icon: 'Goods' },
    children: [
      {
        path: 'list',
        name: 'ProductList',
        component: () => import('@/views/product/list.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'category',
        name: 'ProductCategory',
        component: () => import('@/views/product/category.vue'),
        meta: { title: '商品分类' }
      }
    ]
  },
  {
    path: '/order',
    component: () => import('@/layouts/index.vue'),
    redirect: '/order/list',
    meta: { title: '订单管理', icon: 'List' },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('@/views/order/list.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/detail.vue'),
        meta: { title: '订单详情', hidden: true }
      },
      {
        path: 'refund',
        name: 'RefundList',
        component: () => import('@/views/order/refund.vue'),
        meta: { title: '退款管理' }
      }
    ]
  },
  {
    path: '/group',
    component: () => import('@/layouts/index.vue'),
    redirect: '/group/activity',
    meta: { title: '拼团管理', icon: 'Connection' },
    children: [
      {
        path: 'activity',
        name: 'GroupActivity',
        component: () => import('@/views/group/activity.vue'),
        meta: { title: '拼团活动' }
      },
      {
        path: 'record',
        name: 'GroupRecord',
        component: () => import('@/views/group/record.vue'),
        meta: { title: '拼团记录' }
      }
    ]
  },
  {
    path: '/marketing',
    component: () => import('@/layouts/index.vue'),
    redirect: '/marketing/banner',
    meta: { title: '营销管理', icon: 'TrendCharts' },
    children: [
      {
        path: 'banner',
        name: 'BannerList',
        component: () => import('@/views/marketing/banner.vue'),
        meta: { title: '轮播图管理' }
      },
      {
        path: 'points',
        name: 'PointsConfig',
        component: () => import('@/views/marketing/points.vue'),
        meta: { title: '积分配置' }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/layouts/index.vue'),
    redirect: '/system/admin',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'admin',
        name: 'AdminList',
        component: () => import('@/views/system/admin.vue'),
        meta: { title: '管理员管理' }
      },
      {
        path: 'role',
        name: 'RoleList',
        component: () => import('@/views/system/role.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config.vue'),
        meta: { title: '系统配置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()

  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('admin_token')

  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
