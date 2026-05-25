package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.TaskMapper;
import com.yuan.campuserrandbackend.model.dto.task.RunnerTaskQueryRequest;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskTypeEnum;
import com.yuan.campuserrandbackend.model.enums.UserRoleEnum;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.MessageService;
import com.yuan.campuserrandbackend.service.RunnerTaskService;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 跑腿人员接单Service实现
 */
@Service
public class RunnerTaskServiceImpl implements RunnerTaskService {

    @Resource
    private TaskService taskService;

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    @Resource
    private TaskMapper taskMapper;

    @Override
    public Page<TaskVO> listAvailableTasks(RunnerTaskQueryRequest runnerTaskQueryRequest) {
        if (runnerTaskQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = runnerTaskQueryRequest.getCurrent();
        long size = runnerTaskQueryRequest.getPageSize();

        // 构建查询条件
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        // 固定只查询待接单状态的任务
        queryWrapper.eq("status", TaskStatusEnum.PENDING.getValue());

        String taskType = runnerTaskQueryRequest.getTaskType();
        String title = runnerTaskQueryRequest.getTitle();
        String startLocation = runnerTaskQueryRequest.getStartLocation();
        String endLocation = runnerTaskQueryRequest.getEndLocation();
        BigDecimal minReward = runnerTaskQueryRequest.getMinReward();
        BigDecimal maxReward = runnerTaskQueryRequest.getMaxReward();

        // 任务类型筛选
        if (StrUtil.isNotBlank(taskType)) {
            // 校验任务类型合法性
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(taskType);
            if (taskTypeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务类型不合法");
            }
            queryWrapper.eq("taskType", taskType);
        }
        // 标题模糊搜索
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        // 地点模糊搜索
        queryWrapper.like(StrUtil.isNotBlank(startLocation), "startLocation", startLocation);
        queryWrapper.like(StrUtil.isNotBlank(endLocation), "endLocation", endLocation);
        // 报酬范围筛选
        queryWrapper.ge(minReward != null, "reward", minReward);
        queryWrapper.le(maxReward != null, "reward", maxReward);

        // 排序
        String sortField = runnerTaskQueryRequest.getSortField();
        String sortOrder = runnerTaskQueryRequest.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }

        // 分页查询
        Page<Task> taskPage = taskService.page(new Page<>(current, size), queryWrapper);
        return taskService.getTaskVOPage(taskPage);
    }

    @Override
    public Page<TaskVO> listRecommendedTasks(RunnerTaskQueryRequest runnerTaskQueryRequest, Long runnerId) {
        if (runnerTaskQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long current = runnerTaskQueryRequest.getCurrent();
        long size = runnerTaskQueryRequest.getPageSize();

        // 1. 查询接单员的历史任务类型偏好
        List<Map<String, Object>> typeStats = taskMapper.countRunnerTaskTypes(runnerId);

        // 2. 计算各类型的权重分数
        Map<String, Double> typeWeights = calculateTypeWeights(typeStats);

        // 3. 构建查询条件（与普通查询相同的筛选条件）
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", TaskStatusEnum.PENDING.getValue());

        String taskType = runnerTaskQueryRequest.getTaskType();
        String title = runnerTaskQueryRequest.getTitle();
        String startLocation = runnerTaskQueryRequest.getStartLocation();
        String endLocation = runnerTaskQueryRequest.getEndLocation();
        BigDecimal minReward = runnerTaskQueryRequest.getMinReward();
        BigDecimal maxReward = runnerTaskQueryRequest.getMaxReward();

        if (StrUtil.isNotBlank(taskType)) {
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(taskType);
            if (taskTypeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务类型不合法");
            }
            queryWrapper.eq("taskType", taskType);
        }
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(startLocation), "startLocation", startLocation);
        queryWrapper.like(StrUtil.isNotBlank(endLocation), "endLocation", endLocation);
        queryWrapper.ge(minReward != null, "reward", minReward);
        queryWrapper.le(maxReward != null, "reward", maxReward);

        // 4. 按偏好权重排序（核心推荐逻辑）
        if (!typeWeights.isEmpty()) {
            // 构建 CASE WHEN 排序表达式：偏好类型权重越高排越前
            StringBuilder caseWhen = new StringBuilder("CASE taskType ");
            for (Map.Entry<String, Double> entry : typeWeights.entrySet()) {
                caseWhen.append("WHEN '").append(entry.getKey()).append("' THEN ").append(entry.getValue()).append(" ");
            }
            caseWhen.append("ELSE 0 END");
            // 主排序：按偏好权重降序
            queryWrapper.orderByDesc(caseWhen.toString());
            // 次排序：同类型内按创建时间降序（新任务优先）
            queryWrapper.orderByDesc("createTime");
        } else {
            // 没有历史记录，退化为按时间排序
            queryWrapper.orderByDesc("createTime");
        }

        // 5. 分页查询
        Page<Task> taskPage = taskService.page(new Page<>(current, size), queryWrapper);
        return taskService.getTaskVOPage(taskPage);
    }

    /**
     * 计算各任务类型的权重分数
     * 权重 = 该类型完成数 / 总完成数，范围 [0, 1]
     *
     * @param typeStats 各类型统计结果
     * @return 类型 -> 权重映射
     */
    private Map<String, Double> calculateTypeWeights(List<Map<String, Object>> typeStats) {
        Map<String, Double> weights = new HashMap<>();
        if (typeStats == null || typeStats.isEmpty()) {
            return weights;
        }

        // 计算总任务数
        long totalCount = 0;
        for (Map<String, Object> stat : typeStats) {
            totalCount += ((Number) stat.get("count")).longValue();
        }

        // 计算各类型权重
        if (totalCount > 0) {
            for (Map<String, Object> stat : typeStats) {
                String type = (String) stat.get("taskType");
                long count = ((Number) stat.get("count")).longValue();
                // 权重 = 该类型数量 / 总数量
                double weight = (double) count / totalCount;
                weights.put(type, weight);
            }
        }

        return weights;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptTask(long taskId, HttpServletRequest request) {
        if (taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 校验用户角色：只有跑腿人员可以接单
        if (!UserRoleEnum.RUNNER.getValue().equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有跑腿人员可以接单");
        }

        // 查询任务是否存在
        Task task = taskService.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 校验任务状态：只有待接单状态的任务可以接单
        if (!TaskStatusEnum.PENDING.getValue().equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该任务已被接单或不可接单");
        }

        // 不能接自己发布的任务
        if (task.getPublisherId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能接自己发布的任务");
        }

        // 更新任务状态为进行中，设置接单者id
        Task updateTask = new Task();
        updateTask.setId(taskId);
        updateTask.setStatus(TaskStatusEnum.IN_PROGRESS.getValue());
        updateTask.setRunnerId(loginUser.getId());
        boolean result = taskService.updateById(updateTask);

        if (result) {
            // 更新跑腿人员的接单数量 +1
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("id", loginUser.getId())
                    .setSql("orderCount = orderCount + 1");
            userService.update(userUpdateWrapper);

            // 发送通知消息给任务发布者
            messageService.sendToUser(task.getPublisherId(),
                    "您的任务已被接单",
                    "您发布的任务「" + task.getTitle() + "」已被跑腿人员「" + loginUser.getUserName() + "」接单，正在进行中。",
                    "TASK_ACCEPTED");
        }

        return result;
    }
}
