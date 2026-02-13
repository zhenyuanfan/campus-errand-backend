package com.yuan.campuserrandbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuan.campuserrandbackend.model.dto.review.ReviewReplyAddRequest;
import com.yuan.campuserrandbackend.model.entity.ReviewReply;
import com.yuan.campuserrandbackend.model.vo.ReviewReplyVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评价回复Service
 */
public interface ReviewReplyService extends IService<ReviewReply> {

    /**
     * 回复评价（仅被评价的跑腿人员可回复，每条评价仅限一次回复）
     *
     * @param reviewReplyAddRequest 回复请求
     * @param request               HTTP请求
     * @return 回复id
     */
    long addReply(ReviewReplyAddRequest reviewReplyAddRequest, HttpServletRequest request);

    /**
     * 获取评价的回复列表
     *
     * @param reviewId 评价id
     * @return 回复VO列表
     */
    List<ReviewReplyVO> getReplyList(long reviewId);
}
