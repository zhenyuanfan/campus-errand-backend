package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.DeliveryTrackingMapper;
import com.yuan.campuserrandbackend.model.dto.delivery.DeliveryLocationUpdateRequest;
import com.yuan.campuserrandbackend.model.dto.delivery.DeliveryStatusUpdateRequest;
import com.yuan.campuserrandbackend.model.entity.DeliveryTracking;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.DeliveryStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.model.vo.DeliveryTrackingVO;
import com.yuan.campuserrandbackend.service.DeliveryTrackingService;
import com.yuan.campuserrandbackend.service.MessageService;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配送跟踪Service实现
 */
@Service
public class DeliveryTrackingServiceImpl extends ServiceImpl<DeliveryTrackingMapper, DeliveryTracking>
        implements DeliveryTrackingService {

    @Resource
    private TaskService taskService;

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeliveryStatus(DeliveryStatusUpdateRequest deliveryStatusUpdateRequest,
            HttpServletRequest request) {
        if (deliveryStatusUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long taskId = deliveryStatusUpdateRequest.getTaskId();
        String status = deliveryStatusUpdateRequest.getStatus();

        // 参数校验
        if (taskId == null || taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }
        if (StrUtil.isBlank(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "配送状态不能为空");
        }

        // 校验配送状态合法性
        DeliveryStatusEnum deliveryStatusEnum = DeliveryStatusEnum.getEnumByValue(status);
        if (deliveryStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "配送状态不合法");
        }

        // 校验描述长度
        String description = deliveryStatusUpdateRequest.getDescription();
        if (StrUtil.isNotBlank(description) && description.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "进度描述过长，最多500字");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 查询任务
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 校验权限：只有该任务的接单者可以更新配送状态
        if (!loginUser.getId().equals(task.getRunnerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有该任务的接单者可以更新配送状态");
        }

        // 校验任务状态：只有进行中的任务可以更新配送状态
        if (!TaskStatusEnum.IN_PROGRESS.getValue().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有进行中的任务可以更新配送状态");
        }

        // 创建配送跟踪记录
        DeliveryTracking deliveryTracking = new DeliveryTracking();
        deliveryTracking.setTaskId(taskId);
        deliveryTracking.setRunnerId(loginUser.getId());
        deliveryTracking.setStatus(status);
        deliveryTracking.setDescription(description);
        deliveryTracking.setLongitude(deliveryStatusUpdateRequest.getLongitude());
        deliveryTracking.setLatitude(deliveryStatusUpdateRequest.getLatitude());
        deliveryTracking.setAddress(deliveryStatusUpdateRequest.getAddress());

        boolean result = this.save(deliveryTracking);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新配送状态失败，数据库错误");
        }

        // 如果配送状态为已完成，同步更新任务状态为已完成
        if (DeliveryStatusEnum.COMPLETED.getValue().equals(status)) {
            Task updateTask = new Task();
            updateTask.setId(taskId);
            updateTask.setStatus(TaskStatusEnum.COMPLETED.getValue());
            taskService.updateById(updateTask);

            // 通知任务发布者
            messageService.sendToUser(task.getPublisherId(),
                    "您的任务已完成配送",
                    "您发布的任务「" + task.getTitle() + "」已由跑腿人员「" + loginUser.getUserName() + "」完成配送。请确认并评价。",
                    "DELIVERY_COMPLETED");
        } else {
            // 通知任务发布者配送进度更新
            messageService.sendToUser(task.getPublisherId(),
                    "配送进度更新",
                    "您发布的任务「" + task.getTitle() + "」配送状态已更新为：" + deliveryStatusEnum.getText()
                            + (StrUtil.isNotBlank(description) ? "，备注：" + description : ""),
                    "DELIVERY_STATUS_UPDATE");
        }

        return true;
    }

    @Override
    public boolean updateLocation(DeliveryLocationUpdateRequest deliveryLocationUpdateRequest,
            HttpServletRequest request) {
        if (deliveryLocationUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long taskId = deliveryLocationUpdateRequest.getTaskId();
        Double longitude = deliveryLocationUpdateRequest.getLongitude();
        Double latitude = deliveryLocationUpdateRequest.getLatitude();

        // 参数校验
        if (taskId == null || taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }
        if (longitude == null || latitude == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "经纬度不能为空");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 查询任务
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 校验权限：只有该任务的接单者可以上报位置
        if (!loginUser.getId().equals(task.getRunnerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有该任务的接单者可以上报位置");
        }

        // 校验任务状态：只有进行中的任务可以上报位置
        if (!TaskStatusEnum.IN_PROGRESS.getValue().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有进行中的任务可以上报位置");
        }

        // 获取当前最新的配送状态
        String currentStatus = getCurrentDeliveryStatus(taskId);

        // 创建位置轨迹记录
        DeliveryTracking deliveryTracking = new DeliveryTracking();
        deliveryTracking.setTaskId(taskId);
        deliveryTracking.setRunnerId(loginUser.getId());
        deliveryTracking.setStatus(currentStatus != null ? currentStatus : DeliveryStatusEnum.DELIVERING.getValue());
        deliveryTracking.setLongitude(longitude);
        deliveryTracking.setLatitude(latitude);
        deliveryTracking.setAddress(deliveryLocationUpdateRequest.getAddress());

        boolean result = this.save(deliveryTracking);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上报位置失败，数据库错误");
        }

        return true;
    }

    @Override
    public List<DeliveryTrackingVO> getDeliveryTrackingList(long taskId) {
        if (taskId <= 0) {
            return Collections.emptyList();
        }
        QueryWrapper<DeliveryTracking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("taskId", taskId);
        queryWrapper.orderByAsc("createTime");
        List<DeliveryTracking> trackingList = this.list(queryWrapper);
        return trackingList.stream()
                .map(this::getDeliveryTrackingVO)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryTrackingVO getLatestTracking(long taskId) {
        if (taskId <= 0) {
            return null;
        }
        QueryWrapper<DeliveryTracking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("taskId", taskId);
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("limit 1");
        DeliveryTracking deliveryTracking = this.getOne(queryWrapper);
        if (deliveryTracking == null) {
            return null;
        }
        return getDeliveryTrackingVO(deliveryTracking);
    }

    /**
     * 获取配送跟踪VO
     */
    private DeliveryTrackingVO getDeliveryTrackingVO(DeliveryTracking deliveryTracking) {
        if (deliveryTracking == null) {
            return null;
        }
        DeliveryTrackingVO deliveryTrackingVO = new DeliveryTrackingVO();
        BeanUtils.copyProperties(deliveryTracking, deliveryTrackingVO);

        // 设置配送状态文本
        DeliveryStatusEnum deliveryStatusEnum = DeliveryStatusEnum.getEnumByValue(deliveryTracking.getStatus());
        if (deliveryStatusEnum != null) {
            deliveryTrackingVO.setStatusText(deliveryStatusEnum.getText());
        }

        // 设置跑腿人员昵称
        Long runnerId = deliveryTracking.getRunnerId();
        if (runnerId != null && runnerId > 0) {
            User runner = userService.getById(runnerId);
            if (runner != null) {
                deliveryTrackingVO.setRunnerName(runner.getUserName());
            }
        }

        return deliveryTrackingVO;
    }

    /**
     * 获取当前最新的配送状态
     */
    private String getCurrentDeliveryStatus(long taskId) {
        QueryWrapper<DeliveryTracking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("taskId", taskId);
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("limit 1");
        DeliveryTracking latestTracking = this.getOne(queryWrapper);
        return latestTracking != null ? latestTracking.getStatus() : null;
    }
}
