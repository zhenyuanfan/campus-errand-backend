<template>
  <div class="page-container" v-if="task">
    <el-button text @click="$router.back()" style="margin-bottom: 16px;">← 返回</el-button>
    <div class="detail-grid">
      <div class="apple-card detail-main">
        <div class="detail-header">
          <div>
            <el-tag :type="statusType(task.status)" round size="large">{{ task.statusText }}</el-tag>
            <el-tag round size="large" type="info" style="margin-left: 8px;">{{ task.taskTypeText }}</el-tag>
          </div>
          <span class="detail-reward">¥{{ task.reward }}</span>
        </div>
        <h1 class="detail-title">{{ task.title }}</h1>
        <p class="detail-desc">{{ task.description }}</p>

        <div class="detail-info-grid">
          <div class="info-item"><span class="info-label">起始地点</span><span>{{ task.startLocation }}</span></div>
          <div class="info-item"><span class="info-label">目的地</span><span>{{ task.endLocation }}</span></div>
          <div class="info-item"><span class="info-label">期望时间</span><span>{{ task.expectedTime }}</span></div>
          <div class="info-item"><span class="info-label">联系方式</span><span>{{ task.contactInfo }}</span></div>
          <div class="info-item" v-if="task.remark"><span class="info-label">备注</span><span>{{ task.remark }}</span></div>
          <div class="info-item"><span class="info-label">发布时间</span><span>{{ task.createTime }}</span></div>
        </div>

        <!-- 操作按钮 -->
        <div class="detail-actions" v-if="isOwner">
          <el-button v-if="task.status === 'pending'" type="danger" round @click="handleCancel">取消任务</el-button>
          <el-button v-if="task.status === 'pending'" round @click="showEdit = true">编辑任务</el-button>
          <el-button v-if="task.status === 'completed'" type="primary" round @click="$router.push(`/delivery/${task.id}`)">查看配送记录</el-button>
        </div>
      </div>

      <!-- 发布者/跑腿人员信息 -->
      <div class="side-cards">
        <div class="apple-card">
          <h3 class="card-title">发布者</h3>
          <div class="user-info">
            <el-avatar :size="48" :src="task.publisherAvatar" />
            <div><div class="user-name">{{ task.publisherName }}</div></div>
          </div>
        </div>
        <div class="apple-card" v-if="task.runnerName">
          <h3 class="card-title">跑腿人员</h3>
          <div class="user-info">
            <el-avatar :size="48" :src="task.runnerAvatar" />
            <div>
              <div class="user-name">{{ task.runnerName }}</div>
              <el-button v-if="task.status === 'in_progress' || task.status === 'completed'" size="small" text type="primary"
                @click="$router.push(`/delivery/${task.id}`)">查看配送</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getTaskVO, cancelTask } from '@/api/task'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const task = ref(null)
const showEdit = ref(false)

const isOwner = computed(() => task.value && userStore.loginUser?.id === task.value.publisherId)

onMounted(async () => {
  task.value = await getTaskVO(route.params.id)
})

const handleCancel = async () => {
  await ElMessageBox.confirm('确定取消此任务？', '提示', { type: 'warning' })
  await cancelTask(task.value.id)
  ElMessage.success('任务已取消')
  task.value = await getTaskVO(route.params.id)
}

const statusType = (s) => ({ pending: 'info', accepted: '', in_progress: 'warning', completed: 'success', cancelled: 'danger' }[s] || 'info')
</script>

<style scoped>
.detail-grid { display: grid; grid-template-columns: 1fr 300px; gap: 20px; }
.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.detail-reward { font-size: 28px; font-weight: 700; color: var(--color-primary); }
.detail-title { font-size: 24px; font-weight: 700; margin-bottom: 12px; letter-spacing: -0.02em; }
.detail-desc { font-size: 15px; color: var(--color-text-secondary); line-height: 1.7; margin-bottom: 24px; }
.detail-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 12px; color: var(--color-text-tertiary); text-transform: uppercase; }
.detail-actions { display: flex; gap: 12px; padding-top: 20px; border-top: 1px solid var(--color-border-light); }
.side-cards { display: flex; flex-direction: column; gap: 16px; }
.card-title { font-size: 15px; font-weight: 600; margin-bottom: 12px; }
.user-info { display: flex; gap: 12px; align-items: center; }
.user-name { font-size: 15px; font-weight: 600; }
</style>
