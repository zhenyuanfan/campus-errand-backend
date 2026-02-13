package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 平台概览统计VO
 */
@Data
public class OverviewStatsVO implements Serializable {

    /**
     * 总用户数
     */
    private Integer totalUsers;

    /**
     * 总跑腿人员数
     */
    private Integer totalRunners;

    /**
     * 总任务数
     */
    private Integer totalTasks;

    /**
     * 已完成任务数
     */
    private Integer completedTasks;

    /**
     * 总报酬金额
     */
    private BigDecimal totalReward;

    /**
     * 今日新增任务
     */
    private Integer todayNewTasks;

    /**
     * 今日完成任务
     */
    private Integer todayCompletedTasks;

    /**
     * 今日新增用户
     */
    private Integer todayNewUsers;
}
