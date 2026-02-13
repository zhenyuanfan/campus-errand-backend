<template>
  <div class="page-container">
    <h1 class="page-title">任务大厅</h1>
    <p class="page-subtitle">浏览和筛选所有跑腿任务</p>

    <!-- 筛选栏 -->
    <div class="apple-card filter-bar">
      <el-input v-model="query.title" placeholder="搜索任务标题" clearable style="width: 240px" :prefix-icon="Search" @clear="loadData" @keyup.enter="loadData" />
      <el-select v-model="query.taskType" placeholder="任务类型" clearable @change="loadData" style="width: 140px">
        <el-option label="文件传递" value="file_delivery" />
        <el-option label="物品采购" value="goods_purchase" />
        <el-option label="餐饮配送" value="food_delivery" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-select v-model="query.status" placeholder="任务状态" clearable @change="loadData" style="width: 130px">
        <el-option label="待接单" value="pending" />
        <el-option label="已接单" value="accepted" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" round @click="loadData"><el-icon><Search /></el-icon>搜索</el-button>
    </div>

    <!-- 任务列表 -->
    <div v-if="tasks.length" class="task-grid">
      <div v-for="task in tasks" :key="task.id" class="apple-card task-card" @click="$router.push(`/task/${task.id}`)">
        <div class="task-card-top">
          <el-tag :type="statusType(task.status)" size="small" round>{{ task.statusText }}</el-tag>
          <el-tag size="small" round type="info">{{ task.taskTypeText }}</el-tag>
        </div>
        <h3 class="task-title">{{ task.title }}</h3>
        <p class="task-desc">{{ task.description?.slice(0, 60) }}{{ task.description?.length > 60 ? '...' : '' }}</p>
        <div class="task-info">
          <span><el-icon><Location /></el-icon>{{ task.startLocation }} → {{ task.endLocation }}</span>
          <span><el-icon><Timer /></el-icon>{{ task.expectedTime }}</span>
        </div>
        <div class="task-bottom">
          <div class="task-publisher">
            <el-avatar :size="24" :src="task.publisherAvatar" />
            <span>{{ task.publisherName }}</span>
          </div>
          <span class="task-reward">¥{{ task.reward }}</span>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无任务" />

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listTaskVOByPage } from '@/api/task'
import { Search, Location, Timer } from '@element-plus/icons-vue'

const query = ref({ current: 1, pageSize: 12, title: '', taskType: '', status: '' })
const tasks = ref([])
const total = ref(0)

const loadData = async () => {
  const params = { current: query.value.current, pageSize: query.value.pageSize }
  if (query.value.title) params.title = query.value.title
  if (query.value.taskType) params.taskType = query.value.taskType
  if (query.value.status) params.status = query.value.status
  const res = await listTaskVOByPage(params)
  tasks.value = res.records || []
  total.value = res.total || 0
}

onMounted(loadData)

const statusType = (s) => ({ pending: 'info', accepted: '', in_progress: 'warning', completed: 'success', cancelled: 'danger' }[s] || 'info')
</script>

<style scoped>
.filter-bar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin-bottom: 24px; padding: 16px 20px; }
.task-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.task-card { cursor: pointer; padding: 20px; transition: all var(--transition-normal); }
.task-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
.task-card-top { display: flex; gap: 8px; margin-bottom: 10px; }
.task-title { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.task-desc { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 12px; line-height: 1.6; }
.task-info { font-size: 12px; color: var(--color-text-tertiary); display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
.task-info span { display: flex; align-items: center; gap: 4px; }
.task-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid var(--color-border-light); }
.task-publisher { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--color-text-secondary); }
.task-reward { font-size: 20px; font-weight: 700; color: var(--color-primary); }
</style>
