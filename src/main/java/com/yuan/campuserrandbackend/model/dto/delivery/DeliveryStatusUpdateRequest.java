package com.yuan.campuserrandbackend.model.dto.delivery;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新配送状态请求
 */
@Data
public class DeliveryStatusUpdateRequest implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 配送状态：picked_up-已取件/delivering-配送中/arrived-已到达/completed-已完成
     */
    private String status;

    /**
     * 进度描述
     */
    private String description;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 当前地址
     */
    private String address;
}
