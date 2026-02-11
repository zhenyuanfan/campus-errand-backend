package com.yuan.campuserrandbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.order.OrderTrackingQueryRequest;
import com.yuan.campuserrandbackend.model.vo.OrderTrackingVO;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.OrderTrackingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单跟踪接口
 */
@RestController
@RequestMapping("/order")
public class OrderTrackingController {

    @Resource
    private OrderTrackingService orderTrackingService;

    /**
     * 获取订单跟踪详情（聚合视图）
     */
    @GetMapping("/tracking")
    public BaseResponse<OrderTrackingVO> getOrderTracking(long taskId) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        OrderTrackingVO orderTrackingVO = orderTrackingService.getOrderTracking(taskId);
        return ResultUtils.success(orderTrackingVO);
    }

    /**
     * 用户查看我的订单列表（分页）
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<TaskVO>> listMyOrders(@RequestBody OrderTrackingQueryRequest orderTrackingQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(orderTrackingQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long size = orderTrackingQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页数量不能超过20");
        Page<TaskVO> taskVOPage = orderTrackingService.listMyOrders(orderTrackingQueryRequest, request);
        return ResultUtils.success(taskVOPage);
    }
}
