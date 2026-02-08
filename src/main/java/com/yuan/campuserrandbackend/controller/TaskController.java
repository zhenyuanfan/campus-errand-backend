package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.DeleteRequest;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.task.TaskAddRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskQueryRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskReviewAddRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskUpdateRequest;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.RunnerVO;
import com.yuan.campuserrandbackend.model.vo.TaskReviewVO;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.OperationLogService;
import com.yuan.campuserrandbackend.service.TaskReviewService;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 任务接口
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @Resource
    private UserService userService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 发布任务
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTask(@RequestBody TaskAddRequest taskAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(taskAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long taskId = taskService.addTask(taskAddRequest, request);
        operationLogService.addLog(loginUser.getId(), "PUBLISH_TASK", "发布任务，任务ID: " + taskId);
        return ResultUtils.success(taskId);
    }

    /**
     * 更新任务
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTask(@RequestBody TaskUpdateRequest taskUpdateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(taskUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(taskUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = taskService.updateTask(taskUpdateRequest, request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "UPDATE_TASK", "更新任务，任务ID: " + taskUpdateRequest.getId());
        }
        return ResultUtils.success(result);
    }

    /**
     * 删除任务
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTask(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = taskService.deleteTask(deleteRequest.getId(), request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "DELETE_TASK", "删除任务，任务ID: " + deleteRequest.getId());
        }
        return ResultUtils.success(result);
    }

    /**
     * 取消任务
     */
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelTask(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = taskService.cancelTask(deleteRequest.getId(), request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "CANCEL_TASK", "取消任务，任务ID: " + deleteRequest.getId());
        }
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取任务（VO）
     */
    @GetMapping("/get/vo")
    public BaseResponse<TaskVO> getTaskVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Task task = taskService.getById(id);
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(taskService.getTaskVO(task));
    }

    /**
     * 分页获取任务列表（VO）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<TaskVO>> listTaskVOByPage(@RequestBody TaskQueryRequest taskQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(taskQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = taskQueryRequest.getCurrent();
        long size = taskQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<Task> taskPage = taskService.page(new Page<>(current, size),
                taskService.getQueryWrapper(taskQueryRequest));
        return ResultUtils.success(taskService.getTaskVOPage(taskPage));
    }

    /**
     * 获取我发布的任务列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<TaskVO>> listMyTaskVOByPage(@RequestBody TaskQueryRequest taskQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(taskQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        taskQueryRequest.setPublisherId(loginUser.getId());
        long current = taskQueryRequest.getCurrent();
        long size = taskQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<Task> taskPage = taskService.page(new Page<>(current, size),
                taskService.getQueryWrapper(taskQueryRequest));
        return ResultUtils.success(taskService.getTaskVOPage(taskPage));
    }

    @Resource
    private TaskReviewService taskReviewService;

    /**
     * 评价任务
     */
    @PostMapping("/review/add")
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
    @GetMapping("/review/list")
    public BaseResponse<java.util.List<TaskReviewVO>> getTaskReviewList(long taskId) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(taskReviewService.getTaskReviewList(taskId));
    }

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
    @GetMapping("/runner/review/page")
    public BaseResponse<Page<TaskReviewVO>> getRunnerReviewPage(long runnerId, long current, long size) {
        ThrowUtils.throwIf(runnerId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        return ResultUtils.success(taskReviewService.getRunnerReviewPage(runnerId, current, size));
    }
}
