<template>
  <div class="page-container">
    <h1 class="page-title">用户管理</h1>
    <p class="page-subtitle">管理平台所有用户，查看用户信息、修改角色或删除用户</p>

    <!-- 搜索筛选栏 -->
    <div class="apple-card filter-bar">
      <el-input v-model="query.userName" placeholder="搜索用户昵称" clearable style="width: 180px"
        @keyup.enter="loadData" />
      <el-input v-model="query.userAccount" placeholder="搜索账号" clearable style="width: 180px"
        @keyup.enter="loadData" />
      <el-select v-model="query.userRole" placeholder="角色筛选" clearable @change="loadData" style="width: 140px">
        <el-option label="普通用户" value="user" />
        <el-option label="跑腿人员" value="runner" />
        <el-option label="管理员" value="admin" />
      </el-select>
      <el-button type="primary" round @click="loadData">
        <el-icon><Search /></el-icon>查询
      </el-button>
      <el-button round @click="resetQuery">重置</el-button>
    </div>

    <!-- 用户列表 -->
    <el-table :data="users" stripe style="width: 100%; border-radius: 12px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80">
        <template #default="{ row }">
          <span style="font-family: monospace; font-size: 12px;">{{ String(row.id).slice(-6) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="用户信息" min-width="200">
        <template #default="{ row }">
          <div class="user-info-cell">
            <el-avatar :size="36" :src="row.userAvatar">
              <el-icon :size="18"><User /></el-icon>
            </el-avatar>
            <div class="user-info-text">
              <span class="user-info-name">{{ row.userName || '未设置昵称' }}</span>
              <span class="user-info-account">{{ row.userAccount }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userRole" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.userRole)" round size="small" effect="plain">
            {{ roleLabel(row.userRole) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="contactInfo" label="联系方式" width="140">
        <template #default="{ row }">
          <span>{{ row.contactInfo || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信誉分" width="90">
        <template #default="{ row }">
          <span :class="['credit-score', row.creditScore < 60 ? 'credit-low' : row.creditScore >= 90 ? 'credit-high' : '']">
            {{ row.creditScore ?? '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="orderCount" label="接单数" width="80">
        <template #default="{ row }">{{ row.orderCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openRoleDialog(row)">
            <el-icon><Edit /></el-icon>角色
          </el-button>
          <el-popconfirm title="确定删除该用户吗？此操作不可恢复！" confirm-button-text="确定" cancel-button-text="取消"
            @confirm="handleDelete(row)">
            <template #reference>
              <el-button size="small" text type="danger" :disabled="row.userRole === 'admin'">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination :current-page="query.current" :page-size="query.pageSize" :total="total"
        @current-change="(v) => { query.current = v; loadData() }"
        layout="total, prev, pager, next" />
    </div>

    <!-- 修改角色弹窗 -->
    <el-dialog v-model="showRoleDialog" title="修改用户角色" width="440px" :close-on-click-modal="false">
      <div v-if="currentUser" class="role-dialog-info">
        <div class="detail-row">
          <span>用户</span>
          <span>{{ currentUser.userName || currentUser.userAccount }}</span>
        </div>
        <div class="detail-row">
          <span>当前角色</span>
          <el-tag :type="roleTagType(currentUser.userRole)" round size="small">{{ roleLabel(currentUser.userRole) }}</el-tag>
        </div>
      </div>
      <el-form label-position="top" style="margin-top: 16px;">
        <el-form-item label="新角色">
          <el-select v-model="newRole" style="width: 100%">
            <el-option label="普通用户" value="user" />
            <el-option label="跑腿人员" value="runner" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRoleDialog = false" round>取消</el-button>
        <el-button type="primary" round :loading="updating" @click="handleUpdateRole">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminListUser, adminUpdateUserRole, adminDeleteUser } from '@/api/user'
import { ElMessage } from 'element-plus'

const query = ref({ current: 1, pageSize: 10, userName: '', userAccount: '', userRole: '' })
const users = ref([])
const total = ref(0)
const loading = ref(false)

const showRoleDialog = ref(false)
const currentUser = ref(null)
const newRole = ref('')
const updating = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await adminListUser(query.value)
    users.value = res.records || []
    total.value = res.total || 0
  } catch { /* ignore */ }
  finally { loading.value = false }
}

const resetQuery = () => {
  query.value = { current: 1, pageSize: 10, userName: '', userAccount: '', userRole: '' }
  loadData()
}

onMounted(loadData)

const roleLabel = (role) => {
  const map = { user: '普通用户', runner: '跑腿人员', admin: '管理员', publisher: '发布者' }
  return map[role] || role
}

const roleTagType = (role) => {
  const map = { user: 'info', runner: 'warning', admin: 'danger', publisher: '' }
  return map[role] || 'info'
}

const openRoleDialog = (row) => {
  currentUser.value = row
  newRole.value = row.userRole
  showRoleDialog.value = true
}

const handleUpdateRole = async () => {
  if (newRole.value === currentUser.value.userRole) {
    ElMessage.info('角色未发生变化')
    return
  }
  updating.value = true
  try {
    await adminUpdateUserRole({ id: currentUser.value.id, userRole: newRole.value })
    ElMessage.success('角色修改成功')
    showRoleDialog.value = false
    loadData()
  } catch { /* ignore */ }
  finally { updating.value = false }
}

const handleDelete = async (row) => {
  try {
    await adminDeleteUser({ id: row.id })
    ElMessage.success('删除成功')
    loadData()
  } catch { /* ignore */ }
}
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  flex-wrap: wrap;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-info-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.user-info-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.user-info-account {
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.credit-score {
  font-weight: 600;
}
.credit-high {
  color: #30d158;
}
.credit-low {
  color: #ff3b30;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .filter-bar .el-input, .filter-bar .el-select { width: 100% !important; }
  .pagination-wrap { justify-content: center; }
}

.role-dialog-info {
  background: var(--color-bg-secondary, #f7f7f7);
  border-radius: 10px;
  padding: 14px 18px;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border-light, #eee);
  font-size: 14px;
}
.detail-row:last-child {
  border-bottom: none;
}
.detail-row span:first-child {
  color: var(--color-text-secondary);
  min-width: 60px;
}
</style>
