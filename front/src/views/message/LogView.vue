<template>
  <div class="page-container">
    <h1 class="page-title">操作记录</h1>
    <p class="page-subtitle">查看你的操作历史</p>

    <div v-if="logs.length" class="log-list">
      <div v-for="log in logs" :key="log.id" class="apple-card log-card">
        <div class="log-header">
          <span class="log-action">{{ log.action }}</span>
          <span class="log-time">{{ log.createTime }}</span>
        </div>
        <p>{{ log.content }}</p>
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
