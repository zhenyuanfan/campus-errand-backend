<template>
  <div class="page-container">
    <el-button text @click="$router.back()" style="margin-bottom: 16px;">← 返回</el-button>
    <h1 class="page-title">配送跟踪</h1>
    <p class="page-subtitle">任务 #{{ taskId }} 的配送轨迹</p>

    <div class="tracking-grid">
      <!-- 地图区域 -->
      <div class="apple-card map-card">
        <div id="amap-container" class="map-container"></div>
        <!-- 跑腿人员：实时位置上报开关 -->
        <div v-if="isTaskRunner && taskInfo?.status === 'in_progress'" class="location-bar">
          <div class="location-status">
            <span :class="['status-dot', { active: geoWatching }]"></span>
            <span>{{ geoWatching ? '正在上报位置...' : '位置上报已关闭' }}</span>
          </div>
          <el-button :type="geoWatching ? 'danger' : 'success'" round size="small" @click="toggleGeoWatch">
            {{ geoWatching ? '停止上报' : '开始上报位置' }}
          </el-button>
        </div>
        <!-- 发布者：自动刷新提示 -->
        <div v-if="!isTaskRunner && taskInfo?.status === 'in_progress'" class="location-bar">
          <div class="location-status">
            <span :class="['status-dot', { active: true }]"></span>
            <span>每 {{ POLL_INTERVAL / 1000 }} 秒自动刷新</span>
          </div>
          <el-button type="primary" round size="small" @click="refreshAll">手动刷新</el-button>
        </div>
      </div>

      <!-- 配送状态时间线 -->
      <div class="apple-card timeline-card">
        <h3 class="card-title">配送进度</h3>

        <!-- 跑腿人员操作 -->
        <div v-if="isTaskRunner && taskInfo?.status === 'in_progress'" class="runner-actions">
          <el-select v-model="newStatus" placeholder="更新状态" style="width: 100%; margin-bottom: 12px;">
            <el-option label="已取件" value="picked_up" />
            <el-option label="配送中" value="delivering" />
            <el-option label="已到达" value="arrived" />
            <el-option label="已完成" value="completed" />
          </el-select>
          <el-button type="primary" round size="small" @click="handleUpdateStatus" :loading="updating" style="width: 100%;">更新状态</el-button>
        </div>

        <el-timeline v-if="trackingList.length">
          <el-timeline-item v-for="item in trackingList" :key="item.id"
            :timestamp="item.createTime" placement="top"
            :type="item.status === 'arrived' || item.status === 'completed' ? 'success' : 'primary'">
            <div class="timeline-content">
              <span class="timeline-status">{{ item.statusText }}</span>
              <span v-if="item.address" class="timeline-location">📍 {{ item.address }}</span>
              <span v-if="item.description" class="timeline-desc">{{ item.description }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无配送记录" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDeliveryTrackingList, updateDeliveryStatus, updateLocation } from '@/api/delivery'
import { getTaskVO } from '@/api/task'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const taskId = route.params.taskId
const trackingList = ref([])
const taskInfo = ref(null)
const newStatus = ref('')
const updating = ref(false)
const geoWatching = ref(false)

let map = null
let markers = []
let runnerMarker = null   // 跑腿人员实时位置标记
let polyline = null       // 轨迹连线
let geoWatchId = null     // 浏览器定位监听 ID
let pollTimer = null       // 自动刷新定时器

const POLL_INTERVAL = 10000        // 发布者端轮询间隔（10秒）
const GEO_UPLOAD_INTERVAL = 15000  // 跑腿人员位置上报间隔（15秒）
let lastUploadTime = 0

// 判断当前登录用户是否是该任务的接单者
const isTaskRunner = computed(() => {
  return taskInfo.value && userStore.loginUser && String(taskInfo.value.runnerId) === String(userStore.loginUser.id)
})

onMounted(async () => {
  await loadTaskInfo()
  await loadTracking()
  initMap()
  // 如果任务进行中，启动自动刷新（对所有角色）
  if (taskInfo.value?.status === 'in_progress') {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
  stopGeoWatch()
  if (map) map.destroy()
})

const loadTaskInfo = async () => {
  try {
    taskInfo.value = await getTaskVO(taskId)
  } catch { /* ignore */ }
}

const loadTracking = async () => {
  try {
    trackingList.value = await getDeliveryTrackingList(taskId)
  } catch { /* ignore */ }
}

// ==================== 地图相关 ====================

const initMap = () => {
  if (!window.AMap) return
  map = new window.AMap.Map('amap-container', {
    zoom: 15,
    center: [116.397428, 39.90923],
    mapStyle: 'amap://styles/whitesmoke'
  })
  updateMapMarkers()
}

const updateMapMarkers = () => {
  if (!map) return

  // 清除旧标记和线
  markers.forEach(m => m.setMap(null))
  markers = []
  if (polyline) { polyline.setMap(null); polyline = null }

  const points = trackingList.value.filter(item => item.longitude && item.latitude)
  if (points.length === 0) return

  // 添加轨迹点标记
  const path = []
  points.forEach((item, index) => {
    const pos = [item.longitude, item.latitude]
    path.push(pos)

    const isLatest = index === points.length - 1
    const marker = new window.AMap.Marker({
      position: pos,
      map: map,
      label: {
        content: `<div class="map-label ${isLatest ? 'latest' : ''}">${item.statusText || '位置更新'}</div>`,
        direction: 'top',
        offset: [0, -4]
      },
      icon: isLatest ? new window.AMap.Icon({
        size: [24, 24],
        image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
        imageSize: [24, 34]
      }) : undefined
    })
    markers.push(marker)
  })

  // 绘制轨迹线
  if (path.length >= 2) {
    polyline = new window.AMap.Polyline({
      path: path,
      strokeColor: '#409EFF',
      strokeWeight: 4,
      strokeOpacity: 0.8,
      lineJoin: 'round',
      map: map
    })
  }

  // 自适应缩放到所有点
  map.setFitView(markers, false, [50, 50, 50, 50])
}

// ==================== 实时位置上报（跑腿人员端） ====================

const toggleGeoWatch = () => {
  if (geoWatching.value) {
    stopGeoWatch()
    ElMessage.info('已停止位置上报')
  } else {
    startGeoWatch()
  }
}

const startGeoWatch = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持定位功能')
    return
  }

  geoWatching.value = true
  ElMessage.success('已开始上报位置，请保持页面打开')

  geoWatchId = navigator.geolocation.watchPosition(
    async (position) => {
      const { longitude, latitude } = position.coords
      const now = Date.now()
      // 节流：每隔一定时间才上报一次
      if (now - lastUploadTime < GEO_UPLOAD_INTERVAL) return
      lastUploadTime = now

      try {
        await updateLocation({
          taskId: taskId,
          longitude: longitude,
          latitude: latitude,
          address: `经度:${longitude.toFixed(5)} 纬度:${latitude.toFixed(5)}`
        })
        // 上报后立即刷新轨迹
        await loadTracking()
        updateMapMarkers()
      } catch {
        /* 上报失败忽略，下次重试 */
      }
    },
    (error) => {
      console.error('定位失败:', error)
      if (error.code === 1) {
        ElMessage.error('请允许浏览器访问您的位置')
        stopGeoWatch()
      } else {
        ElMessage.warning('定位失败，将继续重试')
      }
    },
    {
      enableHighAccuracy: true,
      maximumAge: 10000,
      timeout: 15000
    }
  )
}

const stopGeoWatch = () => {
  if (geoWatchId !== null) {
    navigator.geolocation.clearWatch(geoWatchId)
    geoWatchId = null
  }
  geoWatching.value = false
}

// ==================== 自动轮询刷新（发布者端 + 通用） ====================

const startPolling = () => {
  if (pollTimer) return
  pollTimer = setInterval(async () => {
    await loadTracking()
    updateMapMarkers()
  }, POLL_INTERVAL)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const refreshAll = async () => {
  await loadTracking()
  updateMapMarkers()
  ElMessage.success('已刷新')
}

// ==================== 更新配送状态 ====================

const handleUpdateStatus = async () => {
  if (!newStatus.value) { ElMessage.warning('请选择状态'); return }
  updating.value = true
  try {
    await updateDeliveryStatus({ taskId: taskId, status: newStatus.value })
    ElMessage.success('状态更新成功')
    newStatus.value = ''
    await loadTracking()
    updateMapMarkers()
    // 如果完成了，停止所有实时功能
    if (newStatus.value === 'completed') {
      stopGeoWatch()
      stopPolling()
      await loadTaskInfo()
    }
  } finally { updating.value = false }
}
</script>

<style scoped>
.tracking-grid { display: grid; grid-template-columns: 1fr 360px; gap: 20px; }
.map-card { padding: 0; overflow: hidden; }
.map-container { width: 100%; height: 500px; background: #f5f5f7; display: flex; align-items: center; justify-content: center; }
.location-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 16px; background: var(--color-bg-hover, #f9f9f9);
  border-top: 1px solid var(--color-border-light, #eee);
}
.location-status { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--color-text-secondary); }
.status-dot {
  width: 8px; height: 8px; border-radius: 50%; background: #ccc;
  display: inline-block;
}
.status-dot.active {
  background: #67c23a;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.3); }
}
.timeline-card { max-height: 600px; overflow-y: auto; }
.card-title { font-size: 16px; font-weight: 600; margin-bottom: 16px; }
.runner-actions { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid var(--color-border-light); }
.timeline-content { display: flex; flex-direction: column; gap: 2px; }
.timeline-status { font-weight: 600; font-size: 14px; }
.timeline-location { font-size: 12px; color: var(--color-text-tertiary); }
.timeline-desc { font-size: 12px; color: var(--color-text-secondary); margin-top: 2px; }
</style>

<style>
/* 地图标签样式（非 scoped，因为由 AMap 动态创建） */
.map-label { font-size: 11px; padding: 2px 6px; background: rgba(255,255,255,0.9); border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.15); white-space: nowrap; }
.map-label.latest { background: #409EFF; color: #fff; font-weight: 600; }
</style>
