<template>
  <div class="page-container">
    <h1 class="page-title">接单中心</h1>
    <p class="page-subtitle">浏览待接单任务，选择你感兴趣的任务</p>

    <!-- 筛选栏 -->
    <div class="apple-card filter-bar">
      <el-select v-model="query.taskType" placeholder="任务类型" clearable @change="loadData" style="width: 140px">
        <el-option label="文件传递" value="file_delivery" />
        <el-option label="物品采购" value="goods_purchase" />
        <el-option label="餐饮配送" value="food_delivery" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-select v-model="query.sortField" placeholder="排序方式" @change="loadData" style="width: 140px">
        <el-option label="最新发布" value="createTime" />
        <el-option label="报酬最高" value="reward" />
      </el-select>
      <el-button type="primary" round @click="loadData">刷新</el-button>
    </div>

    <div v-if="tasks.length" class="task-grid">
      <div v-for="task in tasks" :key="task.id" class="apple-card task-card">
        <div class="task-card-top">
          <el-tag size="small" round type="info">{{ task.taskTypeText }}</el-tag>
          <span class="task-reward">¥{{ task.reward }}</span>
        </div>
        <h3 class="task-title">{{ task.title }}</h3>
        <p class="task-desc">{{ task.description?.slice(0, 80) }}</p>
        <div class="task-info">
          <span><el-icon><Location /></el-icon>{{ task.startLocation }} → {{ task.endLocation }}</span>
          <span><el-icon><Timer /></el-icon>{{ task.expectedTime }}</span>
        </div>
        <div class="task-bottom">
          <div class="task-publisher">
            <el-avatar :size="24" :src="task.publisherAvatar" />
            <span>{{ task.publisherName }}</span>
          </div>
          <el-button type="primary" round size="small" @click="handleAccept(task.id)" :loading="acceptingId === task.id">
            接单
          </el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无待接单任务" />

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listAvailableTasks, acceptTask } from '@/api/runner'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Timer } from '@element-plus/icons-vue'

const query = ref({ current: 1, pageSize: 12, taskType: '', sortField: 'createTime', sortOrder: 'descend' })
const tasks = ref([])
const total = ref(0)
const acceptingId = ref(null)

const loadData = async () => {
  const res = await listAvailableTasks(query.value)
  tasks.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)

const handleAccept = async (taskId) => {
  await ElMessageBox.confirm('确定接受此任务？', '接单确认', { type: 'info' })
  acceptingId.value = taskId
  try {
    await acceptTask(taskId)
    ElMessage.success('接单成功！')
    loadData()
  } finally { acceptingId.value = null }
}
</script>

<style scoped>
.filter-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 24px; padding: 16px 20px; }
.task-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.task-card { padding: 20px; transition: all var(--transition-normal); }
.task-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
.task-card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.task-reward { font-size: 20px; font-weight: 700; color: var(--color-primary); }
.task-title { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.task-desc { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 12px; }
.task-info { font-size: 12px; color: var(--color-text-tertiary); display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
.task-info span { display: flex; align-items: center; gap: 4px; }
.task-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid var(--color-border-light); }
.task-publisher { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--color-text-secondary); }
</style>
