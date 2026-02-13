package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.TaskReviewMapper;
import com.yuan.campuserrandbackend.model.dto.review.ReviewQueryRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskReviewAddRequest;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.TaskReview;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.model.vo.ReviewReplyVO;
import com.yuan.campuserrandbackend.model.vo.ReviewStatsVO;
import com.yuan.campuserrandbackend.model.vo.RunnerVO;
import com.yuan.campuserrandbackend.model.vo.TaskReviewVO;
import com.yuan.campuserrandbackend.service.ReviewReplyService;
import com.yuan.campuserrandbackend.service.TaskReviewService;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 86182
 * @description 针对表【task_review(任务评价)】的数据库操作Service实现
 */
@Service
public class TaskReviewServiceImpl extends ServiceImpl<TaskReviewMapper, TaskReview>
        implements TaskReviewService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private TaskService taskService;

    @Resource
    private TaskReviewMapper taskReviewMapper;

    @Resource
    @Lazy
    private ReviewReplyService reviewReplyService;

    @Override
    public long addTaskReview(TaskReviewAddRequest taskReviewAddRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        Long taskId = taskReviewAddRequest.getTaskId();
        Integer score = taskReviewAddRequest.getScore();
        String content = taskReviewAddRequest.getContent();

        // 参数校验
        if (taskId == null || taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评分必须在1-5之间");
        }
        if (StrUtil.isNotBlank(content) && content.length() > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评价内容过长，最多500字");
        }

        // 查询任务
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 只有任务发布者可以评价
        if (!task.getPublisherId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有任务发布者可以评价");
        }

        // 只有已完成的任务可以评价
        if (!TaskStatusEnum.COMPLETED.getValue().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有已完成的任务可以评价");
        }

        // 检查是否已经评价过
        QueryWrapper<TaskReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("taskId", taskId);
        queryWrapper.eq("reviewerId", loginUser.getId());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该任务已评价过");
        }

        // 创建评价
        TaskReview taskReview = new TaskReview();
        taskReview.setTaskId(taskId);
        taskReview.setReviewerId(loginUser.getId());
        taskReview.setRunnerId(task.getRunnerId());
        taskReview.setScore(score);
        taskReview.setContent(content);
        taskReview.setTags(taskReviewAddRequest.getTags());

        boolean result = this.save(taskReview);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评价失败，数据库错误");
        }

        // 更新跑腿人员信誉评分
        updateRunnerCreditScore(task.getRunnerId());

        return taskReview.getId();
    }

    @Override
    public TaskReviewVO getTaskReviewVO(TaskReview taskReview) {
        if (taskReview == null) {
            return null;
        }
        TaskReviewVO taskReviewVO = new TaskReviewVO();
        BeanUtils.copyProperties(taskReview, taskReviewVO);

        // 获取任务信息
        Task task = taskService.getById(taskReview.getTaskId());
        if (task != null) {
            taskReviewVO.setTaskTitle(task.getTitle());
        }

        // 获取评价者信息
        User reviewer = userService.getById(taskReview.getReviewerId());
        if (reviewer != null) {
            taskReviewVO.setReviewerName(reviewer.getUserName());
            taskReviewVO.setReviewerAvatar(reviewer.getUserAvatar());
        }

        // 获取被评价者信息
        User runner = userService.getById(taskReview.getRunnerId());
        if (runner != null) {
            taskReviewVO.setRunnerName(runner.getUserName());
        }

        // 获取回复信息
        List<ReviewReplyVO> replyList = reviewReplyService.getReplyList(taskReview.getId());
        if (replyList != null && !replyList.isEmpty()) {
            taskReviewVO.setReply(replyList.get(0));
        }

        return taskReviewVO;
    }

    @Override
    public List<TaskReviewVO> getTaskReviewList(Long taskId) {
        if (taskId == null || taskId <= 0) {
            return Collections.emptyList();
        }
        QueryWrapper<TaskReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("taskId", taskId);
        queryWrapper.orderByDesc("createTime");
        List<TaskReview> taskReviewList = this.list(queryWrapper);
        return taskReviewList.stream()
                .map(this::getTaskReviewVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TaskReviewVO> getRunnerReviewPage(Long runnerId, long current, long size) {
        QueryWrapper<TaskReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("runnerId", runnerId);
        queryWrapper.orderByDesc("createTime");
        Page<TaskReview> taskReviewPage = this.page(new Page<>(current, size), queryWrapper);

        Page<TaskReviewVO> taskReviewVOPage = new Page<>(current, size, taskReviewPage.getTotal());
        List<TaskReviewVO> taskReviewVOList = taskReviewPage.getRecords().stream()
                .map(this::getTaskReviewVO)
                .collect(Collectors.toList());
        taskReviewVOPage.setRecords(taskReviewVOList);
        return taskReviewVOPage;
    }

    @Override
    public RunnerVO getRunnerVO(Long runnerId) {
        if (runnerId == null || runnerId <= 0) {
            return null;
        }
        User runner = userService.getById(runnerId);
        if (runner == null) {
            return null;
        }

        RunnerVO runnerVO = new RunnerVO();
        runnerVO.setId(runner.getId());
        runnerVO.setUserName(runner.getUserName());
        runnerVO.setUserAvatar(runner.getUserAvatar());
        runnerVO.setContactInfo(runner.getContactInfo());
        runnerVO.setCreditScore(runner.getCreditScore());
        runnerVO.setOrderCount(runner.getOrderCount());

        // 获取平均评分和评价数量
        Double avgScore = taskReviewMapper.getAvgScoreByRunnerId(runnerId);
        Integer reviewCount = taskReviewMapper.getReviewCountByRunnerId(runnerId);
        runnerVO.setAvgScore(avgScore != null ? avgScore : 0.0);
        runnerVO.setReviewCount(reviewCount != null ? reviewCount : 0);

        return runnerVO;
    }

    @Override
    public void updateRunnerCreditScore(Long runnerId) {
        if (runnerId == null || runnerId <= 0) {
            return;
        }

        // 获取跑腿人员的平均评分
        Double avgScore = taskReviewMapper.getAvgScoreByRunnerId(runnerId);
        if (avgScore == null) {
            return;
        }

        // 信誉评分计算规则：
        // 基础分100分，根据平均评分调整
        // 平均分5分 -> 信誉分100
        // 平均分4分 -> 信誉分90
        // 平均分3分 -> 信誉分75
        // 平均分2分 -> 信誉分50
        // 平均分1分 -> 信誉分20
        double creditScore;
        if (avgScore >= 4.5) {
            creditScore = 95 + (avgScore - 4.5) * 10;
        } else if (avgScore >= 4.0) {
            creditScore = 85 + (avgScore - 4.0) * 20;
        } else if (avgScore >= 3.0) {
            creditScore = 70 + (avgScore - 3.0) * 15;
        } else if (avgScore >= 2.0) {
            creditScore = 45 + (avgScore - 2.0) * 25;
        } else {
            creditScore = 20 + (avgScore - 1.0) * 25;
        }

        // 限制在0-100之间
        creditScore = Math.max(0, Math.min(100, creditScore));

        // 更新信誉评分
        User user = new User();
        user.setId(runnerId);
        user.setCreditScore(creditScore);
        userService.updateById(user);
    }

    @Override
    public Page<TaskReviewVO> listMyReviews(ReviewQueryRequest reviewQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long current = reviewQueryRequest.getCurrent();
        long size = reviewQueryRequest.getPageSize();

        QueryWrapper<TaskReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reviewerId", loginUser.getId());

        // 高级筛选
        buildReviewQueryWrapper(queryWrapper, reviewQueryRequest);
        queryWrapper.orderByDesc("createTime");

        Page<TaskReview> taskReviewPage = this.page(new Page<>(current, size), queryWrapper);
        return convertToVOPage(taskReviewPage, current, size);
    }

    @Override
    public Page<TaskReviewVO> listReceivedReviews(ReviewQueryRequest reviewQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long current = reviewQueryRequest.getCurrent();
        long size = reviewQueryRequest.getPageSize();

        QueryWrapper<TaskReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("runnerId", loginUser.getId());

        // 高级筛选
        buildReviewQueryWrapper(queryWrapper, reviewQueryRequest);
        queryWrapper.orderByDesc("createTime");

        Page<TaskReview> taskReviewPage = this.page(new Page<>(current, size), queryWrapper);
        return convertToVOPage(taskReviewPage, current, size);
    }

    @Override
    public ReviewStatsVO getReviewStats(long runnerId) {
        ReviewStatsVO statsVO = new ReviewStatsVO();

        // 总评价数
        Integer totalCount = taskReviewMapper.getReviewCountByRunnerId(runnerId);
        statsVO.setTotalCount(totalCount != null ? totalCount : 0);

        // 平均评分
        Double avgScore = taskReviewMapper.getAvgScoreByRunnerId(runnerId);
        statsVO.setAvgScore(avgScore != null ? Math.round(avgScore * 10) / 10.0 : 0.0);

        // 评分分布
        List<Map<String, Object>> distributionList = taskReviewMapper.getScoreDistribution(runnerId);
        Map<Integer, Integer> scoreDistribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            scoreDistribution.put(i, 0);
        }
        if (distributionList != null) {
            for (Map<String, Object> item : distributionList) {
                Integer score = ((Number) item.get("score")).intValue();
                Integer count = ((Number) item.get("count")).intValue();
                scoreDistribution.put(score, count);
            }
        }
        statsVO.setScoreDistribution(scoreDistribution);

        // 高频评价标签
        QueryWrapper<TaskReview> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("runnerId", runnerId);
        queryWrapper.isNotNull("tags");
        queryWrapper.ne("tags", "");
        queryWrapper.select("tags");
        List<TaskReview> reviewsWithTags = this.list(queryWrapper);

        Map<String, Integer> tagCountMap = new HashMap<>();
        for (TaskReview review : reviewsWithTags) {
            String tags = review.getTags();
            if (StrUtil.isNotBlank(tags)) {
                String[] tagArray = tags.split(",");
                for (String tag : tagArray) {
                    String trimTag = tag.trim();
                    if (!trimTag.isEmpty()) {
                        tagCountMap.put(trimTag, tagCountMap.getOrDefault(trimTag, 0) + 1);
                    }
                }
            }
        }

        List<String> topTags = tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        statsVO.setTopTags(topTags);

        return statsVO;
    }

    /**
     * 构建评价查询条件
     */
    private void buildReviewQueryWrapper(QueryWrapper<TaskReview> queryWrapper, ReviewQueryRequest reviewQueryRequest) {
        String keyword = reviewQueryRequest.getKeyword();
        Integer minScore = reviewQueryRequest.getMinScore();
        Integer maxScore = reviewQueryRequest.getMaxScore();
        Date startTime = reviewQueryRequest.getStartTime();
        Date endTime = reviewQueryRequest.getEndTime();

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.like("content", keyword);
        }
        if (minScore != null) {
            queryWrapper.ge("score", minScore);
        }
        if (maxScore != null) {
            queryWrapper.le("score", maxScore);
        }
        if (startTime != null) {
            queryWrapper.ge("createTime", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("createTime", endTime);
        }
    }

    /**
     * 转换分页数据为 VO 分页
     */
    private Page<TaskReviewVO> convertToVOPage(Page<TaskReview> taskReviewPage, long current, long size) {
        Page<TaskReviewVO> taskReviewVOPage = new Page<>(current, size, taskReviewPage.getTotal());
        List<TaskReviewVO> taskReviewVOList = taskReviewPage.getRecords().stream()
                .map(this::getTaskReviewVO)
                .collect(Collectors.toList());
        taskReviewVOPage.setRecords(taskReviewVOList);
        return taskReviewVOPage;
    }
}
