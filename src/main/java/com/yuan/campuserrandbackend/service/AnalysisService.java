package com.yuan.campuserrandbackend.service;

import com.yuan.campuserrandbackend.model.vo.OverviewStatsVO;
import com.yuan.campuserrandbackend.model.vo.RunnerAnalysisVO;
import com.yuan.campuserrandbackend.model.vo.TaskAnalysisVO;
import com.yuan.campuserrandbackend.model.vo.UserAnalysisVO;

/**
 * 数据分析Service
 */
public interface AnalysisService {

    /**
     * 获取平台概览统计
     */
    OverviewStatsVO getOverviewStats();

    /**
     * 获取任务分析数据
     */
    TaskAnalysisVO getTaskAnalysis();

    /**
     * 获取用户活跃度分析
     */
    UserAnalysisVO getUserAnalysis();

    /**
     * 获取跑腿人员分析
     */
    RunnerAnalysisVO getRunnerAnalysis();
}
