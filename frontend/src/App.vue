<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">EP</div>
        <div>
          <h1>EdgePulse</h1>
          <p>Industrial IoT Console</p>
        </div>
      </div>
      <nav>
        <button class="nav-item active">总览</button>
        <button class="nav-item">设备</button>
        <button class="nav-item">告警</button>
        <button class="nav-item">规则</button>
      </nav>
    </aside>

    <main class="main-panel">
      <header class="topbar">
        <div>
          <h2>工业设备监控</h2>
          <span>MQTT 数据接入 · 实时状态 · 规则告警</span>
        </div>
        <el-tag :type="connected ? 'success' : 'warning'" effect="dark">
          {{ connected ? '实时连接' : '轮询中' }}
        </el-tag>
      </header>

      <section class="metric-grid">
        <div class="metric-card">
          <span>设备总数</span>
          <strong>{{ summary.totalDevices }}</strong>
        </div>
        <div class="metric-card">
          <span>在线设备</span>
          <strong>{{ summary.onlineDevices }}</strong>
        </div>
        <div class="metric-card danger">
          <span>活动告警</span>
          <strong>{{ summary.activeAlarms }}</strong>
        </div>
        <div class="metric-card">
          <span>今日告警</span>
          <strong>{{ summary.todayAlarms }}</strong>
        </div>
      </section>

      <section class="content-grid">
        <div class="panel device-panel">
          <div class="panel-header">
            <h3>设备列表</h3>
            <el-button size="small" @click="loadAll">刷新</el-button>
          </div>
          <el-table :data="devices" height="310" empty-text="等待设备上报">
            <el-table-column prop="id" label="设备编号" width="110" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="type" label="类型" width="110" />
            <el-table-column label="状态" width="95">
              <template #default="{ row }">
                <el-tag size="small" :type="row.status === 'ONLINE' ? 'success' : 'info'">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastSeenAt" label="最近上报" min-width="180" />
          </el-table>
        </div>

        <div class="panel chart-panel">
          <div class="panel-header">
            <h3>最新遥测曲线</h3>
            <span>{{ selectedDevice || '全部设备' }}</span>
          </div>
          <div ref="chartRef" class="chart"></div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <h3>告警中心</h3>
          <span>ACTIVE → ACKED → RESOLVED</span>
        </div>
        <el-table :data="alarms" height="280" empty-text="暂无告警">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="deviceId" label="设备" width="110" />
          <el-table-column prop="metricName" label="指标" width="110" />
          <el-table-column prop="metricValue" label="当前值" width="100" />
          <el-table-column prop="threshold" label="阈值" width="90" />
          <el-table-column label="等级" width="110">
            <template #default="{ row }">
              <el-tag :type="row.level === 'CRITICAL' ? 'danger' : 'warning'" size="small">{{ row.level }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="说明" min-width="260" />
          <el-table-column label="处理" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" :disabled="row.status !== 'ACTIVE'" @click="changeAlarm(row.id, 'ACKED')">确认</el-button>
              <el-button size="small" :disabled="row.status === 'RESOLVED'" @click="changeAlarm(row.id, 'RESOLVED')">解决</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </main>
  </div>
</template>

<script setup>
import * as echarts from 'echarts'
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { api } from './api'

const summary = ref({ totalDevices: 0, onlineDevices: 0, activeAlarms: 0, todayAlarms: 0 })
const devices = ref([])
const telemetry = ref([])
const alarms = ref([])
const connected = ref(false)
const selectedDevice = ref('')
const chartRef = ref(null)
let chart
let eventSource
let timer

async function loadAll() {
  const [summaryData, deviceData, telemetryData, alarmData] = await Promise.all([
    api.summary(),
    api.devices(),
    api.latestTelemetry(),
    api.alarms()
  ])
  summary.value = summaryData
  devices.value = deviceData
  telemetry.value = telemetryData.reverse()
  alarms.value = alarmData
  renderChart()
}

async function changeAlarm(id, status) {
  await api.updateAlarmStatus(id, status)
  await loadAll()
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const points = telemetry.value.slice(-30)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['temperature', 'voltage'] },
    grid: { left: 42, right: 18, top: 44, bottom: 38 },
    xAxis: {
      type: 'category',
      data: points.map((item) => new Date(item.reportedAt).toLocaleTimeString())
    },
    yAxis: { type: 'value' },
    series: [
      { name: 'temperature', type: 'line', smooth: true, data: points.map((item) => item.temperature) },
      { name: 'voltage', type: 'line', smooth: true, data: points.map((item) => item.voltage) }
    ]
  })
}

function connectEvents() {
  eventSource = new EventSource('/api/events/stream')
  eventSource.onopen = () => {
    connected.value = true
  }
  eventSource.onerror = () => {
    connected.value = false
  }
  for (const name of ['telemetry', 'alarm', 'device', 'alarm-status']) {
    eventSource.addEventListener(name, () => loadAll())
  }
}

onMounted(async () => {
  await nextTick()
  await loadAll()
  connectEvents()
  timer = window.setInterval(loadAll, 10000)
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  eventSource?.close()
  window.clearInterval(timer)
  chart?.dispose()
})
</script>
