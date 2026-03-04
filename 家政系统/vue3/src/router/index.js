import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'
import Profile from '@/views/frontend/Profile.vue'

// 静态路由
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/back',
    name: 'Backend',
    component: BackendLayout,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '仪表盘' }
      }
    ]
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/component-test',
    name: 'ComponentTest',
    component: () => import('../views/ComponentTest.vue')
  }
]

// 动态路由
export const asyncRoutes = []

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/frontend/HomeView.vue')
      },
      {
        path: 'services',
        name: 'Services',
        component: () => import('@/views/frontend/ServicesView.vue')
      },
      {
        path: 'services/:id',
        name: 'ServiceCategory',
        component: () => import('@/views/frontend/ServiceCategoryView.vue')
      },
      {
        path: 'service/:id',
        name: 'ServiceDetail',
        component: () => import('@/views/frontend/ServiceDetailView.vue')
      },
      {
        path: 'staff',
        name: 'Staff',
        component: () => import('@/views/frontend/StaffView.vue')
      },
      {
        path: 'staff/:id',
        name: 'StaffDetail',
        component: () => import('@/views/frontend/StaffDetailView.vue')
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/frontend/AboutView.vue')
      },
      {
        path: '/order/create',
        name: 'OrderCreate',
        component: () => import('@/views/frontend/OrderCreateView.vue'),
        meta: {
          requiresAuth: true,
          title: '创建预约'
        }
      },
      {
        path: '/my-orders',
        name: 'MyOrders',
        component: () => import('@/views/frontend/MyOrdersView.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: '/payment',
        name: 'Payment',
        component: () => import('@/views/frontend/PaymentView.vue'),
        meta: {
          requiresAuth: true,
          title: '订单支付'
        }
      },
      {
        path: '/refund/apply',
        name: 'RefundApply',
        component: () => import('@/views/frontend/RefundApplyView.vue'),
        meta: {
          requiresAuth: true,
          title: '申请退款'
        }
      },
      {
        path: '/payment-result',
        name: 'PaymentResult',
        component: () => import('@/views/frontend/PaymentResultView.vue'),
        meta: {
          title: '支付结果'
        }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: Profile
      },

      {
        path: '/browse-history',
        name: 'BrowseHistory',
        component: () => import('@/views/frontend/BrowseHistoryView.vue'),
        meta: {
          requiresAuth: true,
          title: '浏览历史'
        }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...constantRoutes,
    // 将404路由放在最后
    {
      path: '/:pathMatch(.*)*',
      redirect: '/404'
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 家政服务系统`
  }

  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  const isUser = userStore.isUser

  // 根路径重定向到首页
  if (to.path === '/' && to.name !== 'Home') {
    next({ name: 'Home' })
    return
  }

  // 如果是访问登录页
  if (to.path === '/login') {
    if (isLoggedIn) {
      // 已登录用户重定向到对应首页
      next(isUser ? '/' : '/back/dashboard')
      return
    }
    next()
    return
  }

  // 如果未登录
  if (!isLoggedIn) {
    if (to.meta.requiresAuth || to.path.startsWith('/back')) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    next()
    return
  }

  // 已登录用户的路由控制
  if (isLoggedIn) {
    // 动态添加后台路由（仅对非普通用户）
    if (!isUser && !router.hasRoute('Backend-loaded')) {
      await setBackRoutes()
      // 如果当前路由不存在，则重定向到目标路由
      if (to.matched.length === 0) {
        next({ path: to.fullPath, replace: true })
        return
      }
    }

    // 权限控制
    if (!isUser && !to.path.startsWith('/back') && to.path !== '/') {
      next('/back/dashboard')
      return
    } else if (isUser && to.path.startsWith('/back')) {
      next('/')
      return
    }
  }

  next()
})

// 动态加载组件
export function loadComponent(component) {
  try {
    // 如果是布局组件
    if (component === 'Layout') {
      return BackendLayout
    }
    // 处理路径中的斜杠，转换为实际的文件路径
    const path = component.replace(/^\//, '').replace(/\//g, '/')
    // 其他组件动态导入
    return () => import(`@/views/backend/${path}.vue`).catch(error => {
      console.error(`Failed to load component: @/views/backend/${path}.vue`, error)
      // 返回一个简单的错误提示组件
      return {
        template: `
          <div class="error-component">
            <h3>组件加载失败</h3>
            <p>路径: ${path}</p>
          </div>
        `
      }
    })
  } catch (error) {
    console.error('Component loading error:', error)
    // 返回一个空组件而不是直接抛出错误
    return {
      template: '<div>组件加载失败</div>'
    }
  }
}

// 生成动态路由
export function generateAsyncRoutes(menuItems) {
  const menus = menuItems || JSON.parse(localStorage.getItem('menus'))
  if (!menus || !menus.length) {
    console.warn('No menus provided for route generation')
    return []
  }

  const routes = []

  menus.forEach(menu => {
    console.log("current menu", menu)
    if (!menu.path) {
      console.warn('Invalid menu item: missing path')
      return
    }

    const route = {
      path: '/back' + menu.path,
      name: menu.name || menu.path.replace(/\//g, '-').slice(1),
      component: menu.component ? loadComponent(menu.component) : null,
      meta: {
        title: menu.title || menu.name,
        icon: menu.icon,
        hidden: menu.hidden
      }
    }

    if (menu.children && menu.children.length > 0) {
      route.children = generateAsyncRoutes(menu.children)
    }

    routes.push(route)
  })

  return routes
}

export function setBackRoutes() {
  try {
    // 先移除已存在的动态路由
    if (router.hasRoute('Backend-loaded')) {
      router.removeRoute('Backend-loaded')
    }
    
    const routes = generateAsyncRoutes()
    if (!routes || routes.length === 0) {
      console.warn('No dynamic routes generated')
      return
    }
    
    // 找到后台布局路由
    const backendRoute = router.getRoutes().find(route => route.name === 'Backend')
    
    if (backendRoute) {
      // 为每个动态路由添加到后台布局下
      routes.forEach(route => {
        const routeName = 'Backend-' + route.name
        if (router.hasRoute(routeName)) {
          router.removeRoute(routeName)
        }
        router.addRoute('Backend', route)
      })
      
      // 添加标记路由
      router.addRoute({
        path: '/back-loaded',
        name: 'Backend-loaded',
        component: { render: () => null },
        meta: { hidden: true }
      })
    }
  } catch (error) {
    console.error('Failed to set back routes:', error)
  }
}

// 创建一个初始化函数，在main.js中调用
export function initDynamicRoutes() {
  const userStore = useUserStore()
  if (userStore.isLoggedIn && !userStore.isUser) {
    setBackRoutes()
  }
}

export default router
