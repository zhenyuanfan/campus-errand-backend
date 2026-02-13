import request from './request'

// 用户注册
export const userRegister = (data) => request.post('/user/register', data)

// 账号密码登录
export const userLogin = (data) => request.post('/user/login', data)

// 发送短信验证码
export const sendSmsCode = (data) => request.post('/user/sendSmsCode', data)

// 短信验证码登录
export const smsLogin = (data) => request.post('/user/login/sms', data)

// 获取当前登录用户
export const getLoginUser = () => request.get('/user/get/login', { silent: true })

// 退出登录
export const userLogout = () => request.post('/user/logout')

// 更新个人信息
export const updateMyUser = (data) => request.post('/user/update', data)

// 上传头像
export const uploadAvatar = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/user/upload/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 绑定手机号
export const bindPhone = (data) => request.post('/user/bind/phone', data)
