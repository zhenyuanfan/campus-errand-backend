package com.yuan.campuserrandbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.annotation.AuthCheck;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.DeleteRequest;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.user.*;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.UserRoleEnum;
import com.yuan.campuserrandbackend.model.vo.LoginUserVO;
import com.yuan.campuserrandbackend.model.vo.UserAvatarUploadVO;
import com.yuan.campuserrandbackend.model.vo.UserVO;
import com.yuan.campuserrandbackend.service.OperationLogService;
import com.yuan.campuserrandbackend.service.UserService;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userRole = userRegisterRequest.getUserRole();
        String contactInfo = userRegisterRequest.getContactInfo();
        
        // 如果没有指定角色，默认为普通用户
        if (userRole == null || userRole.isEmpty()) {
            userRole = "user";
        }
        
        long result = userService.userRegister(userAccount, userPassword, checkPassword, userRole, contactInfo);
        operationLogService.addLog(result, "REGISTER", "用户账号: " + userAccount);
        return ResultUtils.success(result);
    }
    
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        operationLogService.addLog(loginUserVO.getId(), "LOGIN", "账号密码登录");
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 发送短信验证码（模拟）
     */
    @PostMapping("/sendSmsCode")
    public BaseResponse<Boolean> sendSmsCode(@RequestBody UserSmsCodeRequest smsCodeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(smsCodeRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.sendSmsCode(smsCodeRequest.getPhone(), request);
        return ResultUtils.success(result);
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/login/sms")
    public BaseResponse<LoginUserVO> smsLogin(@RequestBody UserSmsLoginRequest smsLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(smsLoginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.smsLogin(smsLoginRequest.getPhone(), smsLoginRequest.getCode(), request);
        operationLogService.addLog(loginUserVO.getId(), "LOGIN_SMS", "手机号: " + smsLoginRequest.getPhone());
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 更新个人信息
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userUpdateMyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserName(userUpdateMyRequest.getUserName());
        user.setUserProfile(userUpdateMyRequest.getUserProfile());
        user.setContactInfo(userUpdateMyRequest.getContactInfo());
        boolean result = userService.updateById(user);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "UPDATE_PROFILE", "更新基本资料");
        }
        return ResultUtils.success(result);
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<UserAvatarUploadVO> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String url = userService.uploadAvatar(multipartFile, request);
        UserAvatarUploadVO vo = new UserAvatarUploadVO();
        vo.setUrl(url);
        operationLogService.addLog(loginUser.getId(), "UPLOAD_AVATAR", "上传新头像");
        return ResultUtils.success(vo);
    }

    /**
     * 绑定/更换手机号
     */
    @PostMapping("/bind/phone")
    public BaseResponse<Boolean> bindPhone(@RequestBody UserBindPhoneRequest userBindPhoneRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userBindPhoneRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = userService.bindPhone(userBindPhoneRequest.getPhone(), userBindPhoneRequest.getCode(), request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "BIND_PHONE", "绑定手机号: " + userBindPhoneRequest.getPhone());
        }
        return ResultUtils.success(result);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }
    
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    // ==================== 管理员 - 用户管理 ====================

    /**
     * 管理员分页查询用户列表
     */
    @PostMapping("/admin/list")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        // 限制每页最多50条
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR, "每页最多50条");

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userRole = userQueryRequest.getUserRole();
        if (StrUtil.isNotBlank(userName)) {
            queryWrapper.like("userName", userName);
        }
        if (StrUtil.isNotBlank(userAccount)) {
            queryWrapper.like("userAccount", userAccount);
        }
        if (StrUtil.isNotBlank(userRole)) {
            queryWrapper.eq("userRole", userRole);
        }
        queryWrapper.orderByDesc("createTime");

        Page<User> userPage = userService.page(new Page<>(current, pageSize), queryWrapper);
        // 转换为 VO（脱敏，不暴露密码）
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    /**
     * 管理员修改用户角色
     */
    @PostMapping("/admin/updateRole")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateUserRole(@RequestBody UserRoleUpdateRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR);
        String newRole = request.getUserRole();
        // 校验角色合法性
        UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(newRole);
        ThrowUtils.throwIf(roleEnum == null, ErrorCode.PARAMS_ERROR, "角色不合法");
        // 不能修改自己的角色
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser.getId().equals(request.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能修改自己的角色");
        }
        User user = new User();
        user.setId(request.getId());
        user.setUserRole(newRole);
        boolean result = userService.updateById(user);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "ADMIN_UPDATE_ROLE",
                    "将用户" + request.getId() + "角色修改为" + newRole);
        }
        return ResultUtils.success(result);
    }

    /**
     * 管理员删除用户（逻辑删除）
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        // 不能删除自己
        if (loginUser.getId().equals(deleteRequest.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能删除自己");
        }
        boolean result = userService.removeById(deleteRequest.getId());
        if (result) {
            operationLogService.addLog(loginUser.getId(), "ADMIN_DELETE_USER",
                    "删除用户: " + deleteRequest.getId());
        }
        return ResultUtils.success(result);
    }
}