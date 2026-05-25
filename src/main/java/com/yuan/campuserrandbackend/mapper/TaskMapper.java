package com.yuan.campuserrandbackend.mapper;

import com.yuan.campuserrandbackend.model.entity.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author 86182
 * @description 针对表【task(跑腿任务)】的数据库操作Mapper
 * @Entity com.yuan.campuserrandbackend.model.entity.Task
 */
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 统计某个接单员历史已完成任务的各类型数量（推荐算法用）
     *
     * @param runnerId 接单员id
     * @return 每条包含 taskType 和 count
     */
    @Select("SELECT taskType, COUNT(*) AS count FROM task " +
            "WHERE runnerId = #{runnerId} AND status IN ('completed', 'in_progress') " +
            "AND isDelete = 0 GROUP BY taskType ORDER BY count DESC")
    List<Map<String, Object>> countRunnerTaskTypes(@Param("runnerId") Long runnerId);
}
