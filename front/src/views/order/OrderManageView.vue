<template>
  <div class="page-container">
    <h1 class="page-title">订单管理</h1>
    <p class="page-subtitle">查看历史订单记录、订单详情、评价信息</p>

    <div class="tab-switch">
      <button :class="['tab-btn', { active: tab === 'published' }]" @click="tab = 'published'; loadData()">我发布的</button>
      <button :class="['tab-btn', { active: tab === 'runner' }]" @click="tab = 'runner'; loadData()">我接的</button>
    </div>

    <!-- 筛选 -->
    <div class="apple-card filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索订单" clearable style="width: 200px" @keyup.enter="loadData" />
      <el-select v-model="query.status" placeholder="订单状态" clearable @change="loadData" style="width: 130px">
        <el-option label="待接单" value="pending" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" round @click="loadData">查询</el-button>
    </div>

    <el-table :data="orders" stripe style="width: 100%; border-radius: 12px;">
      <el-table-column prop="title" label="任务标题" min-width="180">
        <template #default="{ row }">
          <span class="link-text" @click="viewDetail(row.id)">{{ row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="taskTypeText" label="类型" width="100" />
      <el-table-column prop="statusText" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" round size="small">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reward" label="报酬" width="100">
        <template #default="{ row }"><span style="font-weight:600;color:var(--color-primary)">¥{{ row.reward }}</span></template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="viewDetail(row.id)">详情</el-button>
          <el-button size="small" text type="primary" @click="$router.push(`/delivery/${row.id}`)">配送</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDetail" title="订单详情" width="600px">
      <div v-if="detail">
        <div class="detail-row"><span>标题</span><span>{{ detail.title }}</span></div>
        <div class="detail-row"><span>类型</span><span>{{ detail.taskTypeText }}</span></div>
        <div class="detail-row"><span>状态</span><el-tag :type="statusType(detail.status)" round size="small">{{ detail.statusText }}</el-tag></div>
        <div class="detail-row"><span>报酬</span><span style="font-weight:700;color:var(--color-primary)">¥{{ detail.reward }}</span></div>
        <div class="detail-row"><span>路线</span><span>{{ detail.startLocation }} → {{ detail.endLocation }}</span></div>
        <div class="detail-row"><span>发布时间</span><span>{{ detail.createTime }}</span></div>
        <div v-if="detail.reviewContent" style="margin-top:16px;padding-top:16px;border-top:1px solid var(--color-border-light)">
          <h4 style="margin-bottom:8px;">评价信息</h4>
          <div class="detail-row"><span>评分</span><el-rate :model-value="detail.reviewScore" disabled /></div>
          <div class="detail-row"><span>内容</span><span>{{ detail.reviewContent }}</span></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listMyPublishedOrders, listMyRunnerOrders, getOrderDetail } from '@/api/order'

const tab = ref('published')
const query = ref({ current: 1, pageSize: 10, keyword: '', status: '' })
const orders = ref([])
const total = ref(0)
const showDetail = ref(false)
const detail = ref(null)

const loadData = async () => {
  const fn = tab.value === 'published' ? listMyPublishedOrders : listMyRunnerOrders
  const res = await fn(query.value)
  orders.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)

const viewDetail = async (taskId) => {
  detail.value = await getOrderDetail(taskId)
  showDetail.value = true
}

const statusType = (s) => ({ pending: 'info', accepted: '', in_progress: 'warning', completed: 'success', cancelled: 'danger' }[s] || 'info')
</script>

<style scoped>
.tab-switch { display: flex; background: var(--color-bg-hover); border-radius: 980px; padding: 3px; margin-bottom: 20px; width: fit-content; }
.tab-btn { padding: 8px 24px; border: none; background: none; border-radius: 980px; font-size: 14px; font-weight: 500; color: var(--color-text-secondary); cursor: pointer; transition: all var(--transition-fast); }
.tab-btn.active { background: #fff; color: var(--color-primary); box-shadow: var(--shadow-sm); }
.filter-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; padding: 16px 20px; }
.link-text { cursor: pointer; color: var(--color-primary); font-weight: 500; }
.detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--color-border-light); font-size: 14px; }
.detail-row span:first-child { color: var(--color-text-secondary); }
</style>
