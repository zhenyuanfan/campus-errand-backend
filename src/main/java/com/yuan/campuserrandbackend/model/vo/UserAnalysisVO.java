package com.yuan.campuserrandbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 用户活跃度分析VO
 */
@Data
public class UserAnalysisVO implements Serializable {

    /**
     * 近30天每日新增用户（折线图，date + count）
     */
    private List<Map<String, Object>> dailyNewUsers;

    /**
     * 用户角色分布（饼图）
     */
    private Map<String, Integer> roleDistribution;

    /**
     * 近30天每日活跃用户数（折线图，date + count，基于操作日志）
     */
    private List<Map<String, Object>> dailyActiveUsers;
}
