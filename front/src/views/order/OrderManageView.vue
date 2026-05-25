<template>
  <div class="page-container">
    <h1 class="page-title">我的订单</h1>
    <p class="page-subtitle">查看历史订单记录、订单详情、评价信息</p>



    <!-- 筛选 -->
    <div class="apple-card filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索任务标题"
        clearable
        :prefix-icon="Search"
        @keyup.enter="loadData"
        @clear="loadData"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable @change="loadData">
        <el-option label="待接单" value="pending" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="待确认" value="confirmed" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" round @click="loadData">
        <el-icon><Search /></el-icon>搜索
      </el-button>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- 订单卡片列表 -->
    <div v-else-if="orders.length" class="order-list">
      <div
        v-for="order in orders"
        :key="order.id"
        :class="['apple-card', 'order-card', { 'order-card--active': order.status === 'in_progress', 'order-card--confirmed': order.status === 'confirmed' }]"
        @click="viewDetail(order.id)"
      >
        <!-- 卡片顶部：状态 + 报酬 -->
        <div class="order-card-top">
          <div class="order-tags">
            <el-tag :type="statusType(order.status)" round size="small">{{ order.statusText }}</el-tag>
            <el-tag type="info" round size="small">{{ order.taskTypeText }}</el-tag>
          </div>
          <span class="order-reward">¥{{ order.reward }}</span>
        </div>

        <!-- 标题 -->
        <h3 class="order-title">{{ order.title }}</h3>

        <!-- 路线 -->
        <div class="order-route">
          <el-icon><Location /></el-icon>
          <span>{{ order.startLocation }}</span>
          <span class="route-arrow">→</span>
          <span>{{ order.endLocation }}</span>
        </div>

        <!-- 底部：时间 + 操作 -->
        <div class="order-card-bottom" @click.stop>
          <span class="order-time">🕐 {{ order.createTime }}</span>
          <div class="order-actions">
            <el-button size="small" round @click.stop="viewDetail(order.id)">详情</el-button>
            <!-- 进行中或已完成才显示配送跟踪 -->
            <el-button
              v-if="order.status === 'in_progress' || order.status === 'completed'"
              size="small" type="primary" round
              @click.stop="$router.push(`/delivery/${order.id}`)"
            >
              配送跟踪
            </el-button>
            <!-- 待接单且是发布者才能取消 -->
            <el-button
              v-if="tab === 'published' && order.status === 'pending'"
              size="small" type="danger" round
              @click.stop="handleCancel(order)"
            >
              取消
            </el-button>
            <!-- 待确认且是发布者显示确认收货按钮 -->
            <el-button
              v-if="tab === 'published' && order.status === 'confirmed'"
              size="small" type="success" round
              @click.stop="handleConfirm(order)"
            >
              ✅ 确认收货
            </el-button>
            <!-- 已完成且是发布者可以写评价 -->
            <el-button
              v-if="tab === 'published' && order.status === 'completed'"
              size="small" type="success" round
              @click.stop="goReview(order)"
            >
              写评价
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else
      :description="tab === 'published' ? '暂无发布的任务' : '暂无接单记录'"
      :image-size="120"
    />

    <!-- 分页 -->
    <el-pagination
      v-if="total > query.pageSize"
      :current-page="query.current"
      :page-size="query.pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="(v) => { query.current = v; loadData() }"
    />

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDetail" title="订单详情" :width="dialogWidth" destroy-on-close>
      <div v-if="detail" class="detail-body">
        <div class="detail-row"><span>标题</span><span>{{ detail.title }}</span></div>
        <div class="detail-row"><span>类型</span><span>{{ detail.taskTypeText }}</span></div>
        <div class="detail-row">
          <span>状态</span>
          <el-tag :type="statusType(detail.status)" round size="small">{{ detail.statusText }}</el-tag>
        </div>
        <div class="detail-row">
          <span>报酬</span>
          <span class="detail-reward">¥{{ detail.reward }}</span>
        </div>
        <div class="detail-row"><span>起点</span><span>{{ detail.startLocation }}</span></div>
        <div class="detail-row"><span>终点</span><span>{{ detail.endLocation }}</span></div>
        <div class="detail-row"><span>期望时间</span><span>{{ detail.expectedTime }}</span></div>
        <div class="detail-row"><span>联系方式</span><span>{{ detail.contactInfo }}</span></div>
        <div v-if="detail.remark" class="detail-row"><span>备注</span><span>{{ detail.remark }}</span></div>
        <div class="detail-row">
          <span>支付状态</span>
          <el-tag :type="paymentStatusType(detail.paymentStatus)" round size="small">{{ detail.paymentStatusText || '未知' }}</el-tag>
        </div>
        <div class="detail-row"><span>发布时间</span><span>{{ detail.createTime }}</span></div>

        <!-- 评价信息 -->
        <div v-if="detail.reviewContent" class="review-section">
          <h4>⭐ 评价信息</h4>
          <div class="detail-row">
            <span>评分</span>
            <el-rate :model-value="detail.reviewScore" disabled />
          </div>
          <div class="detail-row"><span>内容</span><span>{{ detail.reviewContent }}</span></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { listMyPublishedOrders, listMyRunnerOrders, getOrderDetail, confirmOrder } from '@/api/order'
import { cancelTask } from '@/api/task'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Location } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 发布员/普通用户只看"我发布的"，接单员只看"我接的"
const tab = ref(userStore.isRunner ? 'runner' : 'published')
const query = ref({ current: 1, pageSize: 10, keyword: '', status: '' })
const orders = ref([])
const total = ref(0)
const loading = ref(false)
const showDetail = ref(false)
const detail = ref(null)

// 响应式 Dialog 宽度
const dialogWidth = computed(() => window.innerWidth <= 768 ? '92%' : '600px')

const switchTab = (t) => {
  tab.value = t
  query.value.current = 1
  query.value.status = ''
  query.value.keyword = ''
  loadData()
}

// 状态优先级：待确认 > 进行中 > 待接单 > 已完成 > 已取消
const STATUS_PRIORITY = { confirmed: 0, in_progress: 1, pending: 2, completed: 3, cancelled: 4 }

const loadData = async () => {
  loading.value = true
  try {
    const fn = tab.value === 'published' ? listMyPublishedOrders : listMyRunnerOrders
    const res = await fn(query.value)
    const list = res.records || []
    // 按状态优先级排序，进行中置顶
    list.sort((a, b) => {
      const pa = STATUS_PRIORITY[a.status] ?? 9
      const pb = STATUS_PRIORITY[b.status] ?? 9
      return pa - pb
    })
    orders.value = list
    total.value = res.total || 0
  } catch { /* ignore */ }
  finally { loading.value = false }
}

onMounted(loadData)

const viewDetail = async (taskId) => {
  detail.value = await getOrderDetail(taskId)
  showDetail.value = true
}

const handleCancel = async (order) => {
  await ElMessageBox.confirm(`确定取消任务「${order.title}」吗？`, '取消确认', { type: 'warning' })
  await cancelTask(order.id)
  ElMessage.success('任务已取消')
  loadData()
}

const goReview = (order) => {
  router.push({ path: '/reviews', query: { taskId: order.id, title: order.title } })
}

const handleConfirm = async (order) => {
  await ElMessageBox.confirm(
    `确认已收到任务「${order.title}」的服务？确认后报酬 ¥${order.reward} 将转给接单员。`,
    '确认收货',
    { type: 'warning', confirmButtonText: '确认收货', cancelButtonText: '再看看' }
  )
  await confirmOrder(order.id)
  ElMessage.success('确认收货成功，报酬已转给接单员')
  loadData()
}

const statusType = (s) => ({ pending: 'info', accepted: '', in_progress: 'warning', confirmed: '', completed: 'success', cancelled: 'danger' }[s] || 'info')

const paymentStatusType = (s) => ({ unpaid: 'info', paid: 'warning', released: 'success', refunded: 'danger' }[s] || 'info')
</script>

<style scoped>
/* ===== Tab ===== */
.tab-switch {
  display: flex;
  background: var(--color-bg-hover);
  border-radius: 980px;
  padding: 3px;
  margin-bottom: 20px;
  width: fit-content;
}
.tab-btn {
  padding: 8px 28px;
  border: none;
  background: none;
  border-radius: 980px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}
.tab-btn.active {
  background: #fff;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

/* ===== 筛选栏 ===== */
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  padding: 14px 20px;
  flex-wrap: wrap;
}
.filter-bar .el-input { flex: 1; min-width: 160px; }
.filter-bar .el-select { width: 140px; }

/* ===== 订单卡片 ===== */
.order-list { display: flex; flex-direction: column; gap: 14px; }

.order-card {
  padding: 18px 20px;
  cursor: pointer;
  transition: all var(--transition-normal);
}
/* 进行中订单：左侧高亮线 + 淡背景 */
.order-card--active {
  border-left: 4px solid var(--color-warning) !important;
  background: linear-gradient(135deg, rgba(255, 159, 10, 0.04) 0%, #fff 60%);
}
.order-card--active .order-title::before {
  content: '🔥 进行中  ';
  font-size: 12px;
  font-weight: 600;
  color: var(--color-warning);
}

/* 待确认订单：绿色高亮线 */
.order-card.order-card--confirmed {
  border-left: 4px solid var(--color-success, #67c23a) !important;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.04) 0%, #fff 60%);
}
.order-card.order-card--confirmed .order-title::before {
  content: '📦 待确认  ';
  font-size: 12px;
  font-weight: 600;
  color: var(--color-success, #67c23a);
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.order-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.order-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.order-reward {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  white-space: nowrap;
}

.order-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.order-route {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.route-arrow { color: var(--color-text-tertiary); }

.order-card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
  flex-wrap: wrap;
  gap: 8px;
}
.order-time {
  font-size: 12px;
  color: var(--color-text-tertiary);
}
.order-actions { display: flex; gap: 8px; flex-wrap: wrap; }

/* ===== 详情弹窗 ===== */
.detail-body { display: flex; flex-direction: column; }
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-light);
  font-size: 14px;
  gap: 12px;
}
.detail-row span:first-child {
  color: var(--color-text-secondary);
  white-space: nowrap;
  min-width: 64px;
}
.detail-row span:last-child {
  text-align: right;
  word-break: break-all;
}
.detail-reward { font-weight: 700; color: var(--color-primary); font-size: 16px; }

.review-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px solid var(--color-border-light);
}
.review-section h4 { font-size: 15px; font-weight: 600; margin-bottom: 8px; }

.loading-wrap { padding: 20px 0; }

/* ===== 移动端 ===== */
@media (max-width: 768px) {
  .tab-btn { padding: 7px 18px; font-size: 13px; }
  .filter-bar { padding: 12px 14px; gap: 8px; }
  .filter-bar .el-input,
  .filter-bar .el-select { width: 100% !important; flex: none; }
  .order-reward { font-size: 17px; }
  .order-title { font-size: 15px; }
  .order-card-bottom { flex-direction: column; align-items: flex-start; }
  .order-actions { width: 100%; justify-content: flex-end; }
}
</style>
