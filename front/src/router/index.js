import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guest: true }
    },
    {
      path: '/',
      component: () => import('@/components/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'home', component: () => import('@/views/home/HomeView.vue') },
        { path: 'profile', name: 'profile', component: () => import('@/views/profile/ProfileView.vue') },
        { path: 'messages', name: 'messages', component: () => import('@/views/message/MessageView.vue') },
        { path: 'logs', name: 'logs', component: () => import('@/views/message/LogView.vue') },
        // 任务模块
        { path: 'task/publish', name: 'taskPublish', component: () => import('@/views/task/TaskPublishView.vue') },
        { path: 'task/list', name: 'taskList', component: () => import('@/views/task/TaskListView.vue') },
        { path: 'task/:id', name: 'taskDetail', component: () => import('@/views/task/TaskDetailView.vue') },
        // 跑腿接单
        { path: 'runner/tasks', name: 'runnerTasks', component: () => import('@/views/runner/RunnerTaskView.vue') },
        // 配送跟踪
        { path: 'delivery/:taskId', name: 'deliveryTracking', component: () => import('@/views/delivery/DeliveryTrackingView.vue') },
        // 订单
        { path: 'orders', name: 'orders', component: () => import('@/views/order/OrderTrackingView.vue') },
        { path: 'orders/manage', name: 'orderManage', component: () => import('@/views/order/OrderManageView.vue') },
        // 评价
        { path: 'reviews', name: 'reviews', component: () => import('@/views/review/ReviewView.vue') },
        // 反馈
        { path: 'feedback', name: 'feedback', component: () => import('@/views/feedback/FeedbackView.vue') },
        // 管理员
        { path: 'admin/dashboard', name: 'adminDashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { admin: true } },
        { path: 'admin/feedback', name: 'adminFeedback', component: () => import('@/views/admin/AdminFeedbackView.vue'), meta: { admin: true } }
      ]
    }
  ]
})

// 导航守卫
router.beforeEach(async (to) => {
  const userStore = useUserStore()

  // 尝试获取用户信息
  if (!userStore.loginUser) {
    await userStore.fetchLoginUser()
  }

  // 需要登录但未登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // 已登录访问登录/注册页
  if (to.meta.guest && userStore.isLoggedIn) {
    return { name: 'home' }
  }

  // 管理员页面权限
  if (to.meta.admin && !userStore.isAdmin) {
    return { name: 'home' }
  }
})

export default router
