package com.yuan.campuserrandbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 任务评价
 * 
 * @TableName task_review
 */
@TableName(value = "task_review")
@Data
public class TaskReview implements Serializable {
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
     * 评价者id（发布者）
     */
    private Long reviewerId;

    /**
     * 被评价者id（跑腿人员）
     */
    private Long runnerId;

    /**
     * 评分（1-5分）
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签（如：速度快、态度好、准时送达等，逗号分隔）
     */
    private String tags;

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
