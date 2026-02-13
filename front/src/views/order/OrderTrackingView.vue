<template>
  <div class="page-container">
    <h1 class="page-title">我的订单</h1>
    <p class="page-subtitle">实时跟踪订单状态</p>

    <div v-if="orders.length" class="order-list">
      <div v-for="order in orders" :key="order.id" class="apple-card order-card">
        <div class="order-header">
          <el-tag :type="statusType(order.status)" round size="small">{{ order.statusText }}</el-tag>
          <span class="order-time">{{ order.createTime }}</span>
        </div>
        <h3 @click="$router.push(`/task/${order.id}`)">{{ order.title }}</h3>
        <div class="order-info">
          <span>{{ order.taskTypeText }}</span>
          <span>{{ order.startLocation }} → {{ order.endLocation }}</span>
        </div>
        <div class="order-footer">
          <span class="order-reward">¥{{ order.reward }}</span>
          <div class="order-actions">
            <el-button size="small" text type="primary" @click="$router.push(`/delivery/${order.id}`)">配送跟踪</el-button>
            <el-button size="small" text type="primary" @click="$router.push(`/task/${order.id}`)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无订单" />

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listMyOrders } from '@/api/order'

const query = ref({ current: 1, pageSize: 10 })
const orders = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await listMyOrders(query.value)
  orders.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)

const statusType = (s) => ({ pending: 'info', accepted: '', in_progress: 'warning', completed: 'success', cancelled: 'danger' }[s] || 'info')
</script>

<style scoped>
.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-card { padding: 20px; transition: all var(--transition-normal); }
.order-card:hover { box-shadow: var(--shadow-lg); }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.order-time { font-size: 12px; color: var(--color-text-tertiary); }
.order-card h3 { font-size: 16px; font-weight: 600; margin-bottom: 8px; cursor: pointer; }
.order-card h3:hover { color: var(--color-primary); }
.order-info { font-size: 13px; color: var(--color-text-secondary); display: flex; gap: 16px; margin-bottom: 12px; }
.order-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid var(--color-border-light); }
.order-reward { font-size: 18px; font-weight: 700; color: var(--color-primary); }
.order-actions { display: flex; gap: 4px; }
</style>
