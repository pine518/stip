<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { closeAlert, getAlerts, handleAlert, type AlertEvent } from '@/api/stip'

const loading = ref(false)
const status = ref('')
const alerts = ref<AlertEvent[]>([])

onMounted(loadAlerts)

async function loadAlerts() {
  loading.value = true
  try {
    alerts.value = await getAlerts(status.value || undefined)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载告警失败')
  } finally {
    loading.value = false
  }
}

async function ack(id: number) {
  await handleAlert(id)
  ElMessage.success('告警已确认')
  await loadAlerts()
}

async function close(id: number) {
  await closeAlert(id)
  ElMessage.success('告警已关闭')
  await loadAlerts()
}
</script>

<template>
  <section class="page-card" v-loading="loading">
    <div class="panel-header">
      <div>
        <h2>告警中心</h2>
        <p>查看、确认和关闭围栏/速度等告警事件。</p>
      </div>
      <div class="inline-actions">
        <el-select v-model="status" clearable placeholder="全部状态" @change="loadAlerts">
          <el-option label="开放" value="OPEN" />
          <el-option label="已确认" value="ACKED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button @click="loadAlerts">刷新</el-button>
      </div>
    </div>

    <el-table :data="alerts" empty-text="暂无告警">
      <el-table-column prop="alertType" label="类型" width="130" />
      <el-table-column prop="alertLevel" label="级别" width="100" />
      <el-table-column prop="entityName" label="实体" width="120" />
      <el-table-column prop="message" label="内容" min-width="220" />
      <el-table-column prop="eventTime" label="时间" min-width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'OPEN' ? 'danger' : row.status === 'ACKED' ? 'warning' : 'info'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :disabled="row.status !== 'OPEN'" @click="ack(row.id)">确认</el-button>
          <el-button size="small" type="primary" :disabled="row.status === 'CLOSED'" @click="close(row.id)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>
