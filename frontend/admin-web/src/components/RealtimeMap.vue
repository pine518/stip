<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, watch } from 'vue'
import L from 'leaflet'
import type { LatestLocation } from '@/api/stip'

const props = defineProps<{
  locations: LatestLocation[]
}>()

let map: L.Map | null = null
let markerLayer: L.LayerGroup | null = null

onMounted(async () => {
  await nextTick()
  map = L.map('realtime-map', {
    zoomControl: true,
  }).setView([31.230416, 121.473701], 13)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(map)

  markerLayer = L.layerGroup().addTo(map)
  renderMarkers()
})

onBeforeUnmount(() => {
  map?.remove()
  map = null
  markerLayer = null
})

watch(
  () => props.locations,
  () => renderMarkers(),
  { deep: true },
)

function renderMarkers() {
  if (!map || !markerLayer) {
    return
  }

  markerLayer.clearLayers()

  const layer = markerLayer
  const bounds: L.LatLngTuple[] = []
  props.locations.forEach((location) => {
    const latLng: L.LatLngTuple = [Number(location.latitude), Number(location.longitude)]
    bounds.push(latLng)
    L.circleMarker(latLng, {
      radius: 8,
      color: markerColor(location.entityType),
      fillColor: markerColor(location.entityType),
      fillOpacity: 0.86,
      weight: 2,
    })
      .bindPopup(
        `<strong>${location.entityName}</strong><br/>类型：${location.entityType}<br/>速度：${location.speed ?? '-'} km/h<br/>时间：${location.eventTime}`,
      )
      .addTo(layer)
  })

  if (bounds.length > 0) {
    map.fitBounds(bounds, { padding: [40, 40], maxZoom: 15 })
  }
}

function markerColor(entityType: string) {
  if (entityType === 'PERSON') return '#2563eb'
  if (entityType === 'DEVICE') return '#7c3aed'
  return '#0f766e'
}
</script>

<template>
  <div id="realtime-map" class="leaflet-map"></div>
</template>
