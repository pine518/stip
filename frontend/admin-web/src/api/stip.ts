import { deleteData, getData, patchData, postData } from './http'

export interface Overview {
  onlineEntities: number
  todayTrajectoryPoints: number
  enabledFences: number
  openAlerts: number
}

export interface LatestLocation {
  entityId: number
  entityCode: string
  entityName: string
  entityType: string
  longitude: number
  latitude: number
  speed?: number
  direction?: number
  eventTime: string
}

export interface EntitySummary {
  id: number
  entityCode: string
  entityName: string
  entityType: string
  status: string
}

export interface TrajectoryPoint {
  id: number
  longitude: number
  latitude: number
  speed?: number
  direction?: number
  eventTime: string
}

export interface LocationEventPayload {
  eventId: string
  entityCode: string
  entityType: string
  sourceType: string
  longitude: number
  latitude: number
  speed?: number
  direction?: number
  accuracy?: number
  eventTime: string
}

export interface LocationEventResult {
  trajectoryPointId: number
  accepted: boolean
}

export interface Fence {
  id: number
  fenceName: string
  fenceType: string
  ruleType: string
  geometryGeoJson: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateFencePayload {
  fenceName: string
  fenceType: string
  ruleType: string
  geometryWkt: string
  enabled: boolean
}

export interface AlertEvent {
  id: number
  alertType: string
  alertLevel: string
  entityId: number
  entityCode: string
  entityName: string
  fenceId?: number
  message: string
  longitude?: number
  latitude?: number
  eventTime: string
  status: string
  handledBy?: number
  handledAt?: string
  createdAt: string
}

export function getOverview() {
  return getData<Overview>('/analytics/overview')
}

export function getLatestLocations() {
  return getData<LatestLocation[]>('/entities/latest-locations')
}

export function getEntities() {
  return getData<EntitySummary[]>('/entities')
}

export function getTrajectory(entityId: number, startTime: string, endTime: string) {
  return getData<TrajectoryPoint[]>(`/entities/${entityId}/trajectory`, { startTime, endTime })
}

export function submitLocationEvent(payload: LocationEventPayload) {
  return postData<LocationEventResult>('/location-events', payload)
}

export function getFences() {
  return getData<Fence[]>('/fences')
}

export function createFence(payload: CreateFencePayload) {
  return postData<Fence>('/fences', payload)
}

export function setFenceEnabled(id: number, enabled: boolean) {
  return patchData<Fence>(`/fences/${id}/enabled`, { enabled })
}

export function deleteFence(id: number) {
  return deleteData<void>(`/fences/${id}`)
}

export function getAlerts(status?: string) {
  return getData<AlertEvent[]>('/alerts', status ? { status } : undefined)
}

export function handleAlert(id: number) {
  return postData<AlertEvent>(`/alerts/${id}/handle`, { handledBy: 1 })
}

export function closeAlert(id: number) {
  return postData<AlertEvent>(`/alerts/${id}/close`, { handledBy: 1 })
}
