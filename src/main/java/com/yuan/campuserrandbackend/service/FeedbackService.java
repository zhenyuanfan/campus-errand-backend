package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackAddRequest;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackQueryRequest;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackReplyRequest;
import com.yuan.campuserrandbackend.model.entity.Feedback;
import com.yuan.campuserrandbackend.model.vo.FeedbackVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户反馈Service
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 提交反馈
     *
     * @param feedbackAddRequest 反馈请求
     * @param request            HTTP请求
     * @return 反馈id
     */
    long addFeedback(FeedbackAddRequest feedbackAddRequest, HttpServletRequest request);

    /**
     * 获取反馈详情
     *
     * @param feedbackId 反馈id
     * @param request    HTTP请求
     * @return 反馈VO
     */
    FeedbackVO getFeedbackDetail(long feedbackId, HttpServletRequest request);

    /**
     * 查看我的反馈列表
     *
     * @param feedbackQueryRequest 查询请求
     * @param request              HTTP请求
     * @return 反馈VO分页
     */
    Page<FeedbackVO> listMyFeedbacks(FeedbackQueryRequest feedbackQueryRequest, HttpServletRequest request);

    /**
     * 管理员查看所有反馈
     *
     * @param feedbackQueryRequest 查询请求
     * @param request              HTTP请求
     * @return 反馈VO分页
     */
    Page<FeedbackVO> listAllFeedbacks(FeedbackQueryRequest feedbackQueryRequest, HttpServletRequest request);

    /**
     * 管理员回复/处理反馈
     *
     * @param feedbackReplyRequest 回复请求
     * @param request              HTTP请求
     * @return 是否成功
     */
    boolean replyFeedback(FeedbackReplyRequest feedbackReplyRequest, HttpServletRequest request);
}
