<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-content">
        <div class="header-left">
          <router-link to="/" class="logo">
            <el-icon :size="22"><Van /></el-icon>
            <span>校园跑腿</span>
          </router-link>
        </div>

        <!-- PC端导航链接 -->
        <nav class="header-nav pc-only">
          <router-link to="/task/list" class="nav-link">任务大厅</router-link>
          <router-link v-if="!userStore.isAdmin" to="/orders" class="nav-link">我的订单</router-link>
          <router-link v-if="userStore.isRunner" to="/runner/tasks" class="nav-link">接单中心</router-link>
          <router-link v-if="!userStore.isAdmin" to="/reviews" class="nav-link">评价中心</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/dashboard" class="nav-link nav-admin">数据分析</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/users" class="nav-link nav-admin">用户管理</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/feedback" class="nav-link nav-admin">反馈管理</router-link>
        </nav>

        <div class="header-right">
          <!-- 消息铃铛 -->
          <router-link to="/messages" class="icon-btn">
            <el-badge :value="unreadCount || undefined" :hidden="!unreadCount">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </router-link>

          <!-- PC端用户下拉 -->
          <el-dropdown class="pc-only" trigger="click" @command="handleCommand">
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
                <el-dropdown-item v-if="!userStore.isAdmin" command="feedback">
                  <el-icon><ChatDotSquare /></el-icon>意见反馈
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 移动端汉堡按钮 -->
          <button class="hamburger mobile-only" @click="drawerOpen = true" aria-label="菜单">
            <span /><span /><span />
          </button>
        </div>
      </div>
    </header>

    <!-- 移动端侧滑抽屉遮罩 -->
    <transition name="overlay-fade">
      <div v-if="drawerOpen" class="drawer-overlay" @click="drawerOpen = false" />
    </transition>

    <!-- 移动端侧滑抽屉 -->
    <transition name="drawer-slide">
      <aside v-if="drawerOpen" class="drawer">
        <!-- 抽屉头部：头像 + 用户名 -->
        <div class="drawer-header">
          <el-avatar :size="48" :src="userStore.loginUser?.userAvatar" />
          <div class="drawer-user-info">
            <div class="drawer-username">{{ userStore.loginUser?.userName || '用户' }}</div>
            <div class="drawer-role">{{ roleText }}</div>
          </div>
          <button class="drawer-close" @click="drawerOpen = false">✕</button>
        </div>

        <!-- 抽屉导航链接 -->
        <nav class="drawer-nav">
          <router-link to="/task/list" class="drawer-link" @click="drawerOpen = false">
            <el-icon><Search /></el-icon> 任务大厅
          </router-link>
          <router-link v-if="!userStore.isAdmin" to="/orders" class="drawer-link" @click="drawerOpen = false">
            <el-icon><Tickets /></el-icon> 我的订单
          </router-link>
          <router-link v-if="userStore.isRunner" to="/runner/tasks" class="drawer-link" @click="drawerOpen = false">
            <el-icon><Bicycle /></el-icon> 接单中心
          </router-link>
          <router-link v-if="!userStore.isAdmin" to="/reviews" class="drawer-link" @click="drawerOpen = false">
            <el-icon><Star /></el-icon> 评价中心
          </router-link>
          <router-link v-if="userStore.isPublisher" to="/task/publish" class="drawer-link drawer-link-primary" @click="drawerOpen = false">
            <el-icon><Edit /></el-icon> 发布任务
          </router-link>

          <!-- 管理员专属 -->
          <template v-if="userStore.isAdmin">
            <div class="drawer-divider">管理功能</div>
            <router-link to="/admin/dashboard" class="drawer-link drawer-link-admin" @click="drawerOpen = false">
              <el-icon><DataAnalysis /></el-icon> 数据分析
            </router-link>
            <router-link to="/admin/users" class="drawer-link drawer-link-admin" @click="drawerOpen = false">
              <el-icon><UserFilled /></el-icon> 用户管理
            </router-link>
            <router-link to="/admin/feedback" class="drawer-link drawer-link-admin" @click="drawerOpen = false">
              <el-icon><ChatDotSquare /></el-icon> 反馈管理
            </router-link>
          </template>

          <div class="drawer-divider">账号</div>
          <router-link to="/profile" class="drawer-link" @click="drawerOpen = false">
            <el-icon><User /></el-icon> 个人中心
          </router-link>
          <router-link to="/logs" class="drawer-link" @click="drawerOpen = false">
            <el-icon><Document /></el-icon> 操作记录
          </router-link>
          <router-link v-if="!userStore.isAdmin" to="/feedback" class="drawer-link" @click="drawerOpen = false">
            <el-icon><ChatDotSquare /></el-icon> 意见反馈
          </router-link>
          <button class="drawer-link drawer-link-logout" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出登录
          </button>
        </nav>
      </aside>
    </transition>

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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listMyMessages } from '@/api/message'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
const drawerOpen = ref(false)

const roleText = computed(() => {
  const role = userStore.loginUser?.userRole
  const map = { admin: '管理员', runner: '跑腿员', user: '普通用户', publisher: '发布者' }
  return map[role] || ''
})

const fetchUnread = async () => {
  try {
    const res = await listMyMessages({ current: 1, pageSize: 1, isRead: 0 })
    unreadCount.value = res.total || 0
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchUnread()
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
  }
}

const handleLogout = async () => {
  drawerOpen.value = false
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

/* ========== 顶部导航栏 ========== */
.app-header {
  position: fixed;
  top: 0; left: 0; right: 0;
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
.header-left .logo:hover { color: var(--color-text-primary); }

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
.nav-admin { color: var(--color-warning); }

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
.user-avatar-btn:hover { background: var(--color-bg-hover); }
.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 汉堡按钮 ========== */
.hamburger {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 8px;
  padding: 6px;
  transition: background var(--transition-fast);
}
.hamburger:hover { background: var(--color-bg-hover); }
.hamburger span {
  display: block;
  width: 20px;
  height: 2px;
  background: var(--color-text-primary);
  border-radius: 2px;
  transition: all var(--transition-fast);
}

/* ========== 遮罩层 ========== */
.drawer-overlay {
  position: fixed;
  inset: 0;
  z-index: 1100;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
}

/* ========== 侧滑抽屉 ========== */
.drawer {
  position: fixed;
  top: 0; right: 0;
  z-index: 1200;
  width: 280px;
  height: 100vh;
  background: var(--color-bg-card);
  box-shadow: -4px 0 24px rgba(0,0,0,0.12);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px 20px;
  background: var(--color-primary-gradient);
  color: #fff;
}
.drawer-user-info { flex: 1; min-width: 0; }
.drawer-username {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.drawer-role {
  font-size: 12px;
  color: rgba(255,255,255,0.75);
  margin-top: 2px;
}
.drawer-close {
  background: rgba(255,255,255,0.2);
  border: none;
  color: #fff;
  width: 28px; height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  transition: background var(--transition-fast);
}
.drawer-close:hover { background: rgba(255,255,255,0.3); }

.drawer-nav {
  padding: 12px 0;
  flex: 1;
}

.drawer-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 20px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
  text-decoration: none;
  transition: all var(--transition-fast);
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
}
.drawer-link:hover { background: var(--color-bg-hover); }
.drawer-link.router-link-active {
  color: var(--color-primary);
  background: rgba(0, 113, 227, 0.06);
}
.drawer-link-primary { color: var(--color-primary); }
.drawer-link-admin { color: var(--color-warning); }
.drawer-link-logout { color: var(--color-danger); }

.drawer-divider {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  padding: 12px 20px 4px;
}

/* ========== 主内容 ========== */
.app-main {
  padding-top: var(--header-height);
  min-height: 100vh;
}

/* ========== 显示/隐藏控制 ========== */
.pc-only { display: flex; }
.mobile-only { display: none; }

@media (max-width: 768px) {
  .header-content { padding: 0 16px; }
  .pc-only { display: none !important; }
  .mobile-only { display: flex !important; }
}

/* ========== 动画 ========== */
.overlay-fade-enter-active,
.overlay-fade-leave-active { transition: opacity 0.25s ease; }
.overlay-fade-enter-from,
.overlay-fade-leave-to { opacity: 0; }

.drawer-slide-enter-active,
.drawer-slide-leave-active { transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94); }
.drawer-slide-enter-from,
.drawer-slide-leave-to { transform: translateX(100%); }
</style>
