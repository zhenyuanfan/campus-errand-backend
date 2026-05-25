<template>
  <div class="page-container">
    <h1 class="page-title">操作记录</h1>
    <p class="page-subtitle">查看你的操作历史</p>

    <div v-if="logs.length" class="log-list">
      <div v-for="log in logs" :key="log.id" class="apple-card log-card">
        <div class="log-header">
          <span class="log-action">{{ actionMap[log.action] || log.action }}</span>
          <span class="log-time">{{ log.createTime }}</span>
        </div>
        <p>{{ log.detail }}</p>
      </div>
    </div>
    <el-empty v-else description="暂无操作记录" />

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listMyOperationLog } from '@/api/message'

// 英文 action 到中文的映射
const actionMap = {
  REGISTER: '用户注册',
  LOGIN: '账号登录',
  LOGIN_SMS: '短信登录',
  UPDATE_PROFILE: '更新资料',
  UPLOAD_AVATAR: '上传头像',
  BIND_PHONE: '绑定手机',
  ADMIN_UPDATE_ROLE: '修改角色',
  ADMIN_DELETE_USER: '删除用户',
  PUBLISH_TASK: '发布任务',
  UPDATE_TASK: '更新任务',
  DELETE_TASK: '删除任务',
  CANCEL_TASK: '取消任务',
  ACCEPT_TASK: '接受任务',
  REVIEW_TASK: '评价任务',
  REPLY_REVIEW: '回复评价',
  SUBMIT_FEEDBACK: '提交反馈',
  REPLY_FEEDBACK: '回复反馈',
  UPDATE_DELIVERY_STATUS: '更新配送状态'
}

const query = ref({ current: 1, pageSize: 20 })
const logs = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await listMyOperationLog(query.value)
  logs.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)
</script>

<style scoped>
.log-list { display: flex; flex-direction: column; gap: 8px; }
.log-card { padding: 16px 20px; }
.log-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.log-action { font-weight: 600; font-size: 14px; color: var(--color-primary); }
.log-time { font-size: 12px; color: var(--color-text-tertiary); }
.log-card p { font-size: 14px; color: var(--color-text-secondary); }
</style>
