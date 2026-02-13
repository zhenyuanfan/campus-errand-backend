import request from './request'

// 更新配送状态
export const updateDeliveryStatus = (data) => request.post('/delivery/status/update', data)

// 上报位置
export const updateLocation = (data) => request.post('/delivery/location/update', data)

// 获取配送轨迹
export const getDeliveryTrackingList = (taskId) => request.get('/delivery/tracking/list', { params: { taskId } })

// 获取最新配送状态
export const getLatestTracking = (taskId) => request.get('/delivery/tracking/latest', { params: { taskId } })
