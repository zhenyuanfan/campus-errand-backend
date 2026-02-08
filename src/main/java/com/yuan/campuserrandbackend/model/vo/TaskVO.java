package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 任务视图对象
 */
@Data
public class TaskVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 发布者id
     */
    private Long publisherId;

    /**
     * 发布者昵称
     */
    private String publisherName;

    /**
     * 接单者id
     */
    private Long runnerId;

    /**
     * 接单者昵称
     */
    private String runnerName;

    /**
     * 任务类型：file_delivery-文件传递/goods_purchase-物品采购/food_delivery-餐饮配送/other-其他
     */
    private String taskType;

    /**
     * 任务类型文本
     */
    private String taskTypeText;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 起始地点
     */
    private String startLocation;

    /**
     * 目的地
     */
    private String endLocation;

    /**
     * 期望完成时间
     */
    private Date expectedTime;

    /**
     * 报酬金额
     */
    private BigDecimal reward;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 任务状态文本
     */
    private String statusText;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
