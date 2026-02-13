import request from './request'

// 平台概览
export const getOverviewStats = () => request.get('/analysis/overview')

// 任务分析
export const getTaskAnalysis = () => request.get('/analysis/task')

// 用户分析
export const getUserAnalysis = () => request.get('/analysis/user')

// 跑腿人员分析
export const getRunnerAnalysis = () => request.get('/analysis/runner')
