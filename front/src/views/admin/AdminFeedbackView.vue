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
      <el-table-column label="接单员" width="100">
        <template #default="{ row }">{{ row.runnerName || '-' }}</template>
      </el-table-column>
      <el-table-column label="申诉" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.runnerAppeal" round size="small" type="warning">已申诉</el-tag>
          <span v-else style="color: var(--color-text-tertiary); font-size: 12px;">-</span>
        </template>
      </el-table-column>
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
        <div v-if="currentFb.runnerName" class="detail-row"><span>接单员</span><span>{{ currentFb.runnerName }}</span></div>
        <div v-if="currentFb.taskTitle" class="detail-row"><span>关联任务</span><span>{{ currentFb.taskTitle }}</span></div>

        <!-- 接单员申诉内容 -->
        <div v-if="currentFb.runnerAppeal" class="appeal-block">
          <div class="appeal-label">📋 接单员申诉</div>
          <div class="appeal-content">{{ currentFb.runnerAppeal }}</div>
          <div class="appeal-time">申诉时间：{{ currentFb.appealTime }}</div>
        </div>
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
.filter-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; padding: 16px 20px; flex-wrap: wrap; }
.detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--color-border-light); font-size: 14px; }
.detail-row span:first-child { color: var(--color-text-secondary); min-width: 60px; }

/* 申诉区块 */
.appeal-block {
  margin-top: 16px;
  padding: 14px;
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.08), rgba(230, 162, 60, 0.04));
  border: 1px solid rgba(230, 162, 60, 0.2);
  border-radius: 8px;
}
.appeal-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-warning, #e6a23c);
  margin-bottom: 8px;
}
.appeal-content {
  font-size: 14px;
  color: var(--color-text-primary);
  line-height: 1.6;
}
.appeal-time {
  margin-top: 8px;
  font-size: 11px;
  color: var(--color-text-tertiary);
}

@media (max-width: 768px) {
  .filter-bar { padding: 12px 14px; gap: 8px; }
  .filter-bar .el-select { width: 100% !important; }
}
</style>
