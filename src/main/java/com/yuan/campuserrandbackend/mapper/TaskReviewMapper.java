package com.yuan.campuserrandbackend.mapper;

import com.yuan.campuserrandbackend.model.entity.TaskReview;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author 86182
 * @description 针对表【task_review(任务评价)】的数据库操作Mapper
 * @Entity com.yuan.campuserrandbackend.model.entity.TaskReview
 */
public interface TaskReviewMapper extends BaseMapper<TaskReview> {

    /**
     * 获取跑腿人员的平均评分
     */
    @Select("SELECT AVG(score) FROM task_review WHERE runnerId = #{runnerId} AND isDelete = 0")
    Double getAvgScoreByRunnerId(Long runnerId);

    /**
     * 获取跑腿人员的评价数量
     */
    @Select("SELECT COUNT(*) FROM task_review WHERE runnerId = #{runnerId} AND isDelete = 0")
    Integer getReviewCountByRunnerId(Long runnerId);

    /**
     * 获取跑腿人员的评分分布
     */
    @Select("SELECT score, COUNT(*) as count FROM task_review WHERE runnerId = #{runnerId} AND isDelete = 0 GROUP BY score ORDER BY score")
    List<Map<String, Object>> getScoreDistribution(Long runnerId);
}
