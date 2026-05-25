<template>
  <div class="page-container">
    <h1 class="page-title">评价中心</h1>
    <p class="page-subtitle">管理评价和查看评价统计</p>



    <!-- 评价列表 -->
    <div v-if="reviews.length" class="review-list">
      <div v-for="review in reviews" :key="review.id" class="apple-card review-card">
        <div class="review-header">
          <div class="review-user">
            <el-avatar :size="36" :src="review.reviewerAvatar" />
            <div>
              <span class="reviewer-name">{{ review.reviewerName }}</span>
              <span class="review-time">{{ review.createTime }}</span>
            </div>
          </div>
          <el-rate :model-value="review.score" disabled show-score text-color="#ff9f0a" />
        </div>
        
        <!-- 新增：任务与骑手信息展示 -->
        <div class="review-task-info">
          <span class="task-badge" @click="$router.push(`/task/${review.taskId}`)">
            🛍️ {{ review.taskTitle || `任务 #${review.taskId}` }}
          </span>
          <span v-if="tab === 'my' && review.runnerName" class="runner-badge">
            🚴 骑手: {{ review.runnerName }}
          </span>
        </div>

        <p class="review-content">{{ review.content }}</p>
        <div v-if="review.tags" class="review-tags">
          <el-tag v-for="tag in (review.tags || '').split(',')" :key="tag" size="small" round type="info">{{ tag }}</el-tag>
        </div>
        <!-- 回复 -->
        <div v-if="review.reply" class="review-reply">
          <el-avatar :size="24" :src="review.reply.replierAvatar" />
          <div>
            <span class="reply-name">{{ review.reply.replierName }}</span>
            <p>{{ review.reply.content }}</p>
          </div>
        </div>
        <!-- 回复按钮（仅跑腿人员且收到的评价） -->
        <div v-if="tab === 'received' && !review.reply" class="reply-action">
          <el-input v-model="replyContent[review.id]" placeholder="回复评价..." size="small" />
          <el-button type="primary" size="small" round @click="handleReply(review.id)" :loading="replyingId === review.id">回复</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无评价" />

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />

    <!-- 提交评价弹窗 -->
    <el-dialog v-model="showAdd" title="提交评价" width="500px">
      <el-form :model="addForm" label-position="top">
        <el-form-item label="选择任务">
          <el-select
            v-model="addForm.taskId"
            placeholder="请选择已完成的任务"
            style="width: 100%"
            :disabled="!!route.query.taskId"
            :loading="loadingTasks"
          >
            <el-option
              v-for="task in completedTasks"
              :key="task.id"
              :label="task.title"
              :value="task.id"
            >
              <span>{{ task.title }}</span>
              <span style="float: right; color: var(--color-text-tertiary); font-size: 12px;">¥{{ task.reward }}</span>
            </el-option>
          </el-select>
          <div v-if="!loadingTasks && !completedTasks.length" style="font-size: 12px; color: var(--color-text-tertiary); margin-top: 4px;">
            暂无可评价的已完成任务
          </div>
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="addForm.score" show-score text-color="#ff9f0a" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="addForm.content" type="textarea" :rows="3" placeholder="分享你的体验" />
        </el-form-item>
        <el-form-item label="标签（逗号分隔）">
          <el-input v-model="addForm.tags" placeholder="如：速度快,态度好" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false" round>取消</el-button>
        <el-button type="primary" round :loading="submitting" @click="handleAddReview">提交</el-button>
      </template>
    </el-dialog>

    <el-button v-if="!userStore.isRunner" type="primary" round class="fab" @click="openAddDialog">
      <el-icon><Edit /></el-icon>写评价
    </el-button>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listMyReviews, listReceivedReviews, addReview, addReviewReply } from '@/api/review'
import { listMyPublishedOrders } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

// 发布员/普通用户看"我的评价"，接单员看"收到的评价"
const tab = ref(userStore.isRunner ? 'received' : 'my')
const query = ref({ current: 1, pageSize: 10 })
const reviews = ref([])
const total = ref(0)
const showAdd = ref(false)
const submitting = ref(false)
const replyContent = reactive({})
const replyingId = ref(null)
const addForm = ref({ taskId: null, score: 5, content: '', tags: '' })
const completedTasks = ref([])
const loadingTasks = ref(false)

const loadData = async () => {
  const fn = tab.value === 'my' ? listMyReviews : listReceivedReviews
  const res = await fn(query.value)
  reviews.value = res.records || []
  total.value = res.total || 0
}
// 加载已完成的任务列表（供评价选择）
const fetchCompletedTasks = async () => {
  loadingTasks.value = true
  try {
    const res = await listMyPublishedOrders({ current: 1, pageSize: 20, status: 'completed' })
    completedTasks.value = res.records || []
  } catch { /* ignore */ }
  finally { loadingTasks.value = false }
}

const openAddDialog = async () => {
  await fetchCompletedTasks()
  showAdd.value = true
}

onMounted(() => {
  loadData()
  // 从订单详情跳过来时自动打开写评价弹窗
  if (route.query.taskId) {
    addForm.value.taskId = route.query.taskId
    openAddDialog()
  }
})

const handleAddReview = async () => {
  submitting.value = true
  try {
    await addReview(addForm.value)
    ElMessage.success('评价提交成功')
    showAdd.value = false
    addForm.value = { taskId: null, score: 5, content: '', tags: '' }
    loadData()
  } finally { submitting.value = false }
}

const handleReply = async (reviewId) => {
  const content = replyContent[reviewId]
  if (!content) { ElMessage.warning('请输入回复内容'); return }
  replyingId.value = reviewId
  try {
    await addReviewReply({ reviewId, content })
    ElMessage.success('回复成功')
    loadData()
  } finally { replyingId.value = null }
}
</script>

<style scoped>
.tab-switch { display: flex; background: var(--color-bg-hover); border-radius: 980px; padding: 3px; margin-bottom: 20px; width: fit-content; }
.tab-btn { padding: 8px 24px; border: none; background: none; border-radius: 980px; font-size: 14px; font-weight: 500; color: var(--color-text-secondary); cursor: pointer; transition: all var(--transition-fast); }
.tab-btn.active { background: #fff; color: var(--color-primary); box-shadow: var(--shadow-sm); }
.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-card { padding: 20px; }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.review-user { display: flex; gap: 10px; align-items: center; }
.reviewer-name { font-weight: 600; font-size: 14px; display: block; }
.review-time { font-size: 12px; color: var(--color-text-tertiary); }
.review-task-info { display: flex; gap: 10px; margin-bottom: 12px; align-items: center; flex-wrap: wrap; }
.task-badge, .runner-badge { font-size: 13px; padding: 4px 10px; background: var(--color-bg-hover, #f5f5f7); border-radius: 6px; color: var(--color-text-secondary); cursor: pointer; transition: background 0.2s; }
.task-badge:hover { background: #e8e8eb; color: var(--color-primary); }
.review-content { font-size: 14px; line-height: 1.7; color: var(--color-text-secondary); margin-bottom: 10px; }
.review-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 10px; }
.review-reply { background: var(--color-bg-hover); border-radius: var(--radius-md); padding: 12px; display: flex; gap: 10px; margin-top: 8px; }
.reply-name { font-weight: 600; font-size: 13px; }
.review-reply p { font-size: 13px; color: var(--color-text-secondary); margin-top: 2px; }
.reply-action { display: flex; gap: 8px; margin-top: 10px; }
.fab { position: fixed; bottom: 32px; right: 32px; box-shadow: var(--shadow-lg); }
</style>
