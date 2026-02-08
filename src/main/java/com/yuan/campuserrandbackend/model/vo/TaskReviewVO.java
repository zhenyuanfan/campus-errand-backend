package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务评价视图对象
 */
@Data
public class TaskReviewVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务标题
     */
    private String taskTitle;

    /**
     * 评价者id
     */
    private Long reviewerId;

    /**
     * 评价者昵称
     */
    private String reviewerName;

    /**
     * 评价者头像
     */
    private String reviewerAvatar;

    /**
     * 被评价者id（跑腿人员）
     */
    private Long runnerId;

    /**
     * 被评价者昵称
     */
    private String runnerName;

    /**
     * 评分（1-5分）
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价标签
     */
    private String tags;

    /**
     * 创建时间
     */
    private Date createTime;
}
