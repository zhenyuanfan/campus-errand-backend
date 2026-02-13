import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
    baseURL: '/api',
    timeout: 15000,
    withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(
    (config) => config,
    (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const { code, data, message } = response.data
        if (code === 0) {
            return data
        }
        // 未登录
        if (code === 40100) {
            if (!response.config.silent) {
                ElMessage.warning('请先登录')
                router.push('/login')
            }
            return Promise.reject(new Error(message))
        }
        ElMessage.error(message || '请求失败')
        return Promise.reject(new Error(message))
    },
    (error) => {
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request
