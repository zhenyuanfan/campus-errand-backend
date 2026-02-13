package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 任务分析数据VO
 */
@Data
public class TaskAnalysisVO implements Serializable {

    /**
     * 任务类型分布（饼图）
     */
    private Map<String, Integer> typeDistribution;

    /**
     * 任务状态分布（饼图）
     */
    private Map<String, Integer> statusDistribution;

    /**
     * 总完成率（百分比）
     */
    private Double completionRate;

    /**
     * 近30天每日任务量（折线图，date + count）
     */
    private List<Map<String, Object>> dailyTaskCounts;

    /**
     * 近30天每日完成量（折线图，date + count）
     */
    private List<Map<String, Object>> dailyCompletedCounts;

    /**
     * 各类型平均报酬（柱状图）
     */
    private Map<String, Double> avgRewardByType;
}
