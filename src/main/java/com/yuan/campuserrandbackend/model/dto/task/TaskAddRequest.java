package com.yuan.campuserrandbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 发布任务请求
 */
@Data
public class TaskAddRequest implements Serializable {

    /**
     * 任务类型：file_delivery-文件传递/goods_purchase-物品采购/food_delivery-餐饮配送/other-其他
     */
    private String taskType;

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
     * 联系方式
     */
    private String contactInfo;

    /**
     * 备注
     */
    private String remark;
}
