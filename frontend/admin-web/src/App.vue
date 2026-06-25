<script setup lang="ts">
const metrics = [
  { label: '在线实体', value: '3', hint: '车辆 / 人员 / 设备' },
  { label: '今日轨迹点', value: '1', hint: '演示数据已初始化' },
  { label: '启用围栏', value: '1', hint: '上海演示围栏' },
  { label: '开放告警', value: '0', hint: '等待规则触发' },
]

const navItems = ['实时监控', '历史轨迹', '电子围栏', '告警中心', '统计分析']
</script>

<template>
  <main class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">S</span>
        <div>
          <strong>STIP</strong>
          <small>SpatioTemporal Insight</small>
        </div>
      </div>
      <nav>
        <button v-for="item in navItems" :key="item" class="nav-item" :class="{ active: item === '实时监控' }">
          {{ item }}
        </button>
      </nav>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <h1>多源时空轨迹智能分析平台</h1>
          <p>第一阶段工程骨架：位置接入、时空存储、地图监控和围栏告警。</p>
        </div>
        <el-button type="primary">接入位置事件</el-button>
      </header>

      <section class="metrics-grid">
        <article v-for="metric in metrics" :key="metric.label" class="metric-card">
          <span>{{ metric.label }}</span>
          <strong>{{ metric.value }}</strong>
          <small>{{ metric.hint }}</small>
        </article>
      </section>

      <section class="content-grid">
        <article class="map-panel">
          <div class="panel-header">
            <div>
              <h2>实时地图</h2>
              <p>Leaflet 地图组件将在后续接入真实轨迹图层。</p>
            </div>
            <el-tag type="success">MVP</el-tag>
          </div>
          <div class="map-placeholder">
            <span class="map-point point-a"></span>
            <span class="map-point point-b"></span>
            <span class="map-point point-c"></span>
            <div class="track-line"></div>
          </div>
        </article>

        <article class="task-panel">
          <h2>下一批后端任务</h2>
          <ol>
            <li>实现轨迹点落库和 latest_location 更新。</li>
            <li>接入 PostGIS 附近查询和围栏判断。</li>
            <li>补充 Testcontainers 集成测试。</li>
          </ol>
        </article>
      </section>
    </section>
  </main>
</template>

