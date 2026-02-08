package com.yuan.campuserrandbackend.model.dto.task;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加任务评价请求
 */
@Data
public class TaskReviewAddRequest implements Serializable {

    /**
     * 任务id
     */
    private Long taskId;

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
}
