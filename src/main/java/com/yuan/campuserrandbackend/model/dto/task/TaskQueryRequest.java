package com.yuan.campuserrandbackend.model.dto.task;

import com.yuan.campuserrandbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 查询任务请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TaskQueryRequest extends PageRequest implements Serializable {

    /**
     * 任务id
     */
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
     * 任务类型
     */
    private String taskType;

    /**
     * 任务标题（模糊搜索）
     */
    private String title;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 起始地点（模糊搜索）
     */
    private String startLocation;

    /**
     * 目的地（模糊搜索）
     */
    private String endLocation;

    /**
     * 最低报酬
     */
    private BigDecimal minReward;

    /**
     * 最高报酬
     */
    private BigDecimal maxReward;
}
