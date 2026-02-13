package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackAddRequest;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackQueryRequest;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackReplyRequest;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.FeedbackVO;
import com.yuan.campuserrandbackend.service.FeedbackService;
import com.yuan.campuserrandbackend.service.OperationLogService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 反馈接口
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @Resource
    private UserService userService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 提交反馈
     */
    @PostMapping("/add")
    public BaseResponse<Long> addFeedback(@RequestBody FeedbackAddRequest feedbackAddRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(feedbackAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long feedbackId = feedbackService.addFeedback(feedbackAddRequest, request);
        operationLogService.addLog(loginUser.getId(), "SUBMIT_FEEDBACK", "提交反馈，反馈ID: " + feedbackId);
        return ResultUtils.success(feedbackId);
    }

    /**
     * 查看反馈详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<FeedbackVO> getFeedbackDetail(long feedbackId, HttpServletRequest request) {
        ThrowUtils.throwIf(feedbackId <= 0, ErrorCode.PARAMS_ERROR);
        FeedbackVO feedbackVO = feedbackService.getFeedbackDetail(feedbackId, request);
        return ResultUtils.success(feedbackVO);
    }

    /**
     * 查看我的反馈列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<FeedbackVO>> listMyFeedbacks(@RequestBody FeedbackQueryRequest feedbackQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(feedbackQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = feedbackQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<FeedbackVO> feedbackPage = feedbackService.listMyFeedbacks(feedbackQueryRequest, request);
        return ResultUtils.success(feedbackPage);
    }

    /**
     * 管理员查看所有反馈
     */
    @PostMapping("/admin/list/page/vo")
    public BaseResponse<Page<FeedbackVO>> listAllFeedbacks(@RequestBody FeedbackQueryRequest feedbackQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(feedbackQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = feedbackQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<FeedbackVO> feedbackPage = feedbackService.listAllFeedbacks(feedbackQueryRequest, request);
        return ResultUtils.success(feedbackPage);
    }

    /**
     * 管理员回复/处理反馈
     */
    @PostMapping("/admin/reply")
    public BaseResponse<Boolean> replyFeedback(@RequestBody FeedbackReplyRequest feedbackReplyRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(feedbackReplyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = feedbackService.replyFeedback(feedbackReplyRequest, request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "REPLY_FEEDBACK",
                    "处理反馈，反馈ID: " + feedbackReplyRequest.getFeedbackId());
        }
        return ResultUtils.success(result);
    }
}
