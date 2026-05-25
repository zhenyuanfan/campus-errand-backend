<template>
  <div class="page-container">
    <h1 class="page-title">意见反馈</h1>
    <p class="page-subtitle">提交你的建议和反馈</p>

    <!-- 接单员角色显示 Tab 切换 -->
    <el-tabs v-if="userStore.isRunner" v-model="activeTab" class="feedback-tabs">
      <el-tab-pane label="提交反馈" name="submit" />
      <el-tab-pane name="complaints">
        <template #label>
          <span>针对我的投诉</span>
          <el-badge v-if="complaintsTotal > 0" :value="complaintsTotal" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- ===== Tab 1: 提交反馈 + 我的反馈列表（所有角色通用） ===== -->
    <div v-show="activeTab === 'submit'">
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
          <!-- 投诉时选择关联任务（关联接单员） -->
          <el-form-item v-if="form.type === 'complaint'" label="关联任务（选择要投诉的订单）">
            <el-select
              v-model="form.taskId"
              placeholder="请选择关联的任务"
              style="width: 100%"
              clearable
              filterable
              :loading="loadingTasks"
              @change="onTaskChange"
            >
              <el-option
                v-for="task in myTasks"
                :key="task.id"
                :label="task.title"
                :value="task.id"
              >
                <span>{{ task.title }}</span>
                <span style="float: right; color: var(--color-text-tertiary); font-size: 12px;">{{ task.statusText }}</span>
              </el-option>
            </el-select>
            <div v-if="selectedRunner" style="font-size: 12px; color: var(--color-text-secondary); margin-top: 6px;">
              📌 投诉对象（接单员）：<strong>{{ selectedRunner }}</strong>
            </div>
            <div v-if="!loadingTasks && !myTasks.length" style="font-size: 12px; color: var(--color-text-tertiary); margin-top: 4px;">
              暂无有接单员的任务
            </div>
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
          <!-- 接单员申诉内容（如果有） -->
          <div v-if="fb.runnerAppeal" class="fb-appeal">
            <strong>📋 接单员申诉：</strong>{{ fb.runnerAppeal }}
            <span class="fb-appeal-time">{{ fb.appealTime }}</span>
          </div>
          <div v-if="fb.adminReply" class="fb-reply">
            <strong>管理员回复：</strong>{{ fb.adminReply }}
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无反馈记录" />

      <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
        @current-change="(v) => { query.current = v; loadData() }" layout="prev, pager, next" />
    </div>

    <!-- ===== Tab 2: 针对我的投诉（仅接单员可见） ===== -->
    <div v-if="userStore.isRunner" v-show="activeTab === 'complaints'">
      <!-- 筛选 -->
      <div class="apple-card filter-bar">
        <el-select v-model="complaintsQuery.status" placeholder="处理状态" clearable @change="loadComplaints" style="width: 140px">
          <el-option label="待处理" value="pending" />
          <el-option label="处理中" value="processing" />
          <el-option label="已解决" value="resolved" />
          <el-option label="已驳回" value="rejected" />
        </el-select>
        <el-button type="primary" round @click="loadComplaints">刷新</el-button>
      </div>

      <div v-if="complaints.length" class="feedback-list">
        <div v-for="fb in complaints" :key="fb.id" class="apple-card fb-card complaint-card">
          <div class="fb-header">
            <el-tag :type="fbStatusType(fb.status)" round size="small">{{ fb.statusText }}</el-tag>
            <el-tag round size="small" type="danger">投诉</el-tag>
            <span class="fb-time">{{ fb.createTime }}</span>
          </div>
          <h4>{{ fb.title }}</h4>
          <p>{{ fb.content }}</p>

          <!-- 关联任务信息 -->
          <div v-if="fb.taskTitle" class="fb-task-info">
            <el-icon><Tickets /></el-icon>
            <span>关联任务：{{ fb.taskTitle }}</span>
          </div>

          <!-- 投诉者信息 -->
          <div class="fb-user-info">
            <el-avatar :size="20" :src="fb.userAvatar" />
            <span>投诉人：{{ fb.userName }}</span>
          </div>

          <!-- 管理员回复（如果有） -->
          <div v-if="fb.adminReply" class="fb-reply">
            <strong>管理员回复：</strong>{{ fb.adminReply }}
          </div>

          <!-- 申诉内容（如果已申诉） -->
          <div v-if="fb.runnerAppeal" class="fb-appeal">
            <strong>✅ 我的申诉：</strong>{{ fb.runnerAppeal }}
            <span class="fb-appeal-time">{{ fb.appealTime }}</span>
          </div>

          <!-- 申诉按钮（未申诉时显示） -->
          <div v-if="!fb.runnerAppeal" class="fb-actions">
            <el-button type="warning" round size="small" @click="openAppealDialog(fb)">
              <el-icon><Edit /></el-icon> 提交申诉
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无针对你的投诉" />

      <el-pagination :current-page="complaintsQuery.current" :page-size="complaintsQuery.pageSize" :total="complaintsTotal"
        @current-change="(v) => { complaintsQuery.current = v; loadComplaints() }" layout="prev, pager, next" />
    </div>

    <!-- ===== 申诉弹窗 ===== -->
    <el-dialog v-model="showAppealDialog" title="提交申诉" width="520px">
      <div v-if="currentComplaint" style="margin-bottom: 20px;">
        <div class="detail-row"><span>投诉标题</span><span>{{ currentComplaint.title }}</span></div>
        <div class="detail-row"><span>投诉内容</span><span>{{ currentComplaint.content }}</span></div>
        <div class="detail-row"><span>投诉人</span><span>{{ currentComplaint.userName }}</span></div>
        <div v-if="currentComplaint.taskTitle" class="detail-row"><span>关联任务</span><span>{{ currentComplaint.taskTitle }}</span></div>
      </div>
      <el-form label-position="top">
        <el-form-item label="申诉内容">
          <el-input v-model="appealContent" type="textarea" :rows="4" placeholder="请详细说明情况，为自己辩护" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAppealDialog = false" round>取消</el-button>
        <el-button type="primary" round :loading="appealing" @click="handleAppeal">提交申诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { addFeedback, listMyFeedback, listRunnerFeedback, appealFeedback } from '@/api/feedback'
import { listMyPublishedOrders } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Tickets, Edit } from '@element-plus/icons-vue'

const userStore = useUserStore()

// ===== Tab 状态 =====
const activeTab = ref('submit')

// ===== 提交反馈 =====
const submitting = ref(false)
const form = ref({ type: 'suggestion', title: '', content: '', contactInfo: '', taskId: null })
const myTasks = ref([])
const loadingTasks = ref(false)
const selectedRunner = ref('')

// 当选择投诉类型时，自动加载用户的任务列表
watch(() => form.value.type, async (newType) => {
  if (newType === 'complaint' && myTasks.value.length === 0) {
    await fetchMyTasks()
  }
  if (newType !== 'complaint') {
    form.value.taskId = null
    selectedRunner.value = ''
  }
})

const fetchMyTasks = async () => {
  loadingTasks.value = true
  try {
    // 一次加载所有已发布的任务，前端过滤出有接单员的
    const res = await listMyPublishedOrders({ current: 1, pageSize: 100 })
    myTasks.value = (res.records || []).filter(t => t.runnerId)
  } catch { /* ignore */ }
  finally { loadingTasks.value = false }
}

const onTaskChange = (taskId) => {
  if (!taskId) {
    selectedRunner.value = ''
    return
  }
  const task = myTasks.value.find(t => t.id === taskId)
  selectedRunner.value = task?.runnerName || '未知'
}

// ===== 我的反馈 =====
const query = ref({ current: 1, pageSize: 10 })
const feedbacks = ref([])
const total = ref(0)

const loadData = async () => {
  const res = await listMyFeedback(query.value)
  feedbacks.value = res.records || []
  total.value = res.total || 0
}

const handleSubmit = async () => {
  if (!form.value.title || !form.value.content) { ElMessage.warning('请填写标题和内容'); return }
  if (form.value.type === 'complaint' && !form.value.taskId) { ElMessage.warning('投诉时请选择关联任务，方便接单员查看和申诉'); return }
  submitting.value = true
  try {
    await addFeedback(form.value)
    ElMessage.success('反馈提交成功')
    form.value = { type: 'suggestion', title: '', content: '', contactInfo: '', taskId: null }
    selectedRunner.value = ''
    loadData()
  } finally { submitting.value = false }
}

// ===== 接单员投诉列表 =====
const complaintsQuery = ref({ current: 1, pageSize: 10, status: '' })
const complaints = ref([])
const complaintsTotal = ref(0)

const loadComplaints = async () => {
  if (!userStore.isRunner) return
  try {
    const res = await listRunnerFeedback(complaintsQuery.value)
    complaints.value = res.records || []
    complaintsTotal.value = res.total || 0
  } catch { /* ignore */ }
}

// ===== 申诉功能 =====
const showAppealDialog = ref(false)
const currentComplaint = ref(null)
const appealContent = ref('')
const appealing = ref(false)

const openAppealDialog = (fb) => {
  currentComplaint.value = fb
  appealContent.value = ''
  showAppealDialog.value = true
}

const handleAppeal = async () => {
  if (!appealContent.value.trim()) { ElMessage.warning('请填写申诉内容'); return }
  appealing.value = true
  try {
    await appealFeedback({ feedbackId: currentComplaint.value.id, runnerAppeal: appealContent.value })
    ElMessage.success('申诉提交成功')
    showAppealDialog.value = false
    loadComplaints()
  } finally { appealing.value = false }
}

// ===== 公共 =====
const fbStatusType = (s) => ({ pending: 'info', processing: 'warning', resolved: 'success', rejected: 'danger' }[s] || 'info')

onMounted(() => {
  loadData()
  if (userStore.isRunner) {
    loadComplaints()
  }
})
</script>

<style scoped>
.feedback-tabs { margin-bottom: 24px; }
.tab-badge { margin-left: 6px; }

.card-title { font-size: 17px; font-weight: 600; margin-bottom: 20px; }
.feedback-list { display: flex; flex-direction: column; gap: 12px; }
.fb-card { padding: 20px; }
.fb-header { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.fb-time { font-size: 12px; color: var(--color-text-tertiary); margin-left: auto; }
.fb-card h4 { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.fb-card p { font-size: 14px; color: var(--color-text-secondary); line-height: 1.6; }
.fb-reply { margin-top: 12px; padding: 12px; background: var(--color-bg-hover); border-radius: var(--radius-md); font-size: 13px; color: var(--color-text-secondary); }

/* 申诉样式 */
.fb-appeal {
  margin-top: 12px;
  padding: 12px;
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.08), rgba(230, 162, 60, 0.04));
  border: 1px solid rgba(230, 162, 60, 0.2);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--color-text-secondary);
}
.fb-appeal-time {
  display: block;
  margin-top: 6px;
  font-size: 11px;
  color: var(--color-text-tertiary);
}

/* 投诉卡片样式 */
.complaint-card {
  border-left: 3px solid var(--color-danger, #f56c6c);
}
.fb-task-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--color-text-tertiary);
}
.fb-user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-tertiary);
}
.fb-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

/* 筛选栏 */
.filter-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; padding: 16px 20px; flex-wrap: wrap; }

/* 弹窗详情行 */
.detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--color-border-light); font-size: 14px; }
.detail-row span:first-child { color: var(--color-text-secondary); min-width: 70px; flex-shrink: 0; }
.detail-row span:last-child { text-align: right; }

@media (max-width: 768px) {
  .filter-bar { padding: 12px 14px; gap: 8px; }
  .filter-bar .el-select { width: 100% !important; }
}
</style>
