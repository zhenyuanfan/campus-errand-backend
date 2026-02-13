package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 评价统计视图对象
 */
@Data
public class ReviewStatsVO implements Serializable {

    /**
     * 总评价数
     */
    private Integer totalCount;

    /**
     * 平均评分
     */
    private Double avgScore;

    /**
     * 评分分布（key: 1-5分, value: 数量）
     */
    private Map<Integer, Integer> scoreDistribution;

    /**
     * 高频评价标签（按出现次数降序）
     */
    private List<String> topTags;
}
