import type { LocationEventPayload } from '@/api/stip'

export function buildSimulatedLocationEvent(): LocationEventPayload {
  const now = new Date()
  const suffix = now.getTime()
  const offset = Math.round(Math.random() * 1000) / 100000

  return {
    eventId: `sim-gps-${suffix}`,
    entityCode: 'CAR-001',
    entityType: 'VEHICLE',
    sourceType: 'GPS',
    longitude: Number((121.473701 + offset).toFixed(6)),
    latitude: Number((31.230416 + offset / 2).toFixed(6)),
    speed: 36.5,
    direction: 92,
    accuracy: 8,
    eventTime: now.toISOString(),
  }
}

