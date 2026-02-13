package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.DeleteRequest;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.task.TaskAddRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskQueryRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskUpdateRequest;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.OperationLogService;
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
}
