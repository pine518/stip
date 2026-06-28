<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { navItems } from './router'

const route = useRoute()
const activeTitle = computed(() => navItems.find((item) => item.name === route.name)?.label ?? '实时监控')
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
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          class="nav-item"
          :class="{ active: route.name === item.name }"
          :to="item.path"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <h1>多源时空轨迹智能分析平台</h1>
          <p>当前页面：{{ activeTitle }}。第一阶段已接入后端 API 和本地 PostgreSQL。</p>
        </div>
      </header>

      <RouterView />
    </section>
  </main>
</template>
