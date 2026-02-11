package com.yuan.campuserrandbackend.service;

import com.yuan.campuserrandbackend.model.dto.delivery.DeliveryLocationUpdateRequest;
import com.yuan.campuserrandbackend.model.dto.delivery.DeliveryStatusUpdateRequest;
import com.yuan.campuserrandbackend.model.vo.DeliveryTrackingVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 配送跟踪Service
 */
public interface DeliveryTrackingService {

    /**
     * 更新配送状态
     *
     * @param deliveryStatusUpdateRequest 配送状态更新请求
     * @param request                     HTTP请求
     * @return 是否成功
     */
    boolean updateDeliveryStatus(DeliveryStatusUpdateRequest deliveryStatusUpdateRequest, HttpServletRequest request);

    /**
     * 上报当前位置
     *
     * @param deliveryLocationUpdateRequest 位置更新请求
     * @param request                       HTTP请求
     * @return 是否成功
     */
    boolean updateLocation(DeliveryLocationUpdateRequest deliveryLocationUpdateRequest, HttpServletRequest request);

    /**
     * 获取配送轨迹列表
     *
     * @param taskId 任务id
     * @return 配送轨迹列表
     */
    List<DeliveryTrackingVO> getDeliveryTrackingList(long taskId);

    /**
     * 获取最新配送状态
     *
     * @param taskId 任务id
     * @return 最新配送跟踪记录
     */
    DeliveryTrackingVO getLatestTracking(long taskId);
}
