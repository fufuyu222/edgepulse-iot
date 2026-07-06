import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 8000
})

export const api = {
  summary: () => http.get('/dashboard/summary').then((res) => res.data),
  devices: () => http.get('/devices').then((res) => res.data),
  latestTelemetry: () => http.get('/telemetry/latest').then((res) => res.data),
  telemetryByDevice: (deviceId) => http.get(`/telemetry/${deviceId}`).then((res) => res.data),
  alarms: () => http.get('/alarms').then((res) => res.data),
  updateAlarmStatus: (id, status) => http.patch(`/alarms/${id}/status`, null, { params: { status } }).then((res) => res.data)
}
