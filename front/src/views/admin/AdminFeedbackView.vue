<template>
  <div class="page-container">
    <h1 class="page-title">反馈管理</h1>
    <p class="page-subtitle">查看和处理用户反馈</p>

    <!-- 筛选 -->
    <div class="apple-card filter-bar">
      <el-select v-model="query.type" placeholder="反馈类型" clearable @change="loadData" style="width: 130px">
        <el-option label="建议" value="suggestion" />
        <el-option label="投诉" value="complaint" />
        <el-option label="Bug" value="bug" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable @change="loadData" style="width: 130px">
        <el-option label="待处理" value="pending" />
        <el-option label="处理中" value="processing" />
        <el-option label="已解决" value="resolved" />
        <el-option label="已拒绝" value="rejected" />
      </el-select>
      <el-button type="primary" round @click="loadData">查询</el-button>
    </div>

    <el-table :data="feedbacks" stripe style="width: 100%; border-radius: 12px;">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="typeText" label="类型" width="90">
        <template #default="{ row }"><el-tag round size="small" type="info">{{ row.typeText }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="statusText" label="状态" width="90">
        <template #default="{ row }"><el-tag :type="statusMap(row.status)" round size="small">{{ row.statusText }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openReply(row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
      @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />

    <!-- 回复弹窗 -->
    <el-dialog v-model="showReply" title="回复反馈" width="560px">
      <div v-if="currentFb" style="margin-bottom: 20px;">
        <div class="detail-row"><span>标题</span><span>{{ currentFb.title }}</span></div>
        <div class="detail-row"><span>内容</span><span>{{ currentFb.content }}</span></div>
        <div class="detail-row"><span>用户</span><span>{{ currentFb.userName }}</span></div>
      </div>
      <el-form :model="replyForm" label-position="top">
        <el-form-item label="回复内容">
          <el-input v-model="replyForm.adminReply" type="textarea" :rows="3" placeholder="输入回复内容" />
        </el-form-item>
        <el-form-item label="更新状态">
          <el-select v-model="replyForm.status" style="width: 100%">
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReply = false" round>取消</el-button>
        <el-button type="primary" round :loading="replying" @click="handleReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listAllFeedback, adminReplyFeedback } from '@/api/feedback'
import { ElMessage } from 'element-plus'

const query = ref({ current: 1, pageSize: 10, type: '', status: '' })
const feedbacks = ref([])
const total = ref(0)
const showReply = ref(false)
const currentFb = ref(null)
const replying = ref(false)
const replyForm = ref({ feedbackId: null, adminReply: '', status: 'resolved' })

const loadData = async () => {
  const res = await listAllFeedback(query.value)
  feedbacks.value = res.records || []
  total.value = res.total || 0
}
onMounted(loadData)

const statusMap = (s) => ({ pending: 'info', processing: 'warning', resolved: 'success', rejected: 'danger' }[s] || 'info')

const openReply = (row) => {
  currentFb.value = row
  replyForm.value = { feedbackId: row.id, adminReply: '', status: 'resolved' }
  showReply.value = true
}

const handleReply = async () => {
  if (!replyForm.value.adminReply) { ElMessage.warning('请输入回复内容'); return }
  replying.value = true
  try {
    await adminReplyFeedback(replyForm.value)
    ElMessage.success('回复成功')
    showReply.value = false
    loadData()
  } finally { replying.value = false }
}
</script>

<style scoped>
.filter-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; padding: 16px 20px; }
.detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--color-border-light); font-size: 14px; }
.detail-row span:first-child { color: var(--color-text-secondary); min-width: 60px; }
</style>
