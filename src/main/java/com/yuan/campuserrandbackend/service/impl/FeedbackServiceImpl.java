package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.FeedbackMapper;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackAddRequest;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackQueryRequest;
import com.yuan.campuserrandbackend.model.dto.feedback.FeedbackReplyRequest;
import com.yuan.campuserrandbackend.model.entity.Feedback;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.FeedbackStatusEnum;
import com.yuan.campuserrandbackend.model.enums.FeedbackTypeEnum;
import com.yuan.campuserrandbackend.model.vo.FeedbackVO;
import com.yuan.campuserrandbackend.service.FeedbackService;
import com.yuan.campuserrandbackend.service.MessageService;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户反馈Service实现
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback>
        implements FeedbackService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private TaskService taskService;

    @Resource
    private MessageService messageService;

    @Override
    public long addFeedback(FeedbackAddRequest feedbackAddRequest, HttpServletRequest request) {
        if (feedbackAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String type = feedbackAddRequest.getType();
        String title = feedbackAddRequest.getTitle();
        String content = feedbackAddRequest.getContent();

        // 参数校验
        if (StrUtil.isBlank(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "反馈类型不能为空");
        }
        FeedbackTypeEnum feedbackTypeEnum = FeedbackTypeEnum.getEnumByValue(type);
        if (feedbackTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "反馈类型不合法");
        }
        if (StrUtil.isBlank(title)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "反馈标题不能为空");
        }
        if (title.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "反馈标题过长，最多100字");
        }
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "反馈内容不能为空");
        }
        if (content.length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "反馈内容过长，最多2000字");
        }

        // 校验关联任务是否存在
        Long taskId = feedbackAddRequest.getTaskId();
        if (taskId != null && taskId > 0) {
            Task task = taskService.getById(taskId);
            if (task == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关联的任务不存在");
            }
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 创建反馈
        Feedback feedback = new Feedback();
        feedback.setUserId(loginUser.getId());
        feedback.setType(type);
        feedback.setTitle(title);
        feedback.setContent(content);
        feedback.setContactInfo(feedbackAddRequest.getContactInfo());
        feedback.setTaskId(taskId);
        feedback.setStatus(FeedbackStatusEnum.PENDING.getValue());

        boolean result = this.save(feedback);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "提交反馈失败，数据库错误");
        }

        return feedback.getId();
    }

    @Override
    public FeedbackVO getFeedbackDetail(long feedbackId, HttpServletRequest request) {
        if (feedbackId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Feedback feedback = this.getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "反馈不存在");
        }

        // 权限校验：本人或管理员可查看
        User loginUser = userService.getLoginUser(request);
        if (!feedback.getUserId().equals(loginUser.getId()) && !"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看该反馈");
        }

        return getFeedbackVO(feedback);
    }

    @Override
    public Page<FeedbackVO> listMyFeedbacks(FeedbackQueryRequest feedbackQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long current = feedbackQueryRequest.getCurrent();
        long size = feedbackQueryRequest.getPageSize();

        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());

        // 筛选条件
        buildQueryWrapper(queryWrapper, feedbackQueryRequest);
        queryWrapper.orderByDesc("createTime");

        Page<Feedback> feedbackPage = this.page(new Page<>(current, size), queryWrapper);
        return convertToVOPage(feedbackPage, current, size);
    }

    @Override
    public Page<FeedbackVO> listAllFeedbacks(FeedbackQueryRequest feedbackQueryRequest, HttpServletRequest request) {
        // 权限校验：仅管理员
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅管理员可查看所有反馈");
        }

        long current = feedbackQueryRequest.getCurrent();
        long size = feedbackQueryRequest.getPageSize();

        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();

        // 筛选条件
        buildQueryWrapper(queryWrapper, feedbackQueryRequest);
        queryWrapper.orderByDesc("createTime");

        Page<Feedback> feedbackPage = this.page(new Page<>(current, size), queryWrapper);
        return convertToVOPage(feedbackPage, current, size);
    }

    @Override
    public boolean replyFeedback(FeedbackReplyRequest feedbackReplyRequest, HttpServletRequest request) {
        if (feedbackReplyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long feedbackId = feedbackReplyRequest.getFeedbackId();
        String status = feedbackReplyRequest.getStatus();
        String adminReply = feedbackReplyRequest.getAdminReply();

        // 参数校验
        if (feedbackId == null || feedbackId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "反馈id不合法");
        }
        if (StrUtil.isBlank(status)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理状态不能为空");
        }
        FeedbackStatusEnum feedbackStatusEnum = FeedbackStatusEnum.getEnumByValue(status);
        if (feedbackStatusEnum == null || FeedbackStatusEnum.PENDING.equals(feedbackStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理状态不合法");
        }
        if (StrUtil.isNotBlank(adminReply) && adminReply.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复内容过长，最多1000字");
        }

        // 权限校验：仅管理员
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅管理员可处理反馈");
        }

        // 查询反馈
        Feedback feedback = this.getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "反馈不存在");
        }

        // 更新反馈
        Feedback updateFeedback = new Feedback();
        updateFeedback.setId(feedbackId);
        updateFeedback.setStatus(status);
        updateFeedback.setAdminId(loginUser.getId());
        updateFeedback.setAdminReply(adminReply);
        updateFeedback.setReplyTime(new Date());

        boolean result = this.updateById(updateFeedback);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理反馈失败，数据库错误");
        }

        // 通知用户
        String statusText = feedbackStatusEnum.getText();
        messageService.sendToUser(feedback.getUserId(),
                "您的反馈已处理",
                "您提交的反馈「" + feedback.getTitle() + "」已被处理，状态：" + statusText
                        + (StrUtil.isNotBlank(adminReply) ? "。回复：" + adminReply : ""),
                "FEEDBACK_REPLY");

        return true;
    }

    /**
     * 获取反馈VO
     */
    private FeedbackVO getFeedbackVO(Feedback feedback) {
        if (feedback == null) {
            return null;
        }
        FeedbackVO feedbackVO = new FeedbackVO();
        BeanUtils.copyProperties(feedback, feedbackVO);

        // 设置类型文本
        FeedbackTypeEnum feedbackTypeEnum = FeedbackTypeEnum.getEnumByValue(feedback.getType());
        if (feedbackTypeEnum != null) {
            feedbackVO.setTypeText(feedbackTypeEnum.getText());
        }

        // 设置状态文本
        FeedbackStatusEnum feedbackStatusEnum = FeedbackStatusEnum.getEnumByValue(feedback.getStatus());
        if (feedbackStatusEnum != null) {
            feedbackVO.setStatusText(feedbackStatusEnum.getText());
        }

        // 设置提交用户信息
        Long userId = feedback.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            if (user != null) {
                feedbackVO.setUserName(user.getUserName());
                feedbackVO.setUserAvatar(user.getUserAvatar());
            }
        }

        // 设置处理人信息
        Long adminId = feedback.getAdminId();
        if (adminId != null && adminId > 0) {
            User admin = userService.getById(adminId);
            if (admin != null) {
                feedbackVO.setAdminName(admin.getUserName());
            }
        }

        // 设置关联任务标题
        Long taskId = feedback.getTaskId();
        if (taskId != null && taskId > 0) {
            Task task = taskService.getById(taskId);
            if (task != null) {
                feedbackVO.setTaskTitle(task.getTitle());
            }
        }

        return feedbackVO;
    }

    /**
     * 构建查询条件
     */
    private void buildQueryWrapper(QueryWrapper<Feedback> queryWrapper, FeedbackQueryRequest feedbackQueryRequest) {
        String type = feedbackQueryRequest.getType();
        String status = feedbackQueryRequest.getStatus();
        String keyword = feedbackQueryRequest.getKeyword();

        if (StrUtil.isNotBlank(type)) {
            queryWrapper.eq("type", type);
        }
        if (StrUtil.isNotBlank(status)) {
            queryWrapper.eq("status", status);
        }
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("content", keyword));
        }
    }

    /**
     * 转换分页数据为 VO 分页
     */
    private Page<FeedbackVO> convertToVOPage(Page<Feedback> feedbackPage, long current, long size) {
        Page<FeedbackVO> feedbackVOPage = new Page<>(current, size, feedbackPage.getTotal());
        List<FeedbackVO> feedbackVOList = feedbackPage.getRecords().stream()
                .map(this::getFeedbackVO)
                .collect(Collectors.toList());
        feedbackVOPage.setRecords(feedbackVOList);
        return feedbackVOPage;
    }
}
