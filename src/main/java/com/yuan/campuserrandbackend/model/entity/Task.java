package com.yuan.campuserrandbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 跑腿任务
 * 
 * @TableName task
 */
@TableName(value = "task")
@Data
public class Task implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发布者id
     */
    private Long publisherId;

    /**
     * 接单者id
     */
    private Long runnerId;

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
     * 任务状态：pending-待接单/accepted-已接单/in_progress-进行中/completed-已完成/cancelled-已取消
     */
    private String status;

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

    /**
     * 是否删除（逻辑删除）
     */
    @TableLogic
    private Integer isDelete;
}
