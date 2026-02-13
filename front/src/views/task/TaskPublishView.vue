<template>
  <div class="page-container">
    <h1 class="page-title">发布任务</h1>
    <p class="page-subtitle">填写任务信息，发布你的跑腿需求</p>

    <div class="apple-card" style="max-width: 680px;">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="任务类型" prop="taskType">
          <el-radio-group v-model="form.taskType" size="large">
            <el-radio-button value="file_delivery">📄 文件传递</el-radio-button>
            <el-radio-button value="goods_purchase">🛒 物品采购</el-radio-button>
            <el-radio-button value="food_delivery">🍜 餐饮配送</el-radio-button>
            <el-radio-button value="other">📦 其他</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="简要描述你的任务" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="详细描述任务要求" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="起始地点" prop="startLocation">
              <el-input v-model="form.startLocation" placeholder="如：第一教学楼" :prefix-icon="Location" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目的地" prop="endLocation">
              <el-input v-model="form.endLocation" placeholder="如：学生宿舍3号楼" :prefix-icon="Location" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="期望完成时间" prop="expectedTime">
              <el-date-picker v-model="form.expectedTime" type="datetime" placeholder="选择时间"
                style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报酬金额（元）" prop="reward">
              <el-input-number v-model="form.reward" :min="0.01" :precision="2" :step="1"
                style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系方式" prop="contactInfo">
          <el-input v-model="form.contactInfo" placeholder="你的手机号或微信号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="补充说明（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" round :loading="loading" native-type="submit"
            style="width: 200px;">发布任务</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { addTask } from '@/api/task'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const formRef = ref()
const form = ref({
  taskType: 'food_delivery', title: '', description: '', startLocation: '', endLocation: '',
  expectedTime: '', reward: 5, contactInfo: '', remark: ''
})
const rules = {
  taskType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  startLocation: [{ required: true, message: '请输入起始地点', trigger: 'blur' }],
  endLocation: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  expectedTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  reward: [{ required: true, message: '请输入报酬', trigger: 'blur' }],
  contactInfo: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await addTask(form.value)
    ElMessage.success('任务发布成功')
    router.push('/task/list')
  } finally { loading.value = false }
}
</script>
