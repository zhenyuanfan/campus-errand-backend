<template>
  <div class="page-container">
    <h1 class="page-title">意见反馈</h1>
    <p class="page-subtitle">提交你的建议和反馈</p>

    <!-- 提交反馈 -->
    <div class="apple-card" style="max-width: 680px; margin-bottom: 32px;">
      <h3 class="card-title">提交反馈</h3>
      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="反馈类型">
          <el-radio-group v-model="form.type">
            <el-radio-button value="suggestion">💡 建议</el-radio-button>
            <el-radio-button value="complaint">📢 投诉</el-radio-button>
            <el-radio-button value="bug">🐛 Bug</el-radio-button>
            <el-radio-button value="other">📋 其他</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="简述问题或建议" />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="详细描述你的反馈" />
        </el-form-item>
        <el-form-item label="联系方式（选填）">
          <el-input v-model="form.contactInfo" placeholder="方便我们联系你" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round :loading="submitting" native-type="submit">提交反馈</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 我的反馈列表 -->
    <h2 style="font-size:20px; font-weight:600; margin-bottom:16px;">我的反馈</h2>
    <div v-if="feedbacks.length" class="feedback-list">
      <div v-for="fb in feedbacks" :key="fb.id" class="apple-card fb-card">
        <div class="fb-header">
          <el-tag :type="fbStatusType(fb.status)" round size="small">{{ fb.statusText }}</el-tag>
          <el-tag round size="small" type="info">{{ fb.typeText }}</el-tag>
          <span class="fb-time">{{ fb.createTime }}</span>
        </div>
        <h4>{{ fb.title }}</h4>
        <p>{{ fb.content }}</p>
        <div v-if="fb.adminReplyContent" class="fb-reply">
          <strong>管理员回复：</strong>{{ fb.adminReplyContent }}
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无反馈记录" />

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { addFeedback, listMyFeedback } from '@/api/feedback'
import { ElMessage } from 'element-plus'

const submitting = ref(false)
const form = ref({ type: 'suggestion', title: '', content: '', contactInfo: '' })
const query = ref({ current: 1, pageSize: 10 })
const feedbacks = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await listMyFeedback(query.value)
  feedbacks.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)

const handleSubmit = async () => {
  if (!form.value.title || !form.value.content) { ElMessage.warning('请填写标题和内容'); return }
  submitting.value = true
  try {
    await addFeedback(form.value)
    ElMessage.success('反馈提交成功')
    form.value = { type: 'suggestion', title: '', content: '', contactInfo: '' }
    loadData()
  } finally { submitting.value = false }
}

const fbStatusType = (s) => ({ pending: 'info', processing: 'warning', resolved: 'success', rejected: 'danger' }[s] || 'info')
</script>

<style scoped>
.card-title { font-size: 17px; font-weight: 600; margin-bottom: 20px; }
.feedback-list { display: flex; flex-direction: column; gap: 12px; }
.fb-card { padding: 20px; }
.fb-header { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.fb-time { font-size: 12px; color: var(--color-text-tertiary); margin-left: auto; }
.fb-card h4 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.fb-card p { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }
.fb-reply { margin-top: 12px; padding: 12px; background: var(--color-bg-hover); border-radius: var(--radius-md); font-size: 13px; color: var(--color-text-secondary); }
</style>
