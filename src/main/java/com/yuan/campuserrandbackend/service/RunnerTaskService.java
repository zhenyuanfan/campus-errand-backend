package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.model.dto.task.RunnerTaskQueryRequest;
import com.yuan.campuserrandbackend.model.vo.TaskVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 跑腿人员接单Service
 */
public interface RunnerTaskService {

    /**
     * 查询待接单任务列表（分页）
     *
     * @param runnerTaskQueryRequest 查询请求
     * @return 待接单任务VO分页
     */
    Page<TaskVO> listAvailableTasks(RunnerTaskQueryRequest runnerTaskQueryRequest);

    /**
     * 获取推荐任务列表（基于接单员历史偏好排序）
     *
     * @param runnerTaskQueryRequest 查询请求
     * @param runnerId              接单员id
     * @return 推荐排序后的待接单任务VO分页
     */
    Page<TaskVO> listRecommendedTasks(RunnerTaskQueryRequest runnerTaskQueryRequest, Long runnerId);

    /**
     * 跑腿人员接单
     *
     * @param taskId  任务id
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean acceptTask(long taskId, HttpServletRequest request);
}
