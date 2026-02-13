package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.review.ReviewQueryRequest;
import com.yuan.campuserrandbackend.model.dto.review.ReviewReplyAddRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskReviewAddRequest;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.*;
import com.yuan.campuserrandbackend.service.OperationLogService;
import com.yuan.campuserrandbackend.service.ReviewReplyService;
import com.yuan.campuserrandbackend.service.TaskReviewService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评价接口
 */
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Resource
    private TaskReviewService taskReviewService;

    @Resource
    private ReviewReplyService reviewReplyService;

    @Resource
    private UserService userService;

    @Resource
    private OperationLogService operationLogService;

    // ==================== 评价基础操作 ====================

    /**
     * 评价任务
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTaskReview(@RequestBody TaskReviewAddRequest taskReviewAddRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(taskReviewAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long reviewId = taskReviewService.addTaskReview(taskReviewAddRequest, request);
        operationLogService.addLog(loginUser.getId(), "REVIEW_TASK", "评价任务，任务ID: " + taskReviewAddRequest.getTaskId());
        return ResultUtils.success(reviewId);
    }

    /**
     * 获取任务的评价列表
     */
    @GetMapping("/list")
    public BaseResponse<List<TaskReviewVO>> getTaskReviewList(long taskId) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(taskReviewService.getTaskReviewList(taskId));
    }

    // ==================== 跑腿人员相关 ====================

    /**
     * 获取跑腿人员信息
     */
    @GetMapping("/runner/get/vo")
    public BaseResponse<RunnerVO> getRunnerVO(long runnerId) {
        ThrowUtils.throwIf(runnerId <= 0, ErrorCode.PARAMS_ERROR);
        RunnerVO runnerVO = taskReviewService.getRunnerVO(runnerId);
        ThrowUtils.throwIf(runnerVO == null, ErrorCode.NOT_FOUND_ERROR, "跑腿人员不存在");
        return ResultUtils.success(runnerVO);
    }

    /**
     * 获取跑腿人员的评价列表（分页）
     */
    @GetMapping("/runner/page")
    public BaseResponse<Page<TaskReviewVO>> getRunnerReviewPage(long runnerId, long current, long size) {
        ThrowUtils.throwIf(runnerId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        return ResultUtils.success(taskReviewService.getRunnerReviewPage(runnerId, current, size));
    }

    // ==================== 评价查询 ====================

    /**
     * 查看我发出的评价列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<TaskReviewVO>> listMyReviews(@RequestBody ReviewQueryRequest reviewQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(reviewQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = reviewQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<TaskReviewVO> reviewPage = taskReviewService.listMyReviews(reviewQueryRequest, request);
        return ResultUtils.success(reviewPage);
    }

    /**
     * 查看我收到的评价列表
     */
    @PostMapping("/received/list/page/vo")
    public BaseResponse<Page<TaskReviewVO>> listReceivedReviews(@RequestBody ReviewQueryRequest reviewQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(reviewQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = reviewQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<TaskReviewVO> reviewPage = taskReviewService.listReceivedReviews(reviewQueryRequest, request);
        return ResultUtils.success(reviewPage);
    }

    // ==================== 评价统计 ====================

    /**
     * 获取跑腿人员评价统计
     */
    @GetMapping("/stats")
    public BaseResponse<ReviewStatsVO> getReviewStats(long runnerId) {
        ThrowUtils.throwIf(runnerId <= 0, ErrorCode.PARAMS_ERROR);
        ReviewStatsVO reviewStatsVO = taskReviewService.getReviewStats(runnerId);
        return ResultUtils.success(reviewStatsVO);
    }

    // ==================== 评价回复 ====================

    /**
     * 回复评价
     */
    @PostMapping("/reply/add")
    public BaseResponse<Long> addReply(@RequestBody ReviewReplyAddRequest reviewReplyAddRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(reviewReplyAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long replyId = reviewReplyService.addReply(reviewReplyAddRequest, request);
        operationLogService.addLog(loginUser.getId(), "REPLY_REVIEW",
                "回复评价，评价ID: " + reviewReplyAddRequest.getReviewId());
        return ResultUtils.success(replyId);
    }

    /**
     * 获取评价的回复列表
     */
    @GetMapping("/reply/list")
    public BaseResponse<List<ReviewReplyVO>> getReplyList(long reviewId) {
        ThrowUtils.throwIf(reviewId <= 0, ErrorCode.PARAMS_ERROR);
        List<ReviewReplyVO> replyList = reviewReplyService.getReplyList(reviewId);
        return ResultUtils.success(replyList);
    }
}
