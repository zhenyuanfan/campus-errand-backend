package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.model.dto.order.OrderQueryRequest;
import com.yuan.campuserrandbackend.model.vo.OrderDetailVO;
import com.yuan.campuserrandbackend.model.vo.TaskVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 订单管理Service
 */
public interface OrderManagementService {

    /**
     * 获取订单详情（含评价信息）
     *
     * @param taskId 任务id
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(long taskId);

    /**
     * 查看我发布的订单列表（高级筛选）
     *
     * @param orderQueryRequest 查询请求
     * @param request           HTTP请求
     * @return 任务VO分页
     */
    Page<TaskVO> listMyOrders(OrderQueryRequest orderQueryRequest, HttpServletRequest request);

    /**
     * 查看我接的订单列表（跑腿人员视角）
     *
     * @param orderQueryRequest 查询请求
     * @param request           HTTP请求
     * @return 任务VO分页
     */
    Page<TaskVO> listMyRunnerOrders(OrderQueryRequest orderQueryRequest, HttpServletRequest request);
}
