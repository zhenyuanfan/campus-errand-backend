import request from './request'

// 提交反馈
export const addFeedback = (data) => request.post('/feedback/add', data)

// 反馈详情
export const getFeedbackVO = (feedbackId) => request.get('/feedback/get/vo', { params: { feedbackId } })

// 我的反馈列表
export const listMyFeedback = (data) => request.post('/feedback/my/list/page/vo', data)

// 管理员反馈列表
export const listAllFeedback = (data) => request.post('/feedback/admin/list/page/vo', data)

// 管理员回复
export const adminReplyFeedback = (data) => request.post('/feedback/admin/reply', data)

// 接单员查看相关投诉
export const listRunnerFeedback = (data) => request.post('/feedback/runner/list/page/vo', data)

// 接单员提交申诉
export const appealFeedback = (data) => request.post('/feedback/runner/appeal', data)
