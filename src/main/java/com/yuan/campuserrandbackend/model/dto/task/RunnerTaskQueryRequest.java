package com.yuan.campuserrandbackend.model.dto.task;

import com.yuan.campuserrandbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 跑腿人员查看待接单任务列表请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RunnerTaskQueryRequest extends PageRequest implements Serializable {

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务标题（模糊搜索）
     */
    private String title;

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
