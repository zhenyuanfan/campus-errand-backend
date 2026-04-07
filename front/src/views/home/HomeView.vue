<template>
  <div class="page-container">
    <div class="home-hero">
      <h1 class="hero-title">欢迎回来，{{ userStore.loginUser?.userName || '同学' }} 👋</h1>
      <p class="page-subtitle" v-if="!userStore.isAdmin">校园跑腿，让生活更便捷</p>
      <p class="page-subtitle" v-else>管理后台 · 平台运营管理中心</p>
    </div>

    <!-- 管理员快捷操作 -->
    <div v-if="userStore.isAdmin" class="quick-actions">
      <div class="action-card apple-card" @click="$router.push('/admin/dashboard')">
        <div class="action-icon" style="background: rgba(0, 113, 227, 0.1);">
          <el-icon :size="28" color="#0071e3"><DataAnalysis /></el-icon>
        </div>
        <h3>数据分析</h3>
        <p>查看平台运营数据</p>
      </div>
      <div class="action-card apple-card" @click="$router.push('/admin/users')">
        <div class="action-icon" style="background: rgba(110, 92, 230, 0.1);">
          <el-icon :size="28" color="#6e5ce6"><UserFilled /></el-icon>
        </div>
        <h3>用户管理</h3>
        <p>管理平台用户</p>
      </div>
      <div class="action-card apple-card" @click="$router.push('/admin/feedback')">
        <div class="action-icon" style="background: rgba(255, 159, 10, 0.1);">
          <el-icon :size="28" color="#ff9f0a"><ChatDotSquare /></el-icon>
        </div>
        <h3>反馈管理</h3>
        <p>处理用户反馈</p>
      </div>
      <div class="action-card apple-card" @click="$router.push('/task/list')">
        <div class="action-icon" style="background: rgba(48, 209, 88, 0.1);">
          <el-icon :size="28" color="#30d158"><Search /></el-icon>
        </div>
        <h3>任务大厅</h3>
        <p>监管平台任务</p>
      </div>
    </div>

    <!-- 普通用户快捷操作 -->
    <div v-else class="quick-actions">
      <div class="action-card apple-card" @click="$router.push('/task/publish')">
        <div class="action-icon" style="background: rgba(0, 113, 227, 0.1);">
          <el-icon :size="28" color="#0071e3"><Edit /></el-icon>
        </div>
        <h3>发布任务</h3>
        <p>发布跑腿需求</p>
      </div>
      <div class="action-card apple-card" @click="$router.push('/task/list')">
        <div class="action-icon" style="background: rgba(48, 209, 88, 0.1);">
          <el-icon :size="28" color="#30d158"><Search /></el-icon>
        </div>
        <h3>任务大厅</h3>
        <p>浏览全部任务</p>
      </div>
      <div class="action-card apple-card" @click="$router.push('/orders')">
        <div class="action-icon" style="background: rgba(255, 159, 10, 0.1);">
          <el-icon :size="28" color="#ff9f0a"><Tickets /></el-icon>
        </div>
        <h3>我的订单</h3>
        <p>查看订单状态</p>
      </div>
      <div v-if="userStore.isRunner" class="action-card apple-card" @click="$router.push('/runner/tasks')">
        <div class="action-icon" style="background: rgba(110, 92, 230, 0.1);">
          <el-icon :size="28" color="#6e5ce6"><Bicycle /></el-icon>
        </div>
        <h3>去接单</h3>
        <p>查看待接任务</p>
      </div>
    </div>

    <!-- 我发布的最近任务（仅普通用户显示） -->
    <div v-if="!userStore.isAdmin" class="section">
      <div class="section-header">
        <h2>我发布的任务</h2>
        <el-button text type="primary" @click="$router.push('/task/list')">查看全部 →</el-button>
      </div>
      <div v-if="myTasks.length" class="task-grid">
        <div v-for="task in myTasks" :key="task.id" class="apple-card task-card"
          @click="$router.push(`/task/${task.id}`)">
          <div class="task-card-header">
            <el-tag :type="statusType(task.status)" size="small" round>{{ task.statusText }}</el-tag>
            <span class="task-reward">¥{{ task.reward }}</span>
          </div>
          <h4 class="task-title">{{ task.title }}</h4>
          <p class="task-desc">{{ task.taskTypeText }}</p>
          <div class="task-meta">
            <span><el-icon><Location /></el-icon>{{ task.startLocation }} → {{ task.endLocation }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无发布的任务" :image-size="100" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { listMyTaskVOByPage } from '@/api/task'

const userStore = useUserStore()
const myTasks = ref([])

onMounted(async () => {
  // 管理员不需要加载"我发布的任务"
  if (userStore.isAdmin) return
  try {
    const res = await listMyTaskVOByPage({ current: 1, pageSize: 6 })
    myTasks.value = res.records || []
  } catch { /* ignore */ }
})

const statusType = (status) => {
  const map = { pending: 'info', accepted: '', in_progress: 'warning', completed: 'success', cancelled: 'danger' }
  return map[status] || 'info'
}
</script>

<style scoped>
.home-hero {
  padding: 40px 0 16px;
}
.hero-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.04em;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 40px;
}

@media (max-width: 768px) {
  .home-hero { padding: 24px 0 12px; }
  .hero-title { font-size: 24px; }
  .quick-actions {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 24px;
  }
  .action-card { padding: 20px 12px; }
  .action-icon { width: 44px; height: 44px; margin-bottom: 10px; }
  .action-card h3 { font-size: 14px; }
  .task-grid { grid-template-columns: 1fr; }
}
.action-card {
  cursor: pointer;
  text-align: center;
  padding: 28px 20px;
  transition: all var(--transition-normal);
}
.action-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.action-icon {
  width: 56px; height: 56px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 14px;
}
.action-card h3 {
  font-size: 16px; font-weight: 600; margin-bottom: 4px;
}
.action-card p {
  font-size: 13px; color: var(--color-text-tertiary);
}

.section { margin-bottom: 40px; }
.section-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;
}
.section-header h2 { font-size: 20px; font-weight: 600; }

.task-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;
}
.task-card {
  cursor: pointer; padding: 20px;
}
.task-card:hover { transform: translateY(-2px); }
.task-card-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;
}
.task-reward { font-size: 18px; font-weight: 700; color: var(--color-primary); }
.task-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.task-desc { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 8px; }
.task-meta {
  font-size: 12px; color: var(--color-text-tertiary);
  display: flex; align-items: center; gap: 4px;
}
</style>
