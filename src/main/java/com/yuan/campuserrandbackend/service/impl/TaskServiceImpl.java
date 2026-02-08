package com.yuan.campuserrandbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuan.campuserrandbackend.exception.BusinessException;
import com.yuan.campuserrandbackend.exception.ErrorCode;
import com.yuan.campuserrandbackend.mapper.TaskMapper;
import com.yuan.campuserrandbackend.model.dto.task.TaskAddRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskQueryRequest;
import com.yuan.campuserrandbackend.model.dto.task.TaskUpdateRequest;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskTypeEnum;
import com.yuan.campuserrandbackend.model.vo.TaskVO;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 86182
 * @description 针对表【task(跑腿任务)】的数据库操作Service实现
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
        implements TaskService {

    @Resource
    private UserService userService;

    @Override
    public long addTask(TaskAddRequest taskAddRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 创建任务对象
        Task task = new Task();
        BeanUtils.copyProperties(taskAddRequest, task);
        task.setPublisherId(loginUser.getId());
        task.setStatus(TaskStatusEnum.PENDING.getValue());

        // 如果用户没有填写联系方式，使用用户的默认联系方式
        if (StrUtil.isBlank(task.getContactInfo())) {
            task.setContactInfo(loginUser.getContactInfo());
        }

        // 校验任务信息
        validTask(task, true);

        // 保存任务
        boolean result = this.save(task);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发布任务失败，数据库错误");
        }
        return task.getId();
    }

    @Override
    public void validTask(Task task, boolean add) {
        if (task == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String taskType = task.getTaskType();
        String title = task.getTitle();
        String description = task.getDescription();
        String startLocation = task.getStartLocation();
        String endLocation = task.getEndLocation();
        Date expectedTime = task.getExpectedTime();
        BigDecimal reward = task.getReward();
        String contactInfo = task.getContactInfo();

        // 新增时必填字段校验
        if (add) {
            if (StrUtil.isBlank(taskType)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务类型不能为空");
            }
            if (StrUtil.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务标题不能为空");
            }
            if (StrUtil.isBlank(startLocation)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "起始地点不能为空");
            }
            if (StrUtil.isBlank(endLocation)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "目的地不能为空");
            }
            if (expectedTime == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "期望完成时间不能为空");
            }
            if (reward == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "报酬金额不能为空");
            }
            if (StrUtil.isBlank(contactInfo)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "联系方式不能为空");
            }
        }

        // 任务类型合法性校验
        if (StrUtil.isNotBlank(taskType)) {
            TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(taskType);
            if (taskTypeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务类型不合法");
            }
        }

        // 标题长度校验
        if (StrUtil.isNotBlank(title) && title.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务标题过长，最多100字");
        }

        // 描述长度校验
        if (StrUtil.isNotBlank(description) && description.length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务描述过长，最多2000字");
        }

        // 期望完成时间合理性校验（不能是过去时间）
        if (expectedTime != null && expectedTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "期望完成时间不能是过去时间");
        }

        // 报酬金额合理性校验
        if (reward != null) {
            if (reward.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "报酬金额必须大于0");
            }
            if (reward.compareTo(new BigDecimal("10000")) > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "报酬金额不能超过10000元");
            }
        }
    }

    @Override
    public boolean updateTask(TaskUpdateRequest taskUpdateRequest, HttpServletRequest request) {
        Long taskId = taskUpdateRequest.getId();
        if (taskId == null || taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }

        // 查询任务是否存在
        Task oldTask = this.getById(taskId);
        if (oldTask == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 校验权限：只有发布者可以修改
        User loginUser = userService.getLoginUser(request);
        if (!oldTask.getPublisherId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改该任务");
        }

        // 只有待接单状态的任务可以修改
        if (!TaskStatusEnum.PENDING.getValue().equals(oldTask.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有待接单状态的任务可以修改");
        }

        // 更新任务
        Task task = new Task();
        BeanUtils.copyProperties(taskUpdateRequest, task);

        // 校验任务信息
        validTask(task, false);

        return this.updateById(task);
    }

    @Override
    public boolean deleteTask(long taskId, HttpServletRequest request) {
        if (taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }

        // 查询任务是否存在
        Task task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 校验权限：只有发布者可以删除
        User loginUser = userService.getLoginUser(request);
        if (!task.getPublisherId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除该任务");
        }

        // 只有待接单或已取消状态的任务可以删除
        String status = task.getStatus();
        if (!TaskStatusEnum.PENDING.getValue().equals(status)
                && !TaskStatusEnum.CANCELLED.getValue().equals(status)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有待接单或已取消状态的任务可以删除");
        }

        return this.removeById(taskId);
    }

    @Override
    public QueryWrapper<Task> getQueryWrapper(TaskQueryRequest taskQueryRequest) {
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        if (taskQueryRequest == null) {
            return queryWrapper;
        }

        Long id = taskQueryRequest.getId();
        Long publisherId = taskQueryRequest.getPublisherId();
        Long runnerId = taskQueryRequest.getRunnerId();
        String taskType = taskQueryRequest.getTaskType();
        String title = taskQueryRequest.getTitle();
        String status = taskQueryRequest.getStatus();
        String startLocation = taskQueryRequest.getStartLocation();
        String endLocation = taskQueryRequest.getEndLocation();
        BigDecimal minReward = taskQueryRequest.getMinReward();
        BigDecimal maxReward = taskQueryRequest.getMaxReward();
        String sortField = taskQueryRequest.getSortField();
        String sortOrder = taskQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(publisherId != null && publisherId > 0, "publisherId", publisherId);
        queryWrapper.eq(runnerId != null && runnerId > 0, "runnerId", runnerId);
        queryWrapper.eq(StrUtil.isNotBlank(taskType), "taskType", taskType);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.eq(StrUtil.isNotBlank(status), "status", status);
        queryWrapper.like(StrUtil.isNotBlank(startLocation), "startLocation", startLocation);
        queryWrapper.like(StrUtil.isNotBlank(endLocation), "endLocation", endLocation);
        queryWrapper.ge(minReward != null, "reward", minReward);
        queryWrapper.le(maxReward != null, "reward", maxReward);

        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc("createTime");
        }

        return queryWrapper;
    }

    @Override
    public TaskVO getTaskVO(Task task) {
        if (task == null) {
            return null;
        }
        TaskVO taskVO = new TaskVO();
        BeanUtils.copyProperties(task, taskVO);

        // 设置任务类型文本
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.getEnumByValue(task.getTaskType());
        if (taskTypeEnum != null) {
            taskVO.setTaskTypeText(taskTypeEnum.getText());
        }

        // 设置任务状态文本
        TaskStatusEnum taskStatusEnum = TaskStatusEnum.getEnumByValue(task.getStatus());
        if (taskStatusEnum != null) {
            taskVO.setStatusText(taskStatusEnum.getText());
        }

        // 设置发布者信息
        Long publisherId = task.getPublisherId();
        if (publisherId != null && publisherId > 0) {
            User publisher = userService.getById(publisherId);
            if (publisher != null) {
                taskVO.setPublisherName(publisher.getUserName());
            }
        }

        // 设置接单者信息
        Long runnerId = task.getRunnerId();
        if (runnerId != null && runnerId > 0) {
            User runner = userService.getById(runnerId);
            if (runner != null) {
                taskVO.setRunnerName(runner.getUserName());
            }
        }

        return taskVO;
    }

    @Override
    public Page<TaskVO> getTaskVOPage(Page<Task> taskPage) {
        List<Task> taskList = taskPage.getRecords();
        Page<TaskVO> taskVOPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        if (taskList == null || taskList.isEmpty()) {
            return taskVOPage;
        }
        List<TaskVO> taskVOList = taskList.stream()
                .map(this::getTaskVO)
                .collect(Collectors.toList());
        taskVOPage.setRecords(taskVOList);
        return taskVOPage;
    }

    @Override
    public boolean cancelTask(long taskId, HttpServletRequest request) {
        if (taskId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不合法");
        }

        // 查询任务是否存在
        Task task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        // 校验权限：只有发布者可以取消
        User loginUser = userService.getLoginUser(request);
        if (!task.getPublisherId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权取消该任务");
        }

        // 只有待接单或已接单状态的任务可以取消
        String status = task.getStatus();
        if (!TaskStatusEnum.PENDING.getValue().equals(status)
                && !TaskStatusEnum.ACCEPTED.getValue().equals(status)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有待接单或已接单状态的任务可以取消");
        }

        // 更新状态为已取消
        Task updateTask = new Task();
        updateTask.setId(taskId);
        updateTask.setStatus(TaskStatusEnum.CANCELLED.getValue());
        return this.updateById(updateTask);
    }
}
