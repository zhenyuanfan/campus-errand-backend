package com.yuan.campuserrandbackend.model.dto.delivery;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新配送位置请求
 */
@Data
public class DeliveryLocationUpdateRequest implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;

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
