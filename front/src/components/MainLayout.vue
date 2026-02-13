<template>
  <div class="main-layout">
    <!-- 顶部导航栏 (Apple-style glass header) -->
    <header class="app-header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/" class="logo">
            <el-icon :size="22"><Van /></el-icon>
            <span>校园跑腿</span>
          </router-link>
        </div>
        <nav class="header-nav">
          <router-link to="/task/list" class="nav-link">任务大厅</router-link>
          <router-link to="/orders" class="nav-link">我的订单</router-link>
          <router-link v-if="userStore.isRunner" to="/runner/tasks" class="nav-link">接单中心</router-link>
          <router-link to="/reviews" class="nav-link">评价中心</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/dashboard" class="nav-link nav-admin">数据分析</router-link>
        </nav>
        <div class="header-right">
          <router-link to="/messages" class="icon-btn">
            <el-badge :value="unreadCount || undefined" :hidden="!unreadCount">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </router-link>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-avatar-btn">
              <el-avatar :size="32" :src="userStore.loginUser?.userAvatar" />
              <span class="user-name">{{ userStore.loginUser?.userName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logs">
                  <el-icon><Document /></el-icon>操作记录
                </el-dropdown-item>
                <el-dropdown-item command="feedback">
                  <el-icon><ChatDotSquare /></el-icon>意见反馈
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="adminFeedback" divided>
                  <el-icon><Setting /></el-icon>反馈管理
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listMyMessages } from '@/api/message'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)

const fetchUnread = async () => {
  try {
    const res = await listMyMessages({ current: 1, pageSize: 1, isRead: 0 })
    unreadCount.value = res.total || 0
  } catch {
    // ignore
  }
}

onMounted(() => {
  fetchUnread()
  // 每60秒刷新未读数
  setInterval(fetchUnread, 60000)
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logs') {
    router.push('/logs')
  } else if (command === 'feedback') {
    router.push('/feedback')
  } else if (command === 'adminFeedback') {
    router.push('/admin/feedback')
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: var(--header-height);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-bottom: 1px solid var(--color-border-light);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.header-left .logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  letter-spacing: -0.02em;
}
.header-left .logo:hover {
  color: var(--color-text-primary);
}

.header-nav {
  display: flex;
  gap: 4px;
}

.nav-link {
  padding: 6px 16px;
  border-radius: 980px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}
.nav-link:hover {
  color: var(--color-text-primary);
  background: var(--color-bg-hover);
}
.nav-link.router-link-active {
  color: var(--color-primary);
  background: rgba(0, 113, 227, 0.08);
}
.nav-admin {
  color: var(--color-warning);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-btn {
  display: flex;
  align-items: center;
  padding: 6px;
  border-radius: 50%;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}
.icon-btn:hover {
  color: var(--color-text-primary);
  background: var(--color-bg-hover);
}

.user-avatar-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 980px;
  transition: background var(--transition-fast);
}
.user-avatar-btn:hover {
  background: var(--color-bg-hover);
}
.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-main {
  padding-top: var(--header-height);
  min-height: 100vh;
}
</style>
