package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.entity.Message;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.service.MessageService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    @GetMapping("/my")
    public BaseResponse<Page<Message>> listMyMessage(
            @RequestParam(required = false, defaultValue = "1") long current,
            @RequestParam(required = false, defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer isRead,
            HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        if (isRead != null) {
            queryWrapper.eq("isRead", isRead);
        }
        queryWrapper.orderByDesc("createTime");
        Page<Message> page = messageService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    @PostMapping("/read/{id}")
    public BaseResponse<Boolean> readMessage(@PathVariable Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Message message = messageService.getById(id);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!loginUser.getId().equals(message.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Message update = new Message();
        update.setId(id);
        update.setIsRead(1);
        boolean result = messageService.updateById(update);
        return ResultUtils.success(result);
    }
}
