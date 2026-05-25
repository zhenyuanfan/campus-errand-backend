<template>
  <div class="page-container">
    <h1 class="page-title">接单中心</h1>
    <p class="page-subtitle">浏览待接单任务，选择你感兴趣的任务</p>

    <!-- 筛选栏 -->
    <div class="apple-card filter-bar">
      <div class="filter-left">
        <el-select v-model="query.taskType" placeholder="任务类型" clearable @change="loadData" style="width: 140px">
          <el-option label="文件传递" value="file_delivery" />
          <el-option label="物品采购" value="goods_purchase" />
          <el-option label="餐饮配送" value="food_delivery" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-select v-model="query.sortField" placeholder="排序方式" @change="handleSortChange" style="width: 140px"
          :disabled="useRecommend">
          <el-option label="最新发布" value="createTime" />
          <el-option label="报酬最高" value="reward" />
        </el-select>
        <el-button type="primary" round @click="loadData">刷新</el-button>
      </div>
      <div class="filter-right">
        <el-tooltip :content="useRecommend ? '已开启智能推荐，根据你的接单偏好排序' : '当前为普通排序'" placement="top">
          <div class="recommend-switch" :class="{ active: useRecommend }" @click="toggleRecommend">
            <el-icon :size="16"><MagicStick /></el-icon>
            <span>智能推荐</span>
            <el-switch v-model="useRecommend" size="small" @click.stop @change="loadData" />
          </div>
        </el-tooltip>
      </div>
    </div>

    <!-- 推荐提示 -->
    <div v-if="useRecommend" class="recommend-hint">
      <el-icon><InfoFilled /></el-icon>
      <span>根据你的历史接单偏好，为你智能推荐更匹配的任务</span>
    </div>

    <div v-if="tasks.length" class="task-grid">
      <div v-for="task in tasks" :key="task.id" class="apple-card task-card">
        <div class="task-card-top">
          <div class="tag-group">
            <el-tag size="small" round type="info">{{ task.taskTypeText }}</el-tag>
            <el-tag v-if="useRecommend && isPreferred(task.taskType)" size="small" round effect="dark"
              class="recommend-tag">
              <el-icon :size="12"><Star /></el-icon> 为你推荐
            </el-tag>
          </div>
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
import { listAvailableTasks, listRecommendedTasks, acceptTask } from '@/api/runner'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Timer, MagicStick, Star, InfoFilled } from '@element-plus/icons-vue'

const query = ref({ current: 1, pageSize: 12, taskType: '', sortField: 'createTime', sortOrder: 'descend' })
const tasks = ref([])
const total = ref(0)
const acceptingId = ref(null)
const useRecommend = ref(true) // 默认开启智能推荐
const preferredTypes = ref([]) // 接单员偏好的任务类型列表

const loadData = async () => {
  try {
    let res
    if (useRecommend.value) {
      // 使用推荐接口（不传 sortField，由后端按偏好权重排序）
      const recommendQuery = { ...query.value }
      delete recommendQuery.sortField
      delete recommendQuery.sortOrder
      res = await listRecommendedTasks(recommendQuery)
    } else {
      // 使用普通接口
      res = await listAvailableTasks(query.value)
    }
    tasks.value = res.records || []
    total.value = res.total || 0

    // 从推荐结果中提取偏好类型（排在前面的类型就是偏好类型）
    if (useRecommend.value && tasks.value.length > 0) {
      extractPreferredTypes()
    }
  } catch { /* ignore */ }
}

// 从已返回的任务列表中提取偏好类型
const extractPreferredTypes = () => {
  const typeCount = {}
  tasks.value.forEach(t => {
    typeCount[t.taskType] = (typeCount[t.taskType] || 0) + 1
  })
  // 超过 1 条的类型认为是偏好类型（简单策略）
  preferredTypes.value = Object.keys(typeCount).filter(t => typeCount[t] >= 1)
  // 只取前 2 个作为偏好标注，避免全部标注
  if (preferredTypes.value.length > 2) {
    preferredTypes.value = preferredTypes.value.slice(0, 2)
  }
}

const isPreferred = (taskType) => {
  return preferredTypes.value.includes(taskType)
}

const toggleRecommend = () => {
  useRecommend.value = !useRecommend.value
  query.value.current = 1
  loadData()
}

const handleSortChange = () => {
  if (!useRecommend.value) {
    loadData()
  }
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
.filter-bar { display: flex; gap: 12px; align-items: center; justify-content: space-between; margin-bottom: 16px; padding: 16px 20px; flex-wrap: wrap; }
.filter-left { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.filter-right { display: flex; align-items: center; }

.recommend-switch {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 20px;
  background: var(--color-bg-secondary, #f5f5f7);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary, #86868b);
  transition: all 0.3s ease;
  user-select: none;
}
.recommend-switch.active {
  background: linear-gradient(135deg, rgba(110, 92, 230, 0.12), rgba(0, 113, 227, 0.12));
  color: var(--color-primary, #0071e3);
}
.recommend-switch:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.recommend-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  margin-bottom: 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(110, 92, 230, 0.06), rgba(0, 113, 227, 0.06));
  color: var(--color-primary, #0071e3);
  font-size: 13px;
  font-weight: 500;
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

.task-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.task-card { padding: 20px; transition: all var(--transition-normal); }
.task-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
.task-card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.tag-group { display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
.recommend-tag {
  background: linear-gradient(135deg, #6e5ce6, #0071e3) !important;
  border: none !important;
  color: #fff !important;
  font-size: 11px;
}
.recommend-tag .el-icon { vertical-align: -1px; }
.task-reward { font-size: 20px; font-weight: 700; color: var(--color-primary); }
.task-title { font-size: 16px; font-weight: 600; margin-bottom: 6px; }
.task-desc { font-size: 13px; color: var(--color-text-secondary); margin-bottom: 12px; }
.task-info { font-size: 12px; color: var(--color-text-tertiary); display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
.task-info span { display: flex; align-items: center; gap: 4px; }
.task-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid var(--color-border-light); }
.task-publisher { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--color-text-secondary); }

@media (max-width: 768px) {
  .filter-bar { padding: 12px 14px; gap: 8px; flex-direction: column; align-items: stretch; }
  .filter-left { flex-direction: column; }
  .filter-left .el-select { width: 100% !important; }
  .filter-right { justify-content: center; }
  .task-grid { grid-template-columns: 1fr; gap: 12px; }
  .recommend-hint { font-size: 12px; padding: 8px 12px; }
}
</style>
