import { createRouter, createWebHistory } from 'vue-router'
import RealtimeMonitorView from '@/views/RealtimeMonitorView.vue'
import TrajectoryView from '@/views/TrajectoryView.vue'
import FenceView from '@/views/FenceView.vue'
import AlertView from '@/views/AlertView.vue'
import AnalyticsView from '@/views/AnalyticsView.vue'

export const navItems = [
  { label: '实时监控', name: 'realtime', path: '/' },
  { label: '历史轨迹', name: 'trajectory', path: '/trajectory' },
  { label: '电子围栏', name: 'fence', path: '/fence' },
  { label: '告警中心', name: 'alert', path: '/alert' },
  { label: '统计分析', name: 'analytics', path: '/analytics' },
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'realtime', component: RealtimeMonitorView },
    { path: '/trajectory', name: 'trajectory', component: TrajectoryView },
    { path: '/fence', name: 'fence', component: FenceView },
    { path: '/alert', name: 'alert', component: AlertView },
    { path: '/analytics', name: 'analytics', component: AnalyticsView },
  ],
})

export default router

