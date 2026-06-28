<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import L from 'leaflet'
import { createFence, deleteFence, getFences, setFenceEnabled, type CreateFencePayload, type Fence } from '@/api/stip'

const loading = ref(false)
const dialogVisible = ref(false)
const fences = ref<Fence[]>([])
const drawPoints = ref<L.LatLng[]>([])
let map: L.Map | null = null
let drawLayer: L.LayerGroup | null = null
let fenceLayer: L.LayerGroup | null = null
const form = reactive<CreateFencePayload>({
  fenceName: '临时演示围栏',
  fenceType: 'POLYGON',
  ruleType: 'ENTER',
  geometryWkt:
    'POLYGON((121.482000 31.232000,121.490000 31.232000,121.490000 31.240000,121.482000 31.240000,121.482000 31.232000))',
  enabled: true,
})

onMounted(async () => {
  await loadFences()
  await nextTick()
  initMap()
})

onBeforeUnmount(() => {
  map?.remove()
  map = null
})

watch(fences, renderFences, { deep: true })
watch(drawPoints, renderDrawing, { deep: true })

async function loadFences() {
  loading.value = true
  try {
    fences.value = await getFences()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载围栏失败')
  } finally {
    loading.value = false
  }
}

async function submitFence() {
  await createFence({ ...form })
  ElMessage.success('围栏已创建')
  dialogVisible.value = false
  await loadFences()
}

async function toggleFence(row: Fence) {
  await setFenceEnabled(row.id, !row.enabled)
  ElMessage.success(row.enabled ? '围栏已停用' : '围栏已启用')
  await loadFences()
}

async function removeFence(row: Fence) {
  await ElMessageBox.confirm(`确认删除围栏「${row.fenceName}」？`, '删除确认', { type: 'warning' })
  await deleteFence(row.id)
  ElMessage.success('围栏已删除')
  await loadFences()
}

function initMap() {
  if (map) {
    return
  }

  map = L.map('fence-map').setView([31.234, 121.486], 14)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(map)

  fenceLayer = L.layerGroup().addTo(map)
  drawLayer = L.layerGroup().addTo(map)
  map.on('click', (event: L.LeafletMouseEvent) => {
    drawPoints.value = [...drawPoints.value, event.latlng]
    syncWktFromDrawing()
  })
  renderFences()
}

function clearDrawing() {
  drawPoints.value = []
  form.geometryWkt =
    'POLYGON((121.482000 31.232000,121.490000 31.232000,121.490000 31.240000,121.482000 31.240000,121.482000 31.232000))'
}

function undoPoint() {
  drawPoints.value = drawPoints.value.slice(0, -1)
  syncWktFromDrawing()
}

function syncWktFromDrawing() {
  if (drawPoints.value.length < 3) {
    return
  }
  const points = [...drawPoints.value, drawPoints.value[0]]
  const wktPoints = points.map((point) => `${point.lng.toFixed(6)} ${point.lat.toFixed(6)}`).join(',')
  form.geometryWkt = `POLYGON((${wktPoints}))`
}

function renderDrawing() {
  if (!drawLayer) {
    return
  }
  drawLayer.clearLayers()
  drawPoints.value.forEach((point, index) => {
    L.circleMarker(point, {
      radius: 5,
      color: '#2563eb',
      fillColor: '#2563eb',
      fillOpacity: 0.9,
    })
      .bindTooltip(String(index + 1), { permanent: true, direction: 'top' })
      .addTo(drawLayer!)
  })
  if (drawPoints.value.length >= 2) {
    L.polyline(drawPoints.value, { color: '#2563eb', weight: 3 }).addTo(drawLayer)
  }
  if (drawPoints.value.length >= 3) {
    L.polygon(drawPoints.value, { color: '#2563eb', fillOpacity: 0.16 }).addTo(drawLayer)
  }
}

function renderFences() {
  if (!fenceLayer) {
    return
  }
  fenceLayer.clearLayers()
  const bounds = L.latLngBounds([])
  fences.value.forEach((fence) => {
    try {
      const geoJson = JSON.parse(fence.geometryGeoJson)
      const layer = L.geoJSON(geoJson, {
        style: {
          color: fence.enabled ? '#16a34a' : '#94a3b8',
          fillOpacity: fence.enabled ? 0.12 : 0.04,
          weight: 2,
        },
      }).bindPopup(`${fence.fenceName}<br/>${fence.ruleType}<br/>${fence.enabled ? '启用' : '停用'}`)
      layer.addTo(fenceLayer!)
      layer.eachLayer((item) => {
        const itemBounds = (item as L.Polygon).getBounds?.()
        if (itemBounds?.isValid()) {
          bounds.extend(itemBounds)
        }
      })
    } catch {
      // Ignore invalid legacy geometry display while keeping table rows visible.
    }
  })
  if (map && bounds.isValid()) {
    map.fitBounds(bounds, { padding: [32, 32], maxZoom: 15 })
  }
}
</script>

<template>
  <section class="page-card" v-loading="loading">
    <div class="panel-header">
      <div>
        <h2>电子围栏</h2>
        <p>管理 PostGIS 多边形围栏，第一版使用 WKT 表单创建。</p>
      </div>
      <el-button type="primary" @click="dialogVisible = true">新建围栏</el-button>
    </div>

    <el-table :data="fences" empty-text="暂无围栏">
      <el-table-column prop="fenceName" label="围栏名称" min-width="150" />
      <el-table-column prop="fenceType" label="类型" width="100" />
      <el-table-column prop="ruleType" label="规则" width="120" />
      <el-table-column prop="enabled" label="启用" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="toggleFence(row)">{{ row.enabled ? '停用' : '启用' }}</el-button>
          <el-button size="small" type="danger" @click="removeFence(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="fence-map-wrap">
      <div class="panel-header">
        <div>
          <h3>地图绘制</h3>
          <p>在地图上依次点击 3 个以上点位，可自动生成多边形 WKT。</p>
        </div>
        <div class="inline-actions">
          <el-button @click="undoPoint">撤销点</el-button>
          <el-button @click="clearDrawing">清空绘制</el-button>
        </div>
      </div>
      <div id="fence-map" class="leaflet-map"></div>
    </div>

    <el-dialog v-model="dialogVisible" title="新建电子围栏" width="720px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="围栏名称">
          <el-input v-model="form.fenceName" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="form.ruleType">
            <el-option label="进入告警" value="ENTER" />
            <el-option label="离开告警" value="LEAVE" />
            <el-option label="停留超时" value="STAY_TIMEOUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="WKT">
          <el-input v-model="form.geometryWkt" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFence">创建</el-button>
      </template>
    </el-dialog>
  </section>
</template>
