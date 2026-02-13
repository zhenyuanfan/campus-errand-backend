<template>
  <div class="page-container">
    <el-button text @click="$router.back()" style="margin-bottom: 16px;">← 返回</el-button>
    <h1 class="page-title">配送跟踪</h1>
    <p class="page-subtitle">任务 #{{ taskId }} 的配送轨迹</p>

    <div class="tracking-grid">
      <!-- 地图区域 -->
      <div class="apple-card map-card">
        <div id="amap-container" class="map-container"></div>
      </div>

      <!-- 配送状态时间线 -->
      <div class="apple-card timeline-card">
        <h3 class="card-title">配送进度</h3>

        <!-- 跑腿人员操作 -->
        <div v-if="userStore.isRunner" class="runner-actions">
          <el-select v-model="newStatus" placeholder="更新状态" style="width: 100%; margin-bottom: 12px;">
            <el-option label="已取件" value="picked_up" />
            <el-option label="配送中" value="delivering" />
            <el-option label="已送达" value="delivered" />
            <el-option label="已确认" value="confirmed" />
          </el-select>
          <el-button type="primary" round size="small" @click="handleUpdateStatus" :loading="updating" style="width: 100%;">更新状态</el-button>
        </div>

        <el-timeline v-if="trackingList.length">
          <el-timeline-item v-for="item in trackingList" :key="item.id"
            :timestamp="item.createTime" placement="top"
            :type="item.status === 'delivered' || item.status === 'confirmed' ? 'success' : 'primary'">
            <div class="timeline-content">
              <span class="timeline-status">{{ item.statusText }}</span>
              <span v-if="item.location" class="timeline-location">📍 {{ item.location }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无配送记录" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDeliveryTrackingList, updateDeliveryStatus } from '@/api/delivery'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const taskId = route.params.taskId
const trackingList = ref([])
const newStatus = ref('')
const updating = ref(false)
let map = null

onMounted(async () => {
  await loadTracking()
  initMap()
})

onUnmounted(() => {
  if (map) map.destroy()
})

const loadTracking = async () => {
  try {
    trackingList.value = await getDeliveryTrackingList(taskId)
  } catch { /* ignore */ }
}

const initMap = () => {
  // 高德地图初始化
  if (window.AMap) {
    map = new window.AMap.Map('amap-container', {
      zoom: 15,
      center: [116.397428, 39.90923],
      mapStyle: 'amap://styles/whitesmoke'
    })
    // 在地图上标记轨迹点
    trackingList.value.forEach(item => {
      if (item.longitude && item.latitude) {
        new window.AMap.Marker({
          position: [item.longitude, item.latitude],
          map: map,
          label: { content: item.statusText, direction: 'top' }
        })
      }
    })
  }
}

const handleUpdateStatus = async () => {
  if (!newStatus.value) { ElMessage.warning('请选择状态'); return }
  updating.value = true
  try {
    await updateDeliveryStatus({ taskId: Number(taskId), status: newStatus.value })
    ElMessage.success('状态更新成功')
    newStatus.value = ''
    await loadTracking()
  } finally { updating.value = false }
}
</script>

<style scoped>
.tracking-grid { display: grid; grid-template-columns: 1fr 360px; gap: 20px; }
.map-card { padding: 0; overflow: hidden; }
.map-container { width: 100%; height: 500px; background: #f5f5f7; display: flex; align-items: center; justify-content: center; }
.timeline-card { max-height: 600px; overflow-y: auto; }
.card-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.runner-actions { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid var(--color-border-light); }
.timeline-content { display: flex; flex-direction: column; gap: 2px; }
.timeline-status { font-weight: 600; font-size: 14px; }
.timeline-location { font-size: 12px; color: var(--color-text-tertiary); }
</style>
