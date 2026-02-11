package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单跟踪视图对象（聚合视图）
 */
@Data
public class OrderTrackingVO implements Serializable {

    // ========== 任务基本信息 ==========

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务类型文本
     */
    private String taskTypeText;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 任务状态文本
     */
    private String taskStatusText;

    /**
     * 起始地点
     */
    private String startLocation;

    /**
     * 目的地
     */
    private String endLocation;

    /**
     * 报酬金额
     */
    private BigDecimal reward;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 预计完成时间
     */
    private Date expectedTime;

    /**
     * 任务创建时间
     */
    private Date createTime;

    // ========== 发布者信息 ==========

    /**
     * 发布者id
     */
    private Long publisherId;

    /**
     * 发布者昵称
     */
    private String publisherName;

    // ========== 跑腿人员信息 ==========

    /**
     * 跑腿人员id
     */
    private Long runnerId;

    /**
     * 跑腿人员昵称
     */
    private String runnerName;

    /**
     * 跑腿人员头像
     */
    private String runnerAvatar;

    /**
     * 跑腿人员联系方式
     */
    private String runnerPhone;

    /**
     * 跑腿人员信誉评分
     */
    private Double runnerCreditScore;

    // ========== 配送状态信息 ==========

    /**
     * 当前配送状态
     */
    private String deliveryStatus;

    /**
     * 当前配送状态文本
     */
    private String deliveryStatusText;

    /**
     * 最新进度描述
     */
    private String latestDescription;

    /**
     * 最新地址
     */
    private String latestAddress;

    /**
     * 当前经度
     */
    private Double longitude;

    /**
     * 当前纬度
     */
    private Double latitude;

    // ========== 配送轨迹 ==========

    /**
     * 配送轨迹列表
     */
    private List<DeliveryTrackingVO> trackingList;
}
