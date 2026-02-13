<template>
  <div class="page-container">
    <h1 class="page-title">消息通知</h1>
    <p class="page-subtitle">查看系统通知和订单消息</p>

    <div v-if="messages.length" class="msg-list">
      <div v-for="msg in messages" :key="msg.id"
        :class="['apple-card msg-card', { unread: !msg.isRead }]" @click="handleRead(msg)">
        <div class="msg-header">
          <span class="msg-title">{{ msg.title }}</span>
          <span class="msg-time">{{ msg.createTime }}</span>
        </div>
        <p class="msg-content">{{ msg.content }}</p>
        <el-tag v-if="!msg.isRead" type="danger" round size="small">未读</el-tag>
      </div>
    </div>
    <el-empty v-else description="暂无消息" />

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listMyMessages, readMessage } from '@/api/message'

const query = ref({ current: 1, pageSize: 20 })
const messages = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await listMyMessages(query.value)
  messages.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)

const handleRead = async (msg) => {
  if (!msg.isRead) {
    await readMessage(msg.id)
    msg.isRead = 1
  }
}
</script>

<style scoped>
.msg-list { display: flex; flex-direction: column; gap: 8px; }
.msg-card { padding: 16px 20px; cursor: pointer; transition: all var(--transition-fast); }
.msg-card:hover { box-shadow: var(--shadow-md); }
.msg-card.unread { border-left: 3px solid var(--color-primary); }
.msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.msg-title { font-weight: 600; font-size: 15px; }
.msg-time { font-size: 12px; color: var(--color-text-tertiary); }
.msg-content { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }
</style>
