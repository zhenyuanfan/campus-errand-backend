<template>
  <div class="page-container">
    <h1 class="page-title">评价中心</h1>
    <p class="page-subtitle">管理评价和查看评价统计</p>

    <div class="tab-switch">
      <button :class="['tab-btn', { active: tab === 'my' }]" @click="tab = 'my'; loadData()">我的评价</button>
      <button :class="['tab-btn', { active: tab === 'received' }]" @click="tab = 'received'; loadData()">收到的评价</button>
    </div>

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
        <el-form-item label="任务ID">
          <el-input-number v-model="addForm.taskId" :min="1" style="width: 100%" />
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

    <el-button type="primary" round class="fab" @click="showAdd = true">
      <el-icon><Edit /></el-icon>写评价
    </el-button>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listMyReviews, listReceivedReviews, addReview, addReviewReply } from '@/api/review'
import { ElMessage } from 'element-plus'

const tab = ref('my')
const query = ref({ current: 1, pageSize: 10 })
const reviews = ref([])
const total = ref(0)
const showAdd = ref(false)
const submitting = ref(false)
const replyContent = reactive({})
const replyingId = ref(null)
const addForm = ref({ taskId: null, score: 5, content: '', tags: '' })

const loadData = async () => {
  const fn = tab.value === 'my' ? listMyReviews : listReceivedReviews
  const res = await fn(query.value)
  reviews.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)

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
.review-content { font-size: 14px; line-height: 1.7; color: var(--color-text-secondary); margin-bottom: 10px; }
.review-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 10px; }
.review-reply { background: var(--color-bg-hover); border-radius: var(--radius-md); padding: 12px; display: flex; gap: 10px; margin-top: 8px; }
.reply-name { font-weight: 600; font-size: 13px; }
.review-reply p { font-size: 13px; color: var(--color-text-secondary); margin-top: 2px; }
.reply-action { display: flex; gap: 8px; margin-top: 10px; }
.fab { position: fixed; bottom: 32px; right: 32px; box-shadow: var(--shadow-lg); }
</style>
