package com.yuan.campuserrandbackend.service;

import com.yuan.campuserrandbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.campuserrandbackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86182
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-11-27 19:33:46
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param userRole      用户角色
     * @param contactInfo   联系方式（如手机号）
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userRole, String contactInfo);
    
    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);
    
    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
    
    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);
    
    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    String getEncryptPassword(String userPassword);

    /**
     * 发送手机验证码（模拟）
     *
     * @param phone   手机号
     * @param request 请求
     * @return 是否发送成功
     */
    boolean sendSmsCode(String phone, HttpServletRequest request);

    /**
     * 手机验证码登录
     *
     * @param phone   手机号
     * @param code    验证码
     * @param request 请求
     * @return 登录用户
     */
    LoginUserVO smsLogin(String phone, String code, HttpServletRequest request);
}