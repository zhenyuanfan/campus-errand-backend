package com.yuan.campuserrandbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 配送跟踪
 *
 * @TableName delivery_tracking
 */
@TableName(value = "delivery_tracking")
@Data
public class DeliveryTracking implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
