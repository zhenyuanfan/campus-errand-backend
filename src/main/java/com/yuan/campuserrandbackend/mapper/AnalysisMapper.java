package com.yuan.campuserrandbackend.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据分析统计Mapper
 */
public interface AnalysisMapper {

    // ==================== 平台概览 ====================

    @Select("SELECT COUNT(*) FROM user WHERE isDelete = 0")
    Integer countTotalUsers();

    @Select("SELECT COUNT(*) FROM user WHERE userRole = 'runner' AND isDelete = 0")
    Integer countTotalRunners();

    @Select("SELECT COUNT(*) FROM task WHERE isDelete = 0")
    Integer countTotalTasks();

    @Select("SELECT COUNT(*) FROM task WHERE status = 'completed' AND isDelete = 0")
    Integer countCompletedTasks();

    @Select("SELECT IFNULL(SUM(reward), 0) FROM task WHERE status = 'completed' AND isDelete = 0")
    Double sumTotalReward();

    @Select("SELECT COUNT(*) FROM task WHERE DATE(createTime) = CURDATE() AND isDelete = 0")
    Integer countTodayNewTasks();

    @Select("SELECT COUNT(*) FROM task WHERE status = 'completed' AND DATE(updateTime) = CURDATE() AND isDelete = 0")
    Integer countTodayCompletedTasks();

    @Select("SELECT COUNT(*) FROM user WHERE DATE(createTime) = CURDATE() AND isDelete = 0")
    Integer countTodayNewUsers();

    // ==================== 任务分析 ====================

    @Select("SELECT taskType AS name, COUNT(*) AS value FROM task WHERE isDelete = 0 GROUP BY taskType")
    List<Map<String, Object>> countByTaskType();

    @Select("SELECT status AS name, COUNT(*) AS value FROM task WHERE isDelete = 0 GROUP BY status")
    List<Map<String, Object>> countByTaskStatus();

    @Select("SELECT DATE(createTime) AS date, COUNT(*) AS count FROM task WHERE createTime >= #{startDate} AND isDelete = 0 GROUP BY DATE(createTime) ORDER BY date")
    List<Map<String, Object>> dailyTaskCounts(@Param("startDate") Date startDate);

    @Select("SELECT DATE(updateTime) AS date, COUNT(*) AS count FROM task WHERE status = 'completed' AND updateTime >= #{startDate} AND isDelete = 0 GROUP BY DATE(updateTime) ORDER BY date")
    List<Map<String, Object>> dailyCompletedCounts(@Param("startDate") Date startDate);

    @Select("SELECT taskType AS name, ROUND(AVG(reward), 2) AS value FROM task WHERE isDelete = 0 GROUP BY taskType")
    List<Map<String, Object>> avgRewardByType();

    // ==================== 用户分析 ====================

    @Select("SELECT DATE(createTime) AS date, COUNT(*) AS count FROM user WHERE createTime >= #{startDate} AND isDelete = 0 GROUP BY DATE(createTime) ORDER BY date")
    List<Map<String, Object>> dailyNewUsers(@Param("startDate") Date startDate);

    @Select("SELECT userRole AS name, COUNT(*) AS value FROM user WHERE isDelete = 0 GROUP BY userRole")
    List<Map<String, Object>> countByUserRole();

    @Select("SELECT DATE(createTime) AS date, COUNT(DISTINCT userId) AS count FROM operation_log WHERE createTime >= #{startDate} AND isDelete = 0 GROUP BY DATE(createTime) ORDER BY date")
    List<Map<String, Object>> dailyActiveUsers(@Param("startDate") Date startDate);

    // ==================== 跑腿人员分析 ====================

    @Select("SELECT u.userName AS runnerName, u.orderCount AS orderCount FROM user u WHERE u.userRole = 'runner' AND u.isDelete = 0 ORDER BY u.orderCount DESC LIMIT #{limit}")
    List<Map<String, Object>> topRunnersByOrders(@Param("limit") int limit);

    @Select("SELECT u.userName AS runnerName, ROUND(IFNULL(SUM(t.reward), 0), 2) AS totalIncome FROM user u LEFT JOIN task t ON u.id = t.runnerId AND t.status = 'completed' AND t.isDelete = 0 WHERE u.userRole = 'runner' AND u.isDelete = 0 GROUP BY u.id, u.userName ORDER BY totalIncome DESC LIMIT #{limit}")
    List<Map<String, Object>> topRunnersByIncome(@Param("limit") int limit);

    @Select("SELECT u.userName AS runnerName, ROUND(IFNULL(AVG(r.score), 0), 1) AS avgScore FROM user u LEFT JOIN task_review r ON u.id = r.runnerId AND r.isDelete = 0 WHERE u.userRole = 'runner' AND u.isDelete = 0 GROUP BY u.id, u.userName HAVING COUNT(r.id) > 0 ORDER BY avgScore DESC LIMIT #{limit}")
    List<Map<String, Object>> topRunnersByScore(@Param("limit") int limit);

    @Select("SELECT " +
            "SUM(CASE WHEN creditScore >= 90 THEN 1 ELSE 0 END) AS '90-100', " +
            "SUM(CASE WHEN creditScore >= 80 AND creditScore < 90 THEN 1 ELSE 0 END) AS '80-89', " +
            "SUM(CASE WHEN creditScore >= 70 AND creditScore < 80 THEN 1 ELSE 0 END) AS '70-79', " +
            "SUM(CASE WHEN creditScore >= 60 AND creditScore < 70 THEN 1 ELSE 0 END) AS '60-69', " +
            "SUM(CASE WHEN creditScore < 60 THEN 1 ELSE 0 END) AS '60以下' " +
            "FROM user WHERE userRole = 'runner' AND isDelete = 0")
    Map<String, Object> creditScoreDistribution();
}
