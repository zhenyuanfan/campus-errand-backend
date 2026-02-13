package com.yuan.campuserrandbackend.service.impl;

import com.yuan.campuserrandbackend.mapper.AnalysisMapper;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskTypeEnum;
import com.yuan.campuserrandbackend.model.vo.*;
import com.yuan.campuserrandbackend.service.AnalysisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 数据分析Service实现
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Resource
    private AnalysisMapper analysisMapper;

    @Override
    public OverviewStatsVO getOverviewStats() {
        OverviewStatsVO statsVO = new OverviewStatsVO();
        statsVO.setTotalUsers(analysisMapper.countTotalUsers());
        statsVO.setTotalRunners(analysisMapper.countTotalRunners());
        statsVO.setTotalTasks(analysisMapper.countTotalTasks());
        statsVO.setCompletedTasks(analysisMapper.countCompletedTasks());

        Double totalReward = analysisMapper.sumTotalReward();
        statsVO.setTotalReward(
                BigDecimal.valueOf(totalReward != null ? totalReward : 0).setScale(2, RoundingMode.HALF_UP));

        statsVO.setTodayNewTasks(analysisMapper.countTodayNewTasks());
        statsVO.setTodayCompletedTasks(analysisMapper.countTodayCompletedTasks());
        statsVO.setTodayNewUsers(analysisMapper.countTodayNewUsers());
        return statsVO;
    }

    @Override
    public TaskAnalysisVO getTaskAnalysis() {
        TaskAnalysisVO analysisVO = new TaskAnalysisVO();

        // 任务类型分布（转换为中文名称）
        List<Map<String, Object>> typeList = analysisMapper.countByTaskType();
        Map<String, Integer> typeDistribution = new LinkedHashMap<>();
        for (Map<String, Object> item : typeList) {
            String name = (String) item.get("name");
            Integer value = ((Number) item.get("value")).intValue();
            TaskTypeEnum typeEnum = TaskTypeEnum.getEnumByValue(name);
            typeDistribution.put(typeEnum != null ? typeEnum.getText() : name, value);
        }
        analysisVO.setTypeDistribution(typeDistribution);

        // 任务状态分布（转换为中文名称）
        List<Map<String, Object>> statusList = analysisMapper.countByTaskStatus();
        Map<String, Integer> statusDistribution = new LinkedHashMap<>();
        for (Map<String, Object> item : statusList) {
            String name = (String) item.get("name");
            Integer value = ((Number) item.get("value")).intValue();
            TaskStatusEnum statusEnum = TaskStatusEnum.getEnumByValue(name);
            statusDistribution.put(statusEnum != null ? statusEnum.getText() : name, value);
        }
        analysisVO.setStatusDistribution(statusDistribution);

        // 完成率
        Integer totalTasks = analysisMapper.countTotalTasks();
        Integer completedTasks = analysisMapper.countCompletedTasks();
        if (totalTasks != null && totalTasks > 0) {
            analysisVO.setCompletionRate(Math.round(completedTasks * 1000.0 / totalTasks) / 10.0);
        } else {
            analysisVO.setCompletionRate(0.0);
        }

        // 近30天趋势
        Date startDate = getDateBefore(30);
        analysisVO.setDailyTaskCounts(analysisMapper.dailyTaskCounts(startDate));
        analysisVO.setDailyCompletedCounts(analysisMapper.dailyCompletedCounts(startDate));

        // 各类型平均报酬（转换为中文名称）
        List<Map<String, Object>> avgRewardList = analysisMapper.avgRewardByType();
        Map<String, Double> avgRewardByType = new LinkedHashMap<>();
        for (Map<String, Object> item : avgRewardList) {
            String name = (String) item.get("name");
            Double value = ((Number) item.get("value")).doubleValue();
            TaskTypeEnum typeEnum = TaskTypeEnum.getEnumByValue(name);
            avgRewardByType.put(typeEnum != null ? typeEnum.getText() : name, value);
        }
        analysisVO.setAvgRewardByType(avgRewardByType);

        return analysisVO;
    }

    @Override
    public UserAnalysisVO getUserAnalysis() {
        UserAnalysisVO analysisVO = new UserAnalysisVO();
        Date startDate = getDateBefore(30);

        analysisVO.setDailyNewUsers(analysisMapper.dailyNewUsers(startDate));

        // 角色分布（转换为中文名称）
        List<Map<String, Object>> roleList = analysisMapper.countByUserRole();
        Map<String, Integer> roleDistribution = new LinkedHashMap<>();
        Map<String, String> roleNameMap = new HashMap<>();
        roleNameMap.put("user", "普通用户");
        roleNameMap.put("admin", "管理员");
        roleNameMap.put("publisher", "发布者");
        roleNameMap.put("runner", "跑腿人员");
        for (Map<String, Object> item : roleList) {
            String name = (String) item.get("name");
            Integer value = ((Number) item.get("value")).intValue();
            roleDistribution.put(roleNameMap.getOrDefault(name, name), value);
        }
        analysisVO.setRoleDistribution(roleDistribution);

        analysisVO.setDailyActiveUsers(analysisMapper.dailyActiveUsers(startDate));

        return analysisVO;
    }

    @Override
    public RunnerAnalysisVO getRunnerAnalysis() {
        RunnerAnalysisVO analysisVO = new RunnerAnalysisVO();

        analysisVO.setTopRunnersByOrders(analysisMapper.topRunnersByOrders(10));
        analysisVO.setTopRunnersByIncome(analysisMapper.topRunnersByIncome(10));
        analysisVO.setTopRunnersByScore(analysisMapper.topRunnersByScore(10));

        // 信誉评分区间分布
        Map<String, Object> creditScoreRaw = analysisMapper.creditScoreDistribution();
        Map<String, Integer> creditScoreDistribution = new LinkedHashMap<>();
        creditScoreDistribution.put("90-100", toInt(creditScoreRaw.get("90-100")));
        creditScoreDistribution.put("80-89", toInt(creditScoreRaw.get("80-89")));
        creditScoreDistribution.put("70-79", toInt(creditScoreRaw.get("70-79")));
        creditScoreDistribution.put("60-69", toInt(creditScoreRaw.get("60-69")));
        creditScoreDistribution.put("60以下", toInt(creditScoreRaw.get("60以下")));
        analysisVO.setCreditScoreDistribution(creditScoreDistribution);

        return analysisVO;
    }

    /**
     * 获取N天前的日期
     */
    private Date getDateBefore(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 安全转换为整数
     */
    private Integer toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        return ((Number) obj).intValue();
    }
}
