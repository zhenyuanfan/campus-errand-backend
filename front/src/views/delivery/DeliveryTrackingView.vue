<template>
  <div class="page-container">
    <el-button text @click="$router.back()" style="margin-bottom: 16px;">← 返回</el-button>
    <h1 class="page-title">配送跟踪</h1>
    <p class="page-subtitle">任务 #{{ taskId }} 的配送轨迹</p>

    <div class="tracking-grid">
      <!-- 地图区域 -->
      <div class="apple-card map-card">
        <!-- 配送信息浮层（美团风格） -->
        <div class="delivery-info-panel" v-if="taskInfo">
          <div class="dip-status">
            <span class="dip-status-icon">{{ statusEmoji }}</span>
            <div>
              <div class="dip-status-text">{{ latestStatusText }}</div>
              <div class="dip-status-sub" v-if="latestAddress">📍 {{ latestAddress }}</div>
            </div>
          </div>
          <div class="dip-route">
            <div class="dip-route-item">
              <span class="dip-dot start"></span>
              <span>{{ taskInfo.startLocation }}</span>
            </div>
            <div class="dip-route-line"></div>
            <div class="dip-route-item">
              <span class="dip-dot end"></span>
              <span>{{ taskInfo.endLocation }}</span>
            </div>
          </div>
        </div>
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

// 美团风格的状态信息
const statusEmoji = computed(() => {
  if (taskInfo.value?.status === 'completed') return '✅'
  if (taskInfo.value?.status === 'confirmed') return '✅'
  if (taskInfo.value?.status === 'cancelled') return '❌'
  const latest = trackingList.value[0]
  const map = { picked_up: '📦', delivering: '🚴', arrived: '📍', completed: '✅', location_update: '🚴' }
  return map[latest?.status] || '⏳'
})
const latestStatusText = computed(() => {
  if (taskInfo.value?.status === 'completed') return '订单已完成'
  if (taskInfo.value?.status === 'confirmed') return '物品已送达，待确认'
  if (taskInfo.value?.status === 'cancelled') return '订单已取消'
  
  const latest = trackingList.value[0]
  if (!latest) {
    return taskInfo.value?.status === 'in_progress' ? '骑手正在前往取件' : '等待骑手接单'
  }
  return latest.statusText || '配送中'
})
const latestAddress = computed(() => {
  const latest = trackingList.value[0]
  return latest?.address || ''
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
  
  // 注入定位控件：如果没有轨迹点，默认定位到用户当前位置
  window.AMap.plugin('AMap.Geolocation', () => {
    const geolocation = new window.AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      buttonPosition: 'RB',
      buttonOffset: new window.AMap.Pixel(10, 20),
      zoomToAccuracy: true
    })
    map.addControl(geolocation)
    
    const points = trackingList.value.filter(item => item.longitude && item.latitude)
    if (points.length === 0) {
      geolocation.getCurrentPosition()
    }
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

  // 构建轨迹路径
  const path = []
  points.forEach((item, index) => {
    const pos = [item.longitude, item.latitude]
    path.push(pos)

    const isLatest = index === points.length - 1
    const isFirst = index === 0

    // 最新位置 — 骑手标记（蓝色脉冲圈）
    if (isLatest) {
      const marker = new window.AMap.Marker({
        position: pos,
        map: map,
        anchor: 'center',
        content: `<div class="rider-marker">
          <div class="rider-pulse"></div>
          <div class="rider-dot">🚴</div>
        </div>`,
        offset: new window.AMap.Pixel(-20, -20)
      })
      markers.push(marker)
    } else if (isFirst) {
      // 第一个点 — 起点标记
      const marker = new window.AMap.Marker({
        position: pos,
        map: map,
        anchor: 'center',
        content: `<div class="point-marker start-marker">
          <span>起</span>
        </div>`,
        offset: new window.AMap.Pixel(-14, -14)
      })
      markers.push(marker)
    } else {
      // 中间点 — 小圆点
      const marker = new window.AMap.Marker({
        position: pos,
        map: map,
        anchor: 'center',
        content: '<div class="track-dot"></div>',
        offset: new window.AMap.Pixel(-4, -4)
      })
      markers.push(marker)
    }
  })

  // 绘制轨迹线 — 渐变蓝色
  if (path.length >= 2) {
    polyline = new window.AMap.Polyline({
      path: path,
      strokeColor: '#0071e3',
      strokeWeight: 5,
      strokeOpacity: 0.7,
      lineJoin: 'round',
      lineCap: 'round',
      showDir: true,
      map: map
    })
  }

  // 自适应缩放
  map.setFitView(markers, false, [80, 80, 80, 80])
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

/**
 * 高德逆地理编码：经纬度 → 真实地址字符串
 * 返回如 "北京市朝阳区建国路xxx号" 的地址，失败时兜底返回 "配送中"
 */
const reverseGeocode = (lng, lat) => {
  return new Promise((resolve) => {
    if (!window.AMap) {
      resolve('配送中')
      return
    }
    window.AMap.plugin('AMap.Geocoder', () => {
      const geocoder = new window.AMap.Geocoder({ radius: 100, extensions: 'base' })
      geocoder.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.info === 'OK') {
          const addr = result.regeocode.formattedAddress
          resolve(addr || '配送中')
        } else {
          resolve('配送中')
        }
      })
    })
  })
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
        // 用高德逆地理编码把坐标转为真实地址名称
        const address = await reverseGeocode(longitude, latitude)
        await updateLocation({
          taskId: taskId,
          longitude: longitude,
          latitude: latitude,
          address: address
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
.map-card { padding: 0; overflow: hidden; position: relative; }
.map-container { width: 100%; height: 500px; background: #f5f5f7; display: flex; align-items: center; justify-content: center; }

/* 配送信息浮层 */
.delivery-info-panel {
  position: absolute; top: 12px; left: 12px; right: 12px;
  z-index: 10;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}
.dip-status {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 12px;
}
.dip-status-icon { font-size: 28px; }
.dip-status-text { font-size: 16px; font-weight: 700; color: var(--color-text-primary); }
.dip-status-sub { font-size: 12px; color: var(--color-text-tertiary); margin-top: 2px; }
.dip-route {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 12px;
  background: var(--color-bg-hover, #f5f5f7);
  border-radius: 10px;
  font-size: 13px; color: var(--color-text-secondary);
}
.dip-dot {
  width: 10px; height: 10px; border-radius: 50%;
  display: inline-block; flex-shrink: 0;
}
.dip-dot.start { background: #30d158; }
.dip-dot.end { background: #ff453a; }
.dip-route-item { display: flex; align-items: center; gap: 6px; }
.dip-route-line {
  flex: 1; height: 2px; min-width: 20px;
  background: linear-gradient(90deg, #30d158, #ff453a);
  border-radius: 1px;
}
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

@media (max-width: 768px) {
  .tracking-grid { grid-template-columns: 1fr; }
  .map-container { height: 280px; }
  .timeline-card { max-height: none; }
}
</style>

<style>
/* 骑手标记（蓝色脉冲圈 + emoji） */
.rider-marker {
  width: 40px; height: 40px;
  position: relative;
  display: flex; align-items: center; justify-content: center;
}
.rider-pulse {
  position: absolute; inset: 0;
  border-radius: 50%;
  background: rgba(0, 113, 227, 0.2);
  animation: rider-pulse-anim 2s ease-out infinite;
}
@keyframes rider-pulse-anim {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(2.2); opacity: 0; }
}
.rider-dot {
  width: 32px; height: 32px;
  background: #fff;
  border: 3px solid #0071e3;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0,113,227,0.3);
  z-index: 1;
}

/* 起点标记 */
.point-marker {
  width: 28px; height: 28px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}
.start-marker { background: #30d158; }

/* 轨迹中间小圆点 */
.track-dot {
  width: 8px; height: 8px;
  background: #0071e3;
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0,0,0,0.15);
}
</style>
