package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.ReviewReplyMapper;
import com.yuan.campuserrandbackend.model.dto.review.ReviewReplyAddRequest;
import com.yuan.campuserrandbackend.model.entity.ReviewReply;
import com.yuan.campuserrandbackend.model.entity.TaskReview;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.vo.ReviewReplyVO;
import com.yuan.campuserrandbackend.service.ReviewReplyService;
import com.yuan.campuserrandbackend.service.TaskReviewService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价回复Service实现
 */
@Service
public class ReviewReplyServiceImpl extends ServiceImpl<ReviewReplyMapper, ReviewReply>
        implements ReviewReplyService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private TaskReviewService taskReviewService;

    @Override
    public long addReply(ReviewReplyAddRequest reviewReplyAddRequest, HttpServletRequest request) {
        if (reviewReplyAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long reviewId = reviewReplyAddRequest.getReviewId();
        String content = reviewReplyAddRequest.getContent();

        // 参数校验
        if (reviewId == null || reviewId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价id不合法");
        }
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复内容不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复内容过长，最多500字");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 查询评价
        TaskReview taskReview = taskReviewService.getById(reviewId);
        if (taskReview == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }

        // 权限校验：只有被评价的跑腿人员可以回复
        if (!loginUser.getId().equals(taskReview.getRunnerId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有被评价的跑腿人员可以回复");
        }

        // 检查是否已经回复过
        QueryWrapper<ReviewReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reviewId", reviewId);
        queryWrapper.eq("replierId", loginUser.getId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该评价已回复过");
        }

        // 创建回复
        ReviewReply reviewReply = new ReviewReply();
        reviewReply.setReviewId(reviewId);
        reviewReply.setReplierId(loginUser.getId());
        reviewReply.setContent(content);

        boolean result = this.save(reviewReply);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "回复失败，数据库错误");
        }

        return reviewReply.getId();
    }

    @Override
    public List<ReviewReplyVO> getReplyList(long reviewId) {
        if (reviewId <= 0) {
            return Collections.emptyList();
        }
        QueryWrapper<ReviewReply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reviewId", reviewId);
        queryWrapper.orderByAsc("createTime");
        List<ReviewReply> replyList = this.list(queryWrapper);
        return replyList.stream()
                .map(this::getReplyVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取回复VO
     */
    private ReviewReplyVO getReplyVO(ReviewReply reviewReply) {
        if (reviewReply == null) {
            return null;
        }
        ReviewReplyVO reviewReplyVO = new ReviewReplyVO();
        BeanUtils.copyProperties(reviewReply, reviewReplyVO);

        // 设置回复者信息
        Long replierId = reviewReply.getReplierId();
        if (replierId != null && replierId > 0) {
            User replier = userService.getById(replierId);
            if (replier != null) {
                reviewReplyVO.setReplierName(replier.getUserName());
                reviewReplyVO.setReplierAvatar(replier.getUserAvatar());
            }
        }

        return reviewReplyVO;
    }
}
