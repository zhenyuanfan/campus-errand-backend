package com.yuan.campuserrandbackend.controller;

import com.yuan.campuserrandbackend.common.BaseResponse;
import com.yuan.campuserrandbackend.common.ResultUtils;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.exception.ThrowUtils;
import com.yuan.campuserrandbackend.model.dto.delivery.DeliveryLocationUpdateRequest;
import com.yuan.campuserrandbackend.model.dto.delivery.DeliveryStatusUpdateRequest;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.DeliveryTrackingVO;
import com.yuan.campuserrandbackend.service.DeliveryTrackingService;
import com.yuan.campuserrandbackend.service.OperationLogService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 配送跟踪接口
 */
@RestController
@RequestMapping("/delivery")
public class DeliveryTrackingController {

    @Resource
    private DeliveryTrackingService deliveryTrackingService;

    @Resource
    private UserService userService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * 更新配送状态
     */
    @PostMapping("/status/update")
    public BaseResponse<Boolean> updateDeliveryStatus(
            @RequestBody DeliveryStatusUpdateRequest deliveryStatusUpdateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(deliveryStatusUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = deliveryTrackingService.updateDeliveryStatus(deliveryStatusUpdateRequest, request);
        if (result) {
            operationLogService.addLog(loginUser.getId(), "UPDATE_DELIVERY_STATUS",
                    "更新配送状态，任务ID: " + deliveryStatusUpdateRequest.getTaskId() + "，状态: "
                            + deliveryStatusUpdateRequest.getStatus());
        }
        return ResultUtils.success(result);
    }

    /**
     * 上报当前位置
     */
    @PostMapping("/location/update")
    public BaseResponse<Boolean> updateLocation(
            @RequestBody DeliveryLocationUpdateRequest deliveryLocationUpdateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(deliveryLocationUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = deliveryTrackingService.updateLocation(deliveryLocationUpdateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 获取配送轨迹列表
     */
    @GetMapping("/tracking/list")
    public BaseResponse<List<DeliveryTrackingVO>> getDeliveryTrackingList(long taskId) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        List<DeliveryTrackingVO> trackingList = deliveryTrackingService.getDeliveryTrackingList(taskId);
        return ResultUtils.success(trackingList);
    }

    /**
     * 获取最新配送状态
     */
    @GetMapping("/tracking/latest")
    public BaseResponse<DeliveryTrackingVO> getLatestTracking(long taskId) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        DeliveryTrackingVO latestTracking = deliveryTrackingService.getLatestTracking(taskId);
        return ResultUtils.success(latestTracking);
    }
}
