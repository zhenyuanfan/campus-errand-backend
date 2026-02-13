package com.yuan.campuserrandbackend.model.enums;

import lombok.Getter;

/**
 * 反馈处理状态枚举
 */
@Getter
public enum FeedbackStatusEnum {

    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    RESOLVED("resolved", "已解决"),
    REJECTED("rejected", "已驳回");

    private final String value;
    private final String text;

    FeedbackStatusEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     */
    public static FeedbackStatusEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (FeedbackStatusEnum feedbackStatusEnum : FeedbackStatusEnum.values()) {
            if (feedbackStatusEnum.value.equals(value)) {
                return feedbackStatusEnum;
            }
        }
        return null;
    }
}
