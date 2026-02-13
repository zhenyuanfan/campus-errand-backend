import request from './request'

// 提交评价
export const addReview = (data) => request.post('/review/add', data)

// 获取任务评价列表
export const getTaskReviewList = (taskId) => request.get('/review/list', { params: { taskId } })

// 获取跑腿人员信息
export const getRunnerVO = (runnerId) => request.get('/review/runner/get/vo', { params: { runnerId } })

// 获取跑腿人员评价（分页）
export const getRunnerReviewPage = (runnerId, current, size) => request.get('/review/runner/page', { params: { runnerId, current, size } })

// 回复评价
export const addReviewReply = (data) => request.post('/review/reply/add', data)

// 获取评价回复
export const listReviewReply = (reviewId) => request.get('/review/reply/list', { params: { reviewId } })

// 我的评价
export const listMyReviews = (data) => request.post('/review/my/list/page/vo', data)

// 收到的评价
export const listReceivedReviews = (data) => request.post('/review/received/list/page/vo', data)

// 评价统计
export const getReviewStats = (runnerId) => request.get('/review/stats', { params: { runnerId } })
