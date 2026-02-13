<template>
  <div class="page-container">
    <h1 class="page-title">个人中心</h1>
    <p class="page-subtitle">管理你的个人信息</p>

    <div class="profile-grid">
      <!-- 头像卡片 -->
      <div class="apple-card avatar-card">
        <el-avatar :size="100" :src="userStore.loginUser?.userAvatar" />
        <h3>{{ userStore.loginUser?.userName || '未设置昵称' }}</h3>
        <el-tag round>{{ roleText }}</el-tag>
        <el-upload :show-file-list="false" :before-upload="handleAvatarUpload" accept="image/*">
          <el-button size="small" round>更换头像</el-button>
        </el-upload>
      </div>

      <!-- 基本信息卡片 -->
      <div class="apple-card info-card">
        <h3 class="card-title">基本信息</h3>
        <el-form :model="form" label-position="top">
          <el-form-item label="昵称">
            <el-input v-model="form.userName" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="form.userProfile" type="textarea" :rows="3" placeholder="介绍一下自己" />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="form.contactInfo" placeholder="手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" round :loading="saving" @click="handleSave">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 绑定手机 -->
      <div class="apple-card info-card">
        <h3 class="card-title">安全设置</h3>
        <el-form :model="phoneForm" label-position="top">
          <el-form-item label="绑定手机号">
            <el-input v-model="phoneForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="验证码">
            <div style="display:flex;gap:12px;width:100%">
              <el-input v-model="phoneForm.code" placeholder="验证码" />
              <el-button :disabled="countdown > 0" @click="handleSendCode">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" round :loading="binding" @click="handleBind">绑定手机</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 账户信息 -->
      <div class="apple-card info-card">
        <h3 class="card-title">账户信息</h3>
        <div class="info-row"><span>账号</span><span>{{ userStore.loginUser?.userAccount }}</span></div>
        <div class="info-row"><span>角色</span><span>{{ roleText }}</span></div>
        <div class="info-row"><span>信誉评分</span><span class="stat-number" style="font-size:20px">{{ userStore.loginUser?.creditScore || 100 }}</span></div>
        <div class="info-row"><span>接单数量</span><span>{{ userStore.loginUser?.orderCount || 0 }}</span></div>
        <div class="info-row"><span>注册时间</span><span>{{ userStore.loginUser?.createTime }}</span></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateMyUser, uploadAvatar, bindPhone, sendSmsCode } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const saving = ref(false)
const binding = ref(false)
const countdown = ref(0)

const form = ref({ userName: '', userProfile: '', contactInfo: '' })
const phoneForm = ref({ phone: '', code: '' })

const roleText = computed(() => {
  const map = { user: '普通用户', admin: '管理员', runner: '跑腿人员', publisher: '发布者' }
  return map[userStore.loginUser?.userRole] || '用户'
})

onMounted(() => {
  if (userStore.loginUser) {
    form.value.userName = userStore.loginUser.userName || ''
    form.value.userProfile = userStore.loginUser.userProfile || ''
    form.value.contactInfo = userStore.loginUser.contactInfo || ''
  }
})

const handleSave = async () => {
  saving.value = true
  try {
    await updateMyUser(form.value)
    ElMessage.success('保存成功')
    await userStore.fetchLoginUser()
  } finally { saving.value = false }
}

const handleAvatarUpload = async (file) => {
  try {
    await uploadAvatar(file)
    ElMessage.success('头像更新成功')
    await userStore.fetchLoginUser()
  } catch { /* handled */ }
  return false
}

const handleSendCode = async () => {
  if (!phoneForm.value.phone) { ElMessage.warning('请输入手机号'); return }
  await sendSmsCode({ phone: phoneForm.value.phone })
  ElMessage.success('验证码已发送')
  countdown.value = 60
  const t = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(t) }, 1000)
}

const handleBind = async () => {
  binding.value = true
  try {
    await bindPhone(phoneForm.value)
    ElMessage.success('绑定成功')
    await userStore.fetchLoginUser()
  } finally { binding.value = false }
}
</script>

<style scoped>
.profile-grid {
  display: grid; grid-template-columns: 280px 1fr; gap: 20px;
}
.avatar-card {
  text-align: center; display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 32px;
}
.avatar-card h3 { font-size: 18px; font-weight: 600; }
.card-title { font-size: 17px; font-weight: 600; margin-bottom: 20px; }
.info-row {
  display: flex; justify-content: space-between; padding: 12px 0;
  border-bottom: 1px solid var(--color-border-light); font-size: 14px;
}
.info-row span:first-child { color: var(--color-text-secondary); }
</style>
