import request from './request'

// 订单跟踪详情
export const getOrderTracking = (taskId) => request.get('/order/tracking', { params: { taskId } })

// 我的订单列表（跟踪视角）
export const listMyOrders = (data) => request.post('/order/my/list/page/vo', data)

// 订单详情（含评价）
export const getOrderDetail = (taskId) => request.get('/order/manage/detail', { params: { taskId } })

// 我发布的订单（管理）
export const listMyPublishedOrders = (data) => request.post('/order/manage/my/list/page/vo', data)

// 我接的订单（跑腿视角）
export const listMyRunnerOrders = (data) => request.post('/order/manage/runner/list/page/vo', data)
