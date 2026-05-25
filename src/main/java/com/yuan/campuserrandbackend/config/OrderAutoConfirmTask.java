package com.yuan.campuserrandbackend.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuan.campuserrandbackend.model.entity.Task;
import com.yuan.campuserrandbackend.model.entity.User;
import com.yuan.campuserrandbackend.model.enums.PaymentStatusEnum;
import com.yuan.campuserrandbackend.model.enums.TaskStatusEnum;
import com.yuan.campuserrandbackend.service.MessageService;
import com.yuan.campuserrandbackend.service.TaskService;
import com.yuan.campuserrandbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 订单自动确认定时任务
 * 待确认状态超过3天自动放款
 */
@Slf4j
@Component
public class OrderAutoConfirmTask {

    @Resource
    private TaskService taskService;

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    /**
     * 每小时执行一次，检查超过3天未确认的订单自动放款
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmOrders() {
        // 计算3天前的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        Date threeDaysAgo = calendar.getTime();

        // 查询超过3天仍为待确认状态的任务
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", TaskStatusEnum.CONFIRMED.getValue());
        queryWrapper.eq("paymentStatus", PaymentStatusEnum.PAID.getValue());
        queryWrapper.le("updateTime", threeDaysAgo);

        List<Task> tasks = taskService.list(queryWrapper);

        if (tasks.isEmpty()) {
            return;
        }

        log.info("自动确认定时任务：发现 {} 条超时未确认订单", tasks.size());

        for (Task task : tasks) {
            try {
                autoReleasePayment(task);
            } catch (Exception e) {
                log.error("自动放款失败，任务ID: {}, 错误: {}", task.getId(), e.getMessage());
            }
        }
    }

    /**
     * 自动放款给接单员
     */
    private void autoReleasePayment(Task task) {
        Long runnerId = task.getRunnerId();
        if (runnerId != null && runnerId > 0) {
            User runner = userService.getById(runnerId);
            if (runner != null) {
                BigDecimal runnerBalance = runner.getBalance() != null ? runner.getBalance() : BigDecimal.ZERO;
                User updateRunner = new User();
                updateRunner.setId(runnerId);
                updateRunner.setBalance(runnerBalance.add(task.getReward()));
                userService.updateById(updateRunner);
            }
        }

        // 更新任务状态
        Task updateTask = new Task();
        updateTask.setId(task.getId());
        updateTask.setStatus(TaskStatusEnum.COMPLETED.getValue());
        updateTask.setPaymentStatus(PaymentStatusEnum.RELEASED.getValue());
        taskService.updateById(updateTask);

        // 通知发布者
        messageService.sendToUser(task.getPublisherId(),
                "订单已自动确认",
                "您的任务「" + task.getTitle() + "」超过3天未确认收货，系统已自动确认并放款给接单员。",
                "AUTO_CONFIRM");

        // 通知接单员
        if (runnerId != null && runnerId > 0) {
            messageService.sendToUser(runnerId,
                    "报酬已到账（自动确认）",
                    "任务「" + task.getTitle() + "」已被系统自动确认，报酬" + task.getReward() + "元已转入您的账户。",
                    "PAYMENT_RELEASED");
        }

        log.info("自动放款成功，任务ID: {}, 金额: {}", task.getId(), task.getReward());
    }
}
