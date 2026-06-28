<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getEntities, getTrajectory, type EntitySummary, type TrajectoryPoint } from '@/api/stip'

const entities = ref<EntitySummary[]>([])
const selectedEntityId = ref<number>()
const points = ref<TrajectoryPoint[]>([])
const loading = ref(false)

const today = new Date()
const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
const startTime = ref(yesterday.toISOString())
const endTime = ref(today.toISOString())

onMounted(async () => {
  entities.value = await getEntities()
  selectedEntityId.value = entities.value[0]?.id
})

async function queryTrajectory() {
  if (!selectedEntityId.value) {
    ElMessage.warning('请先选择实体')
    return
  }
  loading.value = true
  try {
    points.value = await getTrajectory(selectedEntityId.value, startTime.value, endTime.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '查询历史轨迹失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="page-card" v-loading="loading">
    <div class="panel-header">
      <div>
        <h2>历史轨迹</h2>
        <p>选择实体和时间范围，查询 PostgreSQL 中的轨迹点。</p>
      </div>
      <el-button type="primary" @click="queryTrajectory">查询轨迹</el-button>
    </div>
    <div class="toolbar-grid">
      <el-select v-model="selectedEntityId" placeholder="选择实体">
        <el-option v-for="entity in entities" :key="entity.id" :label="entity.entityName" :value="entity.id" />
      </el-select>
      <el-input v-model="startTime" />
      <el-input v-model="endTime" />
    </div>
    <el-table :data="points" empty-text="暂无轨迹点">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="longitude" label="经度" />
      <el-table-column prop="latitude" label="纬度" />
      <el-table-column prop="speed" label="速度" />
      <el-table-column prop="eventTime" label="事件时间" min-width="180" />
    </el-table>
  </section>
</template>

