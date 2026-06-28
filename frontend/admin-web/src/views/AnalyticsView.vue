<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import MetricCard from '@/components/MetricCard.vue'
import { getOverview, type Overview } from '@/api/stip'

const overview = ref<Overview>({
  onlineEntities: 0,
  todayTrajectoryPoints: 0,
  enabledFences: 0,
  openAlerts: 0,
})

onMounted(async () => {
  try {
    overview.value = await getOverview()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载统计数据失败')
  }
})
</script>

<template>
  <section class="page-card">
    <h2>统计分析</h2>
    <p>当前展示实时统计概览，后续扩展趋势图、告警分布和停留点 Top N。</p>
    <section class="metrics-grid compact">
      <MetricCard label="在线实体" :value="overview.onlineEntities" hint="ACTIVE 实体数量" />
      <MetricCard label="今日轨迹点" :value="overview.todayTrajectoryPoints" hint="今日 trajectory_point" />
      <MetricCard label="启用围栏" :value="overview.enabledFences" hint="enabled=true" />
      <MetricCard label="开放告警" :value="overview.openAlerts" hint="status=OPEN" />
    </section>
  </section>
</template>

