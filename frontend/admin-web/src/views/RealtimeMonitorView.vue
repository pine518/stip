<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MetricCard from '@/components/MetricCard.vue'
import LocationEventDialog from '@/components/LocationEventDialog.vue'
import RealtimeMap from '@/components/RealtimeMap.vue'
import { getLatestLocations, getOverview, type LatestLocation, type Overview } from '@/api/stip'

const loading = ref(false)
const dialogVisible = ref(false)
const overview = ref<Overview>({
  onlineEntities: 0,
  todayTrajectoryPoints: 0,
  enabledFences: 0,
  openAlerts: 0,
})
const locations = ref<LatestLocation[]>([])

const metrics = computed(() => [
  { label: '在线实体', value: overview.value.onlineEntities, hint: '车辆 / 人员 / 设备' },
  { label: '今日轨迹点', value: overview.value.todayTrajectoryPoints, hint: '来自 PostgreSQL 轨迹表' },
  { label: '启用围栏', value: overview.value.enabledFences, hint: 'PostGIS 空间范围' },
  { label: '开放告警', value: overview.value.openAlerts, hint: '等待规则触发' },
])

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [overviewData, latestLocations] = await Promise.all([getOverview(), getLatestLocations()])
    overview.value = overviewData
    locations.value = latestLocations
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载实时监控数据失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section v-loading="loading">
    <section class="view-actions">
      <el-button type="primary" @click="dialogVisible = true">接入位置事件</el-button>
      <el-button @click="loadData">刷新数据</el-button>
    </section>

    <section class="metrics-grid">
      <MetricCard v-for="metric in metrics" :key="metric.label" v-bind="metric" />
    </section>

    <section class="content-grid">
      <article class="map-panel">
        <div class="panel-header">
          <div>
            <h2>实时地图</h2>
            <p>展示最新位置接口返回的实体点位，点击点位可查看详情。</p>
          </div>
          <el-tag type="success">Leaflet</el-tag>
        </div>
        <RealtimeMap :locations="locations" />
      </article>

      <article class="task-panel">
        <h2>最新位置</h2>
        <el-table :data="locations" size="small" empty-text="暂无最新位置">
          <el-table-column prop="entityName" label="实体" min-width="110" />
          <el-table-column prop="entityType" label="类型" width="90" />
          <el-table-column prop="speed" label="速度" width="80" />
          <el-table-column prop="eventTime" label="时间" min-width="170" />
        </el-table>
      </article>
    </section>

    <LocationEventDialog v-model="dialogVisible" @submitted="loadData" />
  </section>
</template>
