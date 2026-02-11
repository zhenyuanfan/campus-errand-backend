package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.task.RunnerTaskQueryRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskAcceptRequest;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.OperationLogService;
import com.yuan.campuserrandbackend.service.RunnerTaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 跑腿人员接单接口
 */
@RestController
@RequestMapping("/runner/task")
public class RunnerTaskController {

    @Resource
    private RunnerTaskService runnerTaskService;

    @Resource
    private UserService userService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 查看待接单任务列表（分页）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<TaskVO>> listAvailableTasks(@RequestBody RunnerTaskQueryRequest runnerTaskQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(runnerTaskQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = runnerTaskQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<TaskVO> taskVOPage = runnerTaskService.listAvailableTasks(runnerTaskQueryRequest);
        return ResultUtils.success(taskVOPage);
    }

    /**
     * 接单
     */
    @PostMapping("/accept")
    public BaseResponse<Boolean> acceptTask(@RequestBody TaskAcceptRequest taskAcceptRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(taskAcceptRequest == null || taskAcceptRequest.getTaskId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = runnerTaskService.acceptTask(taskAcceptRequest.getTaskId(), request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "ACCEPT_TASK", "接单，任务ID: " + taskAcceptRequest.getTaskId());
        }
        return ResultUtils.success(result);
    }
}
