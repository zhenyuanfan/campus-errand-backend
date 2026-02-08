package com.yuan.campuserrandbackend.model.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum TaskStatusEnum {

    PENDING("pending", "待接单"),
    ACCEPTED("accepted", "已接单"),
    IN_PROGRESS("in_progress", "进行中"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消");

    private final String value;
    private final String text;

    TaskStatusEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     */
    public static TaskStatusEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (TaskStatusEnum taskStatusEnum : TaskStatusEnum.values()) {
            if (taskStatusEnum.value.equals(value)) {
                return taskStatusEnum;
            }
        }
        return null;
    }
}
