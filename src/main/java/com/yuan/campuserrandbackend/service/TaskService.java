package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.campuserrandbackend.model.dto.task.TaskAddRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskQueryRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskUpdateRequest;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.vo.TaskVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 86182
 * @description 针对表【task(跑腿任务)】的数据库操作Service
 */
public interface TaskService extends IService<Task> {

    /**
     * 发布任务
     *
     * @param taskAddRequest 任务发布请求
     * @param request        HTTP请求
     * @return 任务id
     */
    long addTask(TaskAddRequest taskAddRequest, HttpServletRequest request);

    /**
     * 校验任务信息
     *
     * @param task 任务
     * @param add  是否为新增
     */
    void validTask(Task task, boolean add);

    /**
     * 更新任务
     *
     * @param taskUpdateRequest 任务更新请求
     * @param request           HTTP请求
     * @return 是否成功
     */
    boolean updateTask(TaskUpdateRequest taskUpdateRequest, HttpServletRequest request);

    /**
     * 删除任务
     *
     * @param taskId  任务id
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean deleteTask(long taskId, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param taskQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<Task> getQueryWrapper(TaskQueryRequest taskQueryRequest);

    /**
     * 获取任务VO
     *
     * @param task 任务
     * @return 任务VO
     */
    TaskVO getTaskVO(Task task);

    /**
     * 分页获取任务VO
     *
     * @param taskPage 任务分页
     * @return 任务VO分页
     */
    Page<TaskVO> getTaskVOPage(Page<Task> taskPage);

    /**
     * 取消任务
     *
     * @param taskId  任务id
     * @param request HTTP请求
     * @return 是否成功
     */
    boolean cancelTask(long taskId, HttpServletRequest request);
}
