import request from './request'

// 我的消息列表
export const listMyMessages = (params) => request.get('/message/my', { params })

// 标记消息已读
export const readMessage = (id) => request.post(`/message/read/${id}`)

// 我的操作记录
export const listMyOperationLog = (params) => request.get('/operationLog/my', { params })
