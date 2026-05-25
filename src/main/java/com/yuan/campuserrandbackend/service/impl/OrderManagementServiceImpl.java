package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.DeliveryTrackingMapper;
import com.yuan.campuserrandbackend.model.dto.order.OrderQueryRequest;
import com.yuan.campuserrandbackend.model.entity.DeliveryTracking;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.TaskReview;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.DeliveryStatusEnum;
import com.yuan.campuserrandbackend.model.enums.PaymentStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskTypeEnum;
import com.yuan.campuserrandbackend.model.vo.OrderDetailVO;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单管理Service实现
 */
@Service
public class OrderManagementServiceImpl implements OrderManagementService {

    @Resource
    private TaskService taskService;

    @Resource
    private UserService userService;

    @Resource
    private TaskReviewService taskReviewService;

    @Resource
    private DeliveryTrackingMapper deliveryTrackingMapper;

    @Resource
    private MessageService messageService;

    @Override
    public OrderDetailVO getOrderDetail(long taskId) {
        if (taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }

        // 查询任务
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        OrderDetailVO orderDetailVO = new OrderDetailVO();

        // ========== 设置任务基本信息 ==========
        orderDetailVO.setTaskId(task.getId());
        orderDetailVO.setTitle(task.getTitle());
        orderDetailVO.setDescription(task.getDescription());
        orderDetailVO.setTaskType(task.getTaskType());
        orderDetailVO.setStartLocation(task.getStartLocation());
        orderDetailVO.setEndLocation(task.getEndLocation());
        orderDetailVO.setReward(task.getReward());
        orderDetailVO.setContactInfo(task.getContactInfo());
        orderDetailVO.setRemark(task.getRemark());
        orderDetailVO.setExpectedTime(task.getExpectedTime());
        orderDetailVO.setCreateTime(task.getCreateTime());
        orderDetailVO.setTaskStatus(task.getStatus());

        // 支付状态
        orderDetailVO.setPaymentStatus(task.getPaymentStatus());
        PaymentStatusEnum paymentStatusEnum = PaymentStatusEnum.getEnumByValue(task.getPaymentStatus());
        if (paymentStatusEnum != null) {
            orderDetailVO.setPaymentStatusText(paymentStatusEnum.getText());
        }

        // 任务类型文本
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(task.getTaskType());
        if (taskTypeEnum != null) {
            orderDetailVO.setTaskTypeText(taskTypeEnum.getText());
        }

        // 任务状态文本
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.getEnumByValue(task.getStatus());
        if (taskStatusEnum != null) {
            orderDetailVO.setTaskStatusText(taskStatusEnum.getText());
        }

        // ========== 设置发布者信息 ==========
        orderDetailVO.setPublisherId(task.getPublisherId());
        if (task.getPublisherId() != null && task.getPublisherId() > 0) {
            User publisher = userService.getById(task.getPublisherId());
            if (publisher != null) {
                orderDetailVO.setPublisherName(publisher.getUserName());
            }
        }

        // ========== 设置跑腿人员信息 ==========
        Long runnerId = task.getRunnerId();
        orderDetailVO.setRunnerId(runnerId);
        if (runnerId != null && runnerId > 0) {
            User runner = userService.getById(runnerId);
            if (runner != null) {
                orderDetailVO.setRunnerName(runner.getUserName());
                orderDetailVO.setRunnerAvatar(runner.getUserAvatar());
                orderDetailVO.setRunnerPhone(runner.getContactInfo());
                orderDetailVO.setRunnerCreditScore(runner.getCreditScore());
            }
        }

        // ========== 设置评价信息 ==========
        QueryWrapper<TaskReview> reviewQueryWrapper = new QueryWrapper<>();
        reviewQueryWrapper.eq("taskId", taskId);
        reviewQueryWrapper.last("limit 1");
        TaskReview taskReview = taskReviewService.getOne(reviewQueryWrapper);
        if (taskReview != null) {
            orderDetailVO.setReviewed(true);
            orderDetailVO.setReviewScore(taskReview.getScore());
            orderDetailVO.setReviewContent(taskReview.getContent());
            orderDetailVO.setReviewTags(taskReview.getTags());
            orderDetailVO.setReviewTime(taskReview.getCreateTime());
        } else {
            orderDetailVO.setReviewed(false);
        }

        // ========== 设置完成时间 ==========
        QueryWrapper<DeliveryTracking> trackingQueryWrapper = new QueryWrapper<>();
        trackingQueryWrapper.eq("taskId", taskId);
        trackingQueryWrapper.eq("status", DeliveryStatusEnum.COMPLETED.getValue());
        trackingQueryWrapper.orderByDesc("createTime");
        trackingQueryWrapper.last("limit 1");
        DeliveryTracking completedTracking = deliveryTrackingMapper.selectOne(trackingQueryWrapper);
        if (completedTracking != null) {
            orderDetailVO.setCompletedTime(completedTracking.getCreateTime());
        }

        return orderDetailVO;
    }

    @Override
    public Page<TaskVO> listMyOrders(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        // 构建查询条件：只查询当前用户发布的任务
        QueryWrapper<Task> queryWrapper = buildQueryWrapper(orderQueryRequest);
        queryWrapper.eq("publisherId", loginUser.getId());

        long current = orderQueryRequest.getCurrent();
        long size = orderQueryRequest.getPageSize();

        Page<Task> taskPage = taskService.page(new Page<>(current, size), queryWrapper);
        return taskService.getTaskVOPage(taskPage);
    }

    @Override
    public Page<TaskVO> listMyRunnerOrders(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        if (orderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        // 构建查询条件：只查询当前用户接的任务
        QueryWrapper<Task> queryWrapper = buildQueryWrapper(orderQueryRequest);
        queryWrapper.eq("runnerId", loginUser.getId());

        long current = orderQueryRequest.getCurrent();
        long size = orderQueryRequest.getPageSize();

        Page<Task> taskPage = taskService.page(new Page<>(current, size), queryWrapper);
        return taskService.getTaskVOPage(taskPage);
    }

    /**
     * 构建通用查询条件
     */
    private QueryWrapper<Task> buildQueryWrapper(OrderQueryRequest orderQueryRequest) {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();

        String keyword = orderQueryRequest.getKeyword();
        String taskType = orderQueryRequest.getTaskType();
        String status = orderQueryRequest.getStatus();
        Date startTime = orderQueryRequest.getStartTime();
        Date endTime = orderQueryRequest.getEndTime();
        BigDecimal minReward = orderQueryRequest.getMinReward();
        BigDecimal maxReward = orderQueryRequest.getMaxReward();

        // 关键字搜索（同时模糊匹配标题和描述）
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("description", keyword));
        }

        // 任务类型筛选
        if (StrUtil.isNotBlank(taskType)) {
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(taskType);
            if (taskTypeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务类型不合法");
            }
            queryWrapper.eq("taskType", taskType);
        }

        // 状态筛选
        if (StrUtil.isNotBlank(status)) {
            TaskStatusEnum taskStatusEnum = TaskStatusEnum.getEnumByValue(status);
            if (taskStatusEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务状态不合法");
            }
            queryWrapper.eq("status", status);
        }

        // 时间范围筛选
        queryWrapper.ge(startTime != null, "createTime", startTime);
        queryWrapper.le(endTime != null, "createTime", endTime);

        // 报酬范围筛选
        queryWrapper.ge(minReward != null, "reward", minReward);
        queryWrapper.le(maxReward != null, "reward", maxReward);

        // 排序
        String sortField = orderQueryRequest.getSortField();
        String sortOrder = orderQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }

        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(long taskId, HttpServletRequest request) {
        if (taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }

        // 查询任务
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 校验权限：只有发布者可以确认收货
        User loginUser = userService.getLoginUser(request);
        if (!task.getPublisherId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有任务发布者可以确认收货");
        }

        // 校验任务状态：只有待确认状态可以确认
        if (!TaskStatusEnum.CONFIRMED.getValue().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有待确认状态的订单可以确认收货");
        }

        // === 担保交易：放款给接单员 ===
        Long runnerId = task.getRunnerId();
        if (runnerId != null && runnerId > 0) {
            User runner = userService.getById(runnerId);
            if (runner != null) {
                BigDecimal runnerBalance = runner.getBalance() != null ? runner.getBalance() : BigDecimal.ZERO;
                User updateRunner = new User();
                updateRunner.setId(runnerId);
                updateRunner.setBalance(runnerBalance.add(task.getReward()));
                userService.updateById(updateRunner);
            }
        }

        // 更新任务状态为已完成，支付状态为已放款
        Task updateTask = new Task();
        updateTask.setId(taskId);
        updateTask.setStatus(TaskStatusEnum.COMPLETED.getValue());
        updateTask.setPaymentStatus(PaymentStatusEnum.RELEASED.getValue());
        boolean result = taskService.updateById(updateTask);

        // 通知接单员：报酬已到账
        if (result && runnerId != null && runnerId > 0) {
            messageService.sendToUser(runnerId,
                    "报酬已到账",
                    "您完成的任务「" + task.getTitle() + "」已被发布者确认收货，报酬" + task.getReward() + "元已转入您的账户。",
                    "PAYMENT_RELEASED");
        }

        return result;
    }
}
