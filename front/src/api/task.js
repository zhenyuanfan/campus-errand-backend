import request from './request'

// 发布任务
export const addTask = (data) => request.post('/task/add', data)

// 更新任务
export const updateTask = (data) => request.post('/task/update', data)

// 删除任务
export const deleteTask = (id) => request.post('/task/delete', { id })

// 取消任务
export const cancelTask = (id) => request.post('/task/cancel', { id })

// 获取任务详情
export const getTaskVO = (id) => request.get('/task/get/vo', { params: { id } })

// 分页获取任务列表
export const listTaskVOByPage = (data) => request.post('/task/list/page/vo', data)

// 获取我发布的任务列表
export const listMyTaskVOByPage = (data) => request.post('/task/my/list/page/vo', data)
