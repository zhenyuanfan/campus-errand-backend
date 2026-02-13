<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card apple-card">
        <div class="login-header">
          <div class="logo-icon">
            <el-icon :size="36" color="#0071e3"><Van /></el-icon>
          </div>
          <h1>校园跑腿</h1>
          <p>Campus Errand Service</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <button :class="['tab-btn', { active: loginMode === 'password' }]" @click="loginMode = 'password'">
            账号密码
          </button>
          <button :class="['tab-btn', { active: loginMode === 'sms' }]" @click="loginMode = 'sms'">
            短信验证码
          </button>
        </div>

        <!-- 账号密码登录 -->
        <el-form v-if="loginMode === 'password'" ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" @submit.prevent="handlePwdLogin">
          <el-form-item prop="userAccount">
            <el-input v-model="pwdForm.userAccount" placeholder="请输入账号" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="userPassword">
            <el-input v-model="pwdForm.userPassword" type="password" placeholder="请输入密码" size="large"
              :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" native-type="submit" class="login-btn">
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 短信验证码登录 -->
        <el-form v-else ref="smsFormRef" :model="smsForm" :rules="smsRules" @submit.prevent="handleSmsLogin">
          <el-form-item prop="phone">
            <el-input v-model="smsForm.phone" placeholder="请输入手机号" size="large" :prefix-icon="Iphone" />
          </el-form-item>
          <el-form-item prop="code">
            <div class="code-input-wrapper">
              <el-input v-model="smsForm.code" placeholder="验证码" size="large" :prefix-icon="Message" />
              <el-button :disabled="countdown > 0" @click="handleSendCode" size="large" class="code-btn">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" native-type="submit" class="login-btn">
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userLogin, sendSmsCode, smsLogin } from '@/api/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Message } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const loginMode = ref('password')

// 账号密码表单
const pwdFormRef = ref()
const pwdForm = ref({ userAccount: '', userPassword: '' })
const pwdRules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 短信表单
const smsFormRef = ref()
const smsForm = ref({ phone: '', code: '' })
const smsRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}
const countdown = ref(0)

const handlePwdLogin = async () => {
  await pwdFormRef.value.validate()
  loading.value = true
  try {
    const user = await userLogin(pwdForm.value)
    userStore.setLoginUser(user)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

const handleSmsLogin = async () => {
  await smsFormRef.value.validate()
  loading.value = true
  try {
    const user = await smsLogin(smsForm.value)
    userStore.setLoginUser(user)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

const handleSendCode = async () => {
  if (!smsForm.value.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  await sendSmsCode({ phone: smsForm.value.phone })
  ElMessage.success('验证码已发送')
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}
.login-page::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.login-card {
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.logo-icon {
  width: 64px;
  height: 64px;
  background: rgba(0, 113, 227, 0.08);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
.login-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.03em;
}
.login-header p {
  font-size: 13px;
  color: var(--color-text-tertiary);
  margin-top: 4px;
}

.login-tabs {
  display: flex;
  background: var(--color-bg-hover);
  border-radius: 980px;
  padding: 3px;
  margin-bottom: 28px;
}
.tab-btn {
  flex: 1;
  padding: 8px 0;
  border: none;
  background: none;
  border-radius: 980px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.tab-btn.active {
  background: #fff;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

.login-btn {
  width: 100%;
  border-radius: 980px !important;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}
.code-input-wrapper .el-input {
  flex: 1;
}
.code-btn {
  white-space: nowrap;
  border-radius: var(--radius-sm) !important;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
