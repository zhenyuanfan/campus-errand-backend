package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.model.dto.order.OrderTrackingQueryRequest;
import com.yuan.campuserrandbackend.model.vo.OrderTrackingVO;
import com.yuan.campuserrandbackend.model.vo.TaskVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 订单跟踪Service
 */
public interface OrderTrackingService {

    /**
     * 获取订单跟踪详情（聚合视图）
     *
     * @param taskId 任务id
     * @return 订单跟踪详情
     */
    OrderTrackingVO getOrderTracking(long taskId);

    /**
     * 用户查看我的订单列表（分页）
     *
     * @param orderTrackingQueryRequest 查询请求
     * @param request                   HTTP请求
     * @return 任务VO分页
     */
    Page<TaskVO> listMyOrders(OrderTrackingQueryRequest orderTrackingQueryRequest, HttpServletRequest request);
}
