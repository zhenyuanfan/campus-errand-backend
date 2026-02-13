package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.model.dto.order.OrderTrackingQueryRequest;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskTypeEnum;
import com.yuan.campuserrandbackend.model.vo.DeliveryTrackingVO;
import com.yuan.campuserrandbackend.model.vo.OrderTrackingVO;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.DeliveryTrackingService;
import com.yuan.campuserrandbackend.service.OrderTrackingService;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 订单跟踪Service实现
 */
@Service
public class OrderTrackingServiceImpl implements OrderTrackingService {

    @Resource
    private TaskService taskService;

    @Resource
    private UserService userService;

    @Resource
    private DeliveryTrackingService deliveryTrackingService;

    @Override
    public OrderTrackingVO getOrderTracking(long taskId) {
        if (taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }

        // 查询任务
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        OrderTrackingVO orderTrackingVO = new OrderTrackingVO();

        // ========== 设置任务基本信息 ==========
        orderTrackingVO.setTaskId(task.getId());
        orderTrackingVO.setTitle(task.getTitle());
        orderTrackingVO.setDescription(task.getDescription());
        orderTrackingVO.setTaskType(task.getTaskType());
        orderTrackingVO.setStartLocation(task.getStartLocation());
        orderTrackingVO.setEndLocation(task.getEndLocation());
        orderTrackingVO.setReward(task.getReward());
        orderTrackingVO.setContactInfo(task.getContactInfo());
        orderTrackingVO.setRemark(task.getRemark());
        orderTrackingVO.setExpectedTime(task.getExpectedTime());
        orderTrackingVO.setCreateTime(task.getCreateTime());
        orderTrackingVO.setTaskStatus(task.getStatus());

        // 任务类型文本
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(task.getTaskType());
        if (taskTypeEnum != null) {
            orderTrackingVO.setTaskTypeText(taskTypeEnum.getText());
        }

        // 任务状态文本
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.getEnumByValue(task.getStatus());
        if (taskStatusEnum != null) {
            orderTrackingVO.setTaskStatusText(taskStatusEnum.getText());
        }

        // ========== 设置发布者信息 ==========
        orderTrackingVO.setPublisherId(task.getPublisherId());
        if (task.getPublisherId() != null && task.getPublisherId() > 0) {
            User publisher = userService.getById(task.getPublisherId());
            if (publisher != null) {
                orderTrackingVO.setPublisherName(publisher.getUserName());
            }
        }

        // ========== 设置跑腿人员信息 ==========
        Long runnerId = task.getRunnerId();
        orderTrackingVO.setRunnerId(runnerId);
        if (runnerId != null && runnerId > 0) {
            User runner = userService.getById(runnerId);
            if (runner != null) {
                orderTrackingVO.setRunnerName(runner.getUserName());
                orderTrackingVO.setRunnerAvatar(runner.getUserAvatar());
                orderTrackingVO.setRunnerPhone(runner.getContactInfo());
                orderTrackingVO.setRunnerCreditScore(runner.getCreditScore());
            }
        }

        // ========== 设置配送状态和轨迹 ==========
        // 获取最新配送状态
        DeliveryTrackingVO latestTracking = deliveryTrackingService.getLatestTracking(taskId);
        if (latestTracking != null) {
            orderTrackingVO.setDeliveryStatus(latestTracking.getStatus());
            orderTrackingVO.setDeliveryStatusText(latestTracking.getStatusText());
            orderTrackingVO.setLatestDescription(latestTracking.getDescription());
            orderTrackingVO.setLatestAddress(latestTracking.getAddress());
            orderTrackingVO.setLongitude(latestTracking.getLongitude());
            orderTrackingVO.setLatitude(latestTracking.getLatitude());
        }

        // 获取配送轨迹列表
        List<DeliveryTrackingVO> trackingList = deliveryTrackingService.getDeliveryTrackingList(taskId);
        orderTrackingVO.setTrackingList(trackingList);

        return orderTrackingVO;
    }

    @Override
    public Page<TaskVO> listMyOrders(OrderTrackingQueryRequest orderTrackingQueryRequest, HttpServletRequest request) {
        if (orderTrackingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        long current = orderTrackingQueryRequest.getCurrent();
        long size = orderTrackingQueryRequest.getPageSize();
        String status = orderTrackingQueryRequest.getStatus();

        // 构建查询条件：只查询当前用户发布的任务
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("publisherId", loginUser.getId());

        // 按状态筛选
        if (StrUtil.isNotBlank(status)) {
            // 校验状态合法性
            TaskStatusEnum taskStatusEnum = TaskStatusEnum.getEnumByValue(status);
            if (taskStatusEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务状态不合法");
            }
            queryWrapper.eq("status", status);
        }

        // 排序
        String sortField = orderTrackingQueryRequest.getSortField();
        String sortOrder = orderTrackingQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }

        // 分页查询
        Page<Task> taskPage = taskService.page(new Page<>(current, size), queryWrapper);
        return taskService.getTaskVOPage(taskPage);
    }
}
