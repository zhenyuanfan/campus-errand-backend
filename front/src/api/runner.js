import request from './request'

// 查看待接单任务列表
export const listAvailableTasks = (data) => request.post('/runner/task/list/page/vo', data)

// 接单
export const acceptTask = (taskId) => request.post('/runner/task/accept', { taskId })
