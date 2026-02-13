package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 跑腿人员分析VO
 */
@Data
public class RunnerAnalysisVO implements Serializable {

    /**
     * 接单榜 TOP10（柱状图，runnerName + orderCount）
     */
    private List<Map<String, Object>> topRunnersByOrders;

    /**
     * 收入榜 TOP10（柱状图，runnerName + totalIncome）
     */
    private List<Map<String, Object>> topRunnersByIncome;

    /**
     * 评分榜 TOP10（柱状图，runnerName + avgScore）
     */
    private List<Map<String, Object>> topRunnersByScore;

    /**
     * 信誉评分区间分布（柱状图）
     */
    private Map<String, Integer> creditScoreDistribution;
}
