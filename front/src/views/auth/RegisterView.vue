<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card apple-card">
        <div class="login-header">
          <div class="logo-icon">
            <el-icon :size="36" color="#0071e3"><Van /></el-icon>
          </div>
          <h1>创建账号</h1>
          <p>加入校园跑腿平台</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister">
          <el-form-item prop="userAccount">
            <el-input v-model="form.userAccount" placeholder="请输入账号（至少4位）" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="userPassword">
            <el-input v-model="form.userPassword" type="password" placeholder="请输入密码（至少8位）" size="large"
              :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="checkPassword">
            <el-input v-model="form.checkPassword" type="password" placeholder="请确认密码" size="large"
              :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="userRole">
            <el-select v-model="form.userRole" placeholder="请选择角色" size="large" style="width: 100%">
              <el-option label="普通用户（发布任务）" value="user" />
              <el-option label="跑腿人员（接受任务）" value="runner" />
            </el-select>
          </el-form-item>
          <el-form-item prop="contactInfo">
            <el-input v-model="form.contactInfo" placeholder="联系方式（手机号）" size="large" :prefix-icon="Iphone" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" native-type="submit" class="login-btn">
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const formRef = ref()
const form = ref({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userRole: 'user',
  contactInfo: ''
})
const rules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号至少4位', trigger: 'blur' }],
  userPassword: [{ required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' }],
  checkPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }],
  userRole: [{ required: true, message: '请选择角色', trigger: 'change' }],
  contactInfo: [{ required: true, message: '请输入联系方式', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const handleRegister = async () => {
  await formRef.value.validate()
  if (form.value.userPassword !== form.value.checkPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await userRegister(form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
}
.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.logo-icon {
  width: 64px; height: 64px;
  background: rgba(0, 113, 227, 0.08);
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
}
.login-header h1 { font-size: 24px; font-weight: 700; letter-spacing: -0.03em; }
.login-header p { font-size: 13px; color: var(--color-text-tertiary); margin-top: 4px; }
.login-btn {
  width: 100%; border-radius: 980px !important; height: 44px; font-size: 15px; font-weight: 600;
}
.login-footer { text-align: center; margin-top: 20px; font-size: 14px; color: var(--color-text-secondary); }
</style>
