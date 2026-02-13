<template>
  <div class="page-container">
    <h1 class="page-title">数据分析</h1>
    <p class="page-subtitle">平台运营数据概览与分析报表</p>

    <!-- 概览卡片 -->
    <div class="stat-cards">
      <div class="apple-card stat-card">
        <span class="stat-label">总用户数</span>
        <span class="stat-number">{{ overview.totalUsers || 0 }}</span>
      </div>
      <div class="apple-card stat-card">
        <span class="stat-label">总任务数</span>
        <span class="stat-number">{{ overview.totalTasks || 0 }}</span>
      </div>
      <div class="apple-card stat-card">
        <span class="stat-label">总收入（元）</span>
        <span class="stat-number">{{ overview.totalReward || 0 }}</span>
      </div>
      <div class="apple-card stat-card">
        <span class="stat-label">今日新任务</span>
        <span class="stat-number">{{ overview.todayNewTasks || 0 }}</span>
      </div>
      <div class="apple-card stat-card">
        <span class="stat-label">今日新用户</span>
        <span class="stat-number">{{ overview.todayNewUsers || 0 }}</span>
      </div>
      <div class="apple-card stat-card">
        <span class="stat-label">完成率</span>
        <span class="stat-number">{{ overview.completionRate || 0 }}%</span>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-grid">
      <div class="apple-card chart-card">
        <h3>任务类型分布</h3>
        <div ref="taskTypePie" class="chart-box"></div>
      </div>
      <div class="apple-card chart-card">
        <h3>任务状态分布</h3>
        <div ref="taskStatusPie" class="chart-box"></div>
      </div>
      <div class="apple-card chart-card wide">
        <h3>任务完成率趋势（近7天）</h3>
        <div ref="completionLine" class="chart-box"></div>
      </div>
      <div class="apple-card chart-card">
        <h3>用户角色分布</h3>
        <div ref="userRolePie" class="chart-box"></div>
      </div>
      <div class="apple-card chart-card">
        <h3>跑腿人员 TOP10（按接单数）</h3>
        <div ref="runnerBar" class="chart-box"></div>
      </div>
      <div class="apple-card chart-card wide">
        <h3>每日新增用户（近7天）</h3>
        <div ref="newUserLine" class="chart-box"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverviewStats, getTaskAnalysis, getUserAnalysis, getRunnerAnalysis } from '@/api/analysis'

const overview = ref({})
const taskTypePie = ref(null)
const taskStatusPie = ref(null)
const completionLine = ref(null)
const userRolePie = ref(null)
const runnerBar = ref(null)
const newUserLine = ref(null)

const chartTheme = {
  color: ['#0071e3', '#6e5ce6', '#30d158', '#ff9f0a', '#ff453a', '#64d2ff', '#bf5af2'],
  backgroundColor: 'transparent'
}

const makePie = (el, data) => {
  const chart = echarts.init(el, chartTheme)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['40%', '70%'], data, label: { fontSize: 12 }, itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 } }]
  })
  return chart
}

const makeLine = (el, xData, yData, name) => {
  const chart = echarts.init(el, chartTheme)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: xData, axisLine: { lineStyle: { color: '#d2d2d7' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0' } } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    series: [{ name, type: 'line', data: yData, smooth: true, areaStyle: { opacity: 0.1 }, lineStyle: { width: 3 } }]
  })
  return chart
}

const makeBar = (el, xData, yData, name) => {
  const chart = echarts.init(el, chartTheme)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: xData, axisLabel: { rotate: 30, fontSize: 11 } },
    yAxis: { type: 'value' },
    grid: { left: 40, right: 20, top: 20, bottom: 60 },
    series: [{ name, type: 'bar', data: yData, barWidth: '50%', itemStyle: { borderRadius: [6, 6, 0, 0] } }]
  })
  return chart
}

onMounted(async () => {
  const [ov, taskData, userData, runnerData] = await Promise.all([
    getOverviewStats(), getTaskAnalysis(), getUserAnalysis(), getRunnerAnalysis()
  ])
  overview.value = ov || {}

  await nextTick()

  // 任务类型饼图
  if (taskData?.typeDistribution) {
    makePie(taskTypePie.value, taskData.typeDistribution.map(i => ({ name: i.name, value: i.value })))
  }
  // 任务状态饼图
  if (taskData?.statusDistribution) {
    makePie(taskStatusPie.value, taskData.statusDistribution.map(i => ({ name: i.name, value: i.value })))
  }
  // 完成率趋势
  if (taskData?.completionRateTrend) {
    makeLine(completionLine.value,
      taskData.completionRateTrend.map(i => i.date),
      taskData.completionRateTrend.map(i => i.value), '完成率')
  }
  // 用户角色分布
  if (userData?.roleDistribution) {
    makePie(userRolePie.value, userData.roleDistribution.map(i => ({ name: i.name, value: i.value })))
  }
  // 跑腿人员排行
  if (runnerData?.topByOrders) {
    makeBar(runnerBar.value,
      runnerData.topByOrders.map(i => i.name),
      runnerData.topByOrders.map(i => i.value), '接单数')
  }
  // 新增用户趋势
  if (userData?.dailyNewUsers) {
    makeLine(newUserLine.value,
      userData.dailyNewUsers.map(i => i.date),
      userData.dailyNewUsers.map(i => i.value), '新增用户')
  }
})
</script>

<style scoped>
.stat-cards {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; margin-bottom: 32px;
}
.stat-card { text-align: center; padding: 24px 16px; }
.stat-label { display: block; font-size: 13px; color: var(--color-text-secondary); margin-bottom: 8px; }
.chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.chart-card { padding: 24px; }
.chart-card h3 { font-size: 15px; font-weight: 600; margin-bottom: 16px; }
.chart-card.wide { grid-column: span 2; }
.chart-box { width: 100%; height: 300px; }
</style>
