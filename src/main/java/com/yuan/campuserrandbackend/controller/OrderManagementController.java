package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.order.OrderQueryRequest;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.OrderDetailVO;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.OperationLogService;
import com.yuan.campuserrandbackend.service.OrderManagementService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单管理接口
 */
@RestController
@RequestMapping("/order/manage")
public class OrderManagementController {

    @Resource
    private OrderManagementService orderManagementService;

    @Resource
    private UserService userService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 获取订单详情（含评价信息）
     */
    @GetMapping("/detail")
    public BaseResponse<OrderDetailVO> getOrderDetail(long taskId) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        OrderDetailVO orderDetailVO = orderManagementService.getOrderDetail(taskId);
        return ResultUtils.success(orderDetailVO);
    }

    /**
     * 查看我发布的订单列表（高级筛选）
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<TaskVO>> listMyOrders(@RequestBody OrderQueryRequest orderQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(orderQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = orderQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<TaskVO> taskVOPage = orderManagementService.listMyOrders(orderQueryRequest, request);
        return ResultUtils.success(taskVOPage);
    }

    /**
     * 查看我接的订单列表（跑腿人员视角）
     */
    @PostMapping("/runner/list/page/vo")
    public BaseResponse<Page<TaskVO>> listMyRunnerOrders(@RequestBody OrderQueryRequest orderQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(orderQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = orderQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<TaskVO> taskVOPage = orderManagementService.listMyRunnerOrders(orderQueryRequest, request);
        return ResultUtils.success(taskVOPage);
    }

    /**
     * 确认收货（发布者确认后放款给接单员）
     */
    @PostMapping("/confirm/{taskId}")
    public BaseResponse<Boolean> confirmOrder(@PathVariable long taskId, HttpServletRequest request) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = orderManagementService.confirmOrder(taskId, request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "CONFIRM_ORDER", "确认收货，任务ID: " + taskId);
        }
        return ResultUtils.success(result);
    }
}
