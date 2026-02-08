package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.campuserrandbackend.model.dto.task.TaskReviewAddRequest;
import com.yuan.campuserrandbackend.model.entity.TaskReview;
import com.yuan.campuserrandbackend.model.vo.RunnerVO;
import com.yuan.campuserrandbackend.model.vo.TaskReviewVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 86182
 * @description 针对表【task_review(任务评价)】的数据库操作Service
 */
public interface TaskReviewService extends IService<TaskReview> {

    /**
     * 添加任务评价
     *
     * @param taskReviewAddRequest 评价请求
     * @param request              HTTP请求
     * @return 评价id
     */
    long addTaskReview(TaskReviewAddRequest taskReviewAddRequest, HttpServletRequest request);

    /**
     * 获取任务评价VO
     *
     * @param taskReview 任务评价
     * @return 评价VO
     */
    TaskReviewVO getTaskReviewVO(TaskReview taskReview);

    /**
     * 获取任务的评价列表
     *
     * @param taskId 任务id
     * @return 评价列表
     */
    List<TaskReviewVO> getTaskReviewList(Long taskId);

    /**
     * 获取跑腿人员的评价列表（分页）
     *
     * @param runnerId 跑腿人员id
     * @param current  当前页
     * @param size     每页数量
     * @return 评价分页
     */
    Page<TaskReviewVO> getRunnerReviewPage(Long runnerId, long current, long size);

    /**
     * 获取跑腿人员信息
     *
     * @param runnerId 跑腿人员id
     * @return 跑腿人员信息
     */
    RunnerVO getRunnerVO(Long runnerId);

    /**
     * 更新跑腿人员信誉评分
     *
     * @param runnerId 跑腿人员id
     */
    void updateRunnerCreditScore(Long runnerId);
}
