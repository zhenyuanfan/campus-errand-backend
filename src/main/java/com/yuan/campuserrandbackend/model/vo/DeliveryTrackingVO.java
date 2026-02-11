package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 配送跟踪视图对象
 */
@Data
public class DeliveryTrackingVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 跑腿人员id
     */
    private Long runnerId;

    /**
     * 跑腿人员昵称
     */
    private String runnerName;

    /**
     * 配送状态
     */
    private String status;

    /**
     * 配送状态文本
     */
    private String statusText;

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

    /**
     * 创建时间
     */
    private Date createTime;
}
